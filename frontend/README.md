# ERP 智能采购助手 — 前端

Next.js 16 + React 19 + TailwindCSS 4 的流式对话界面，展示 Harness 四阶段（Planning → Executing → Review → Result）、工具调用轨迹、TodoList 与 HITL 审批卡片。

## 本地启动

```bash
cd frontend
npm install
npm run dev
```

浏览器打开 http://localhost:3000 。

## 上游依赖

| 服务 | 端口 | 作用 |
|---|---|---|
| FastAPI 后端 | 8000 | SSE 流式对话、会话历史、中断恢复 |
| MCP 网关 | 9000 | 为后端 Agent 提供 23 个 ERP 工具 |
| ERP 网关 | 8081 | `backend/` 的 Spring Cloud Gateway |

前端只与 `:8000` 的 FastAPI 通信，不直接调用 MCP 或 ERP。未启动上游服务时页面仍可打开，但对话与业务工具不可用。完整启动顺序见根 [README.md](../README.md)。

## 主要目录

```
src/app/           App Router 页面与路由
src/components/    对话、中断交互、侧边栏与通用组件
src/hooks/         useChat / useSSE / useHistory
src/lib/           API 封装、SSE 解析与类型定义
```
