package com.example.erp.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/** 安全开关与放行路径。 */
@ConfigurationProperties(prefix = "erp.security")
public class SecurityProperties {

    private static final List<String> DEFAULTS = List.of(
            "/actuator/health/**", "/v3/api-docs/**", "/swagger-ui/**");

    private boolean enabled = true;

    private List<String> permitAll = new ArrayList<>(DEFAULTS);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getPermitAll() {
        return permitAll;
    }

    public void setPermitAll(List<String> permitAll) {
        this.permitAll = permitAll == null || permitAll.isEmpty() ? new ArrayList<>(DEFAULTS) : permitAll;
    }
}
