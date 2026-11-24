"""
HTTP 基础客户端模块
httpx AsyncClient 单例（连接池、超时），封装 GET/POST/PUT/PATCH/DELETE
"""
import httpx
import json
from typing import Any, Optional
from .server_config import (
    ERP_BASE_URL, HTTP_TIMEOUT, HTTP_MAX_CONNECTIONS,
    ERP_CLIENT_ID, ERP_CLIENT_SECRET, ERP_AUTH_ENABLED,
)
import time
import asyncio


class ERPHttpClient:
    """ERP 后端 HTTP 客户端单例"""
    """
    HTTP 客户端采用单例模式，因为其维护昂贵的连接池资源，全局共享可复用 TCP 连接、
    显著提升性能并统一超时/并发配置；而 LLM 实例不适合单例，因为不同任务需要独立的模型、温度、API Key 等参数配置，
    且调用本身是无状态的，按需创建更灵活、更经济。
    """
    _instance: Optional['ERPHttpClient'] = None
    _client: Optional[httpx.AsyncClient] = None
    _token: Optional[str] = None
    _token_expire_at: float = 0.0
    _token_lock: Optional[asyncio.Lock] = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
        return cls._instance

    @property
    def client(self) -> httpx.AsyncClient:
        if self._client is None or self._client.is_closed:
            self._client = httpx.AsyncClient(
                base_url=ERP_BASE_URL,
                timeout=httpx.Timeout(HTTP_TIMEOUT),
                limits=httpx.Limits(
                    max_connections=HTTP_MAX_CONNECTIONS,
                    max_keepalive_connections=10,
                ),
                headers={"Content-Type": "application/json"},
            )
        return self._client

    async def _ensure_token(self, force: bool = False) -> Optional[str]:
        """换取并缓存服务账号 token，过期前 5 分钟刷新。"""
        if not ERP_AUTH_ENABLED:
            return None
        if not force and self._token and time.time() < self._token_expire_at:
            return self._token
        if self._token_lock is None:
            self._token_lock = asyncio.Lock()
        async with self._token_lock:
            if not force and self._token and time.time() < self._token_expire_at:
                return self._token
            try:
                resp = await self.client.post("/api/auth/token", json={
                    "clientId": ERP_CLIENT_ID, "clientSecret": ERP_CLIENT_SECRET,
                })
                body = resp.json()
                if body.get("code") == 200 and body.get("data"):
                    data = body["data"]
                    self._token = data.get("accessToken") or data.get("access_token")
                    expires_in = data.get("expiresIn") or data.get("expires_in") or 7200
                    self._token_expire_at = time.time() + max(int(expires_in) - 300, 60)
            except Exception:
                self._token = None
            return self._token

    def _auth_headers(self, token: Optional[str]) -> dict:
        headers = {"Content-Type": "application/json"}
        if token:
            headers["Authorization"] = f"Bearer {token}"
        return headers

    async def _request(self, method: str, path: str, **kwargs) -> httpx.Response:
        """带鉴权的请求，收到 code=401 时强制刷新一次并重放。"""
        token = await self._ensure_token()
        headers = self._auth_headers(token)
        response = await self.client.request(method, path, headers=headers, **kwargs)
        if ERP_AUTH_ENABLED:
            try:
                if response.json().get("code") == 401:
                    token = await self._ensure_token(force=True)
                    response = await self.client.request(
                        method, path, headers=self._auth_headers(token), **kwargs)
            except Exception:
                pass
        return response

    async def get(self, path: str, params: Optional[dict] = None) -> dict:
        """GET 请求"""
        # 过滤掉 None 值的参数
        if params:
            params = {k: v for k, v in params.items() if v is not None}
        response = await self._request("GET", path, params=params)
        return self._handle_response(response)

    async def post(self, path: str, json_data: Optional[dict] = None, params: Optional[dict] = None) -> dict:
        """POST 请求"""
        if params:
            params = {k: v for k, v in params.items() if v is not None}
        response = await self._request("POST", path, json=json_data, params=params)
        return self._handle_response(response)

    async def put(self, path: str, json_data: Optional[dict] = None, params: Optional[dict] = None) -> dict:
        """PUT 请求"""
        if params:
            params = {k: v for k, v in params.items() if v is not None}
        response = await self._request("PUT", path, json=json_data, params=params)
        return self._handle_response(response)

    async def patch(self, path: str, params: Optional[dict] = None) -> dict:
        """PATCH 请求"""
        if params:
            params = {k: v for k, v in params.items() if v is not None}
        response = await self._request("PATCH", path, params=params)
        return self._handle_response(response)

    async def delete(self, path: str) -> dict:
        """DELETE 请求"""
        response = await self._request("DELETE", path)
        return self._handle_response(response)

    def _handle_response(self, response: httpx.Response) -> dict:
        """统一响应处理"""
        try:
            data = response.json()
            if response.status_code == 200:
                return data
            else:
                return {
                    "code": response.status_code,
                    "message": f"HTTP Error: {response.status_code}",
                    "data": data,
                }
        except json.JSONDecodeError:
            return {
                "code": response.status_code,
                "message": f"Response not JSON: {response.text[:500]}",
                "data": None,
            }

    async def close(self):
        """关闭客户端"""
        if self._client and not self._client.is_closed:
            await self._client.aclose()


# 全局单例
erp_client = ERPHttpClient()
