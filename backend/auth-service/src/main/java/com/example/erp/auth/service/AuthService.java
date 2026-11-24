package com.example.erp.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.erp.auth.dto.AuthDtos.MeVO;
import com.example.erp.auth.dto.AuthDtos.TokenVO;
import com.example.erp.auth.entity.SysClient;
import com.example.erp.auth.entity.SysPermission;
import com.example.erp.auth.entity.SysRole;
import com.example.erp.auth.entity.SysRolePermission;
import com.example.erp.auth.entity.SysUser;
import com.example.erp.auth.entity.SysUserRole;
import com.example.erp.auth.mapper.SysClientMapper;
import com.example.erp.auth.mapper.SysPermissionMapper;
import com.example.erp.auth.mapper.SysRoleMapper;
import com.example.erp.auth.mapper.SysRolePermissionMapper;
import com.example.erp.auth.mapper.SysUserMapper;
import com.example.erp.auth.mapper.SysUserRoleMapper;
import com.example.erp.common.core.BizException;
import com.example.erp.common.core.ErrorCode;
import com.example.erp.common.security.JwtProperties;
import com.example.erp.common.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class AuthService {

    private static final String WEB_CLIENT = "erp-web";

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysClientMapper clientMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final JwtProperties jwtProperties;
    private final TokenBlacklist blacklist;
    private final String mcpClientSecret;

    public AuthService(SysUserMapper userMapper,
                       SysRoleMapper roleMapper,
                       SysPermissionMapper permissionMapper,
                       SysUserRoleMapper userRoleMapper,
                       SysRolePermissionMapper rolePermissionMapper,
                       SysClientMapper clientMapper,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       JwtProperties jwtProperties,
                       TokenBlacklist blacklist,
                       @Value("${erp.mcp.client-secret:change-me}") String mcpClientSecret) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.userRoleMapper = userRoleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.clientMapper = clientMapper;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.jwtProperties = jwtProperties;
        this.blacklist = blacklist;
        this.mcpClientSecret = mcpClientSecret;
    }

    @Transactional(rollbackFor = Exception.class)
    public TokenVO login(String username, String password) {
        SysUser user = userMapper.selectOne(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, username));
        if (user == null || !isEnabled(user.getStatus()) || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);
        return issueUserTokens(user, WEB_CLIENT);
    }

    @Transactional(rollbackFor = Exception.class)
    public TokenVO token(String clientId, String clientSecret) {
        SysClient client = clientMapper.selectOne(
                Wrappers.<SysClient>lambdaQuery().eq(SysClient::getClientId, clientId));
        if (client == null || !isEnabled(client.getStatus())) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "客户端不可用");
        }
        String storedHash = client.getClientSecretHash();
        if (storedHash == null || storedHash.isBlank()) {
            if (!mcpClientSecret.equals(clientSecret)) {
                throw new BizException(ErrorCode.UNAUTHORIZED, "客户端凭证错误");
            }
            client.setClientSecretHash(passwordEncoder.encode(clientSecret));
            clientMapper.updateById(client);
        } else if (!passwordEncoder.matches(clientSecret, storedHash)) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "客户端凭证错误");
        }
        List<String> scopes = parseScopes(client.getScopes());
        String accessToken = tokenProvider.issueAccess(0L, "service:" + clientId,
                List.of("ROLE_SERVICE"), scopes, clientId);
        return new TokenVO(accessToken, null, "Bearer", jwtProperties.getAccessTtlSeconds());
    }

    public TokenVO refresh(String refreshToken) {
        if (!tokenProvider.validate(refreshToken)) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "刷新令牌无效");
        }
        Claims claims = tokenProvider.parse(refreshToken);
        if (!"refresh".equals(tokenProvider.getType(claims))) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "令牌类型错误");
        }
        SysUser user = userMapper.selectById(tokenProvider.getUserId(claims));
        if (user == null || !isEnabled(user.getStatus())) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "用户不可用");
        }
        String clientId = tokenProvider.getClientId(claims);
        return issueUserTokens(user, clientId == null ? WEB_CLIENT : clientId);
    }

    public MeVO me(Long userId) {
        SysUser user = userId == null ? null : userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return new MeVO(user.getId(), user.getUsername(), user.getNickname(),
                roleCodes(userId), permissionCodes(userId));
    }

    public void logout(String token) {
        if (token == null || token.isBlank() || !tokenProvider.validate(token)) {
            return;
        }
        Claims claims = tokenProvider.parse(token);
        blacklist.add(tokenProvider.getJti(claims), tokenProvider.getExpirationSeconds(claims));
    }

    private TokenVO issueUserTokens(SysUser user, String clientId) {
        Long userId = user.getId();
        String accessToken = tokenProvider.issueAccess(userId, user.getUsername(),
                roleCodes(userId), permissionCodes(userId), clientId);
        String refreshToken = tokenProvider.issueRefresh(userId, clientId);
        return new TokenVO(accessToken, refreshToken, "Bearer", jwtProperties.getAccessTtlSeconds());
    }

    private List<String> roleCodes(Long userId) {
        List<SysUserRole> links = userRoleMapper.selectList(
                Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId));
        List<Long> roleIds = links.stream().map(SysUserRole::getRoleId).filter(Objects::nonNull).toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return roleMapper.selectBatchIds(roleIds).stream()
                .map(SysRole::getRoleCode)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private List<String> permissionCodes(Long userId) {
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId));
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).filter(Objects::nonNull).toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<SysRolePermission> query = Wrappers.<SysRolePermission>lambdaQuery()
                .in(SysRolePermission::getRoleId, roleIds);
        List<SysRolePermission> links = rolePermissionMapper.selectList(query);
        List<Long> permissionIds = links.stream()
                .map(SysRolePermission::getPermissionId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (permissionIds.isEmpty()) {
            return List.of();
        }
        return permissionMapper.selectBatchIds(permissionIds).stream()
                .map(SysPermission::getPermCode)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private static List<String> parseScopes(String scopes) {
        if (scopes == null || scopes.isBlank()) {
            return List.of();
        }
        return Arrays.stream(scopes.split(","))
                .map(String::trim)
                .filter(scope -> !scope.isEmpty())
                .toList();
    }

    private static boolean isEnabled(Integer status) {
        return status != null && status == 1;
    }
}
