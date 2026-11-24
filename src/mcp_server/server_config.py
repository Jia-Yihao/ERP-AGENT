"""
MCP Server 配置模块
项目内置 ERP 网关地址 + MCP 监听配置
"""
import os
from dotenv import load_dotenv

# 加载项目根目录 .env # 加载项目根目录 .env（MCP Server 独立运行，需要自己加载）
load_dotenv(os.path.join(os.path.dirname(__file__), '..', '..', '.env'))

# 项目内置 ERP 网关地址（backend/gateway :8081）
ERP_BASE_URL = os.getenv("ERP_BASE_URL", "http://localhost:8081")

# MCP Server 监听配置
MCP_HOST = os.getenv("MCP_HOST", "0.0.0.0")
MCP_PORT = int(os.getenv("MCP_PORT", "9000"))

# HTTP 客户端配置
HTTP_TIMEOUT = int(os.getenv("HTTP_TIMEOUT", "30"))
HTTP_MAX_CONNECTIONS = int(os.getenv("HTTP_MAX_CONNECTIONS", "20"))

# ERP 服务账号（用于换取访问 token）
ERP_CLIENT_ID = os.getenv("ERP_CLIENT_ID", "mcp-gateway")
ERP_CLIENT_SECRET = os.getenv("ERP_CLIENT_SECRET", "mcp-secret-change-me")
ERP_AUTH_ENABLED = os.getenv("ERP_AUTH_ENABLED", "true").lower() in ("1", "true", "yes")
