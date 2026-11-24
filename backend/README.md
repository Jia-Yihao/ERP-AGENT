# 汽车零部件采购 ERP 微服务后端

本目录是仓库自带的 ERP 后端子系统，Spring Cloud 微服务实现，对外提供 23 条 REST 接口，供 `src/mcp_server` 的 MCP 网关调用。网关监听 `8081` 并保持 `/api/**` 路径不变，因此 Python 侧的 `ERP_BASE_URL`、23 个工具定义、FastAPI 与前端均无需改动。项目总览、整体架构与完整启动顺序见根 [README.md](../README.md)。

## 架构

12 个 Maven 模块：六个基础库 + 六个可部署服务。

| 模块 | 端口 | 职责 |
|---|---|---|
| `erp-common-core` | — | 统一响应 `R`、分页 `PageResult`、错误码 `ErrorCode`、业务异常、用户上下文、跨服务 DTO |
| `erp-common-web` | — | 全局异常处理（9 分支）、OpenAPI 配置 |
| `erp-common-data` | — | `BaseEntity` 审计字段与逻辑删除、乐观锁、分页（`size` ≤ 100）、防全表更新、慢 SQL 拦截 |
| `erp-common-cache` | — | `CacheService` / `LockService` 双实现（Redis / Caffeine 本地）、AOP 幂等 |
| `erp-common-feign` | — | Feign 拦截、错误解码、降级、内部调用签名的基础库 |
| `erp-common-security` | — | JWT 签发解析、SecurityFilterChain、JwtAuthenticationFilter、权限注解、内部签名校验 |
| `gateway` | 8081 | Spring Cloud Gateway：TraceId / CORS / 访问日志 / 限流 / JWT 校验 / 错误改写，四条 `lb://` 路由 |
| `auth-service` | 8086 | 登录、服务账号换 token、刷新、当前用户、登出；六张权限表 |
| `catalog-service` | 8082 | 供应商 5 + 零部件 5 + 报价比价 3 + 供应商绩效 2；`/internal/parts/batch` |
| `order-service` | 8083 | 订单 7 + 采购申请 5 + 收货 2 + Outbox 2；状态机与状态流水 |
| `inventory-service` | 8084 | 库存 6；Lua 条件原子扣减 + 本地锁降级 |
| `report-service` | 8085 | 报表聚合，Feign 汇总订单与库存统计 |

注册与配置统一由外部 Nacos `:8848` 提供，`registry` 模块已移除。业务服务依赖 `erp-common-web` / `erp-common-data` / `erp-common-cache` / `erp-common-security`，网关只依赖 `erp-common-core`（不引入 Spring MVC，避免与 WebFlux 冲突）。

Gateway 只路由 `/api/**`，`/internal/**` 不对外暴露。订单明细的 `partName / supplierName` 与库存 VO 的嵌套 `partDetail` 由 OpenFeign 调用 `catalog-service` 批量组装，一次请求取回全部零件信息，避免逐条 N+1 调用。

23 条原 MCP 契约中不存在跨服务写操作；`POST /api/receives/create-with-stock` 是收货与库存入库的跨服务写场景，由 Seata AT 模式保证两库一致。

## 技术栈

Java 17、Spring Boot 3.2.5、Spring Cloud 2023.0.5 + Alibaba 2023.0.3.2（Nacos 注册与配置、Sentinel 1.8.8 流控熔断、Seata 2.1.0 AT 事务）、Spring Cloud Gateway 4.1.3、OpenFeign + LoadBalancer + Resilience4j、Spring Security 6 + jjwt 0.12.5（HS256 双层校验）、MyBatis-Plus 3.5.9（分页上限 100、乐观锁、逻辑删除、防全表更新）、MapStruct 1.5.5、Redis（Lettuce）/ Caffeine 双实现、H2（默认）/ MySQL 8、Actuator + Micrometer + Prometheus + Zipkin、Maven 3.9。父 POM 统一版本与插件管理，编译期挂 Lombok 与 MapStruct 注解处理器。

### 五项核心组件

- **Nacos**：注册中心与配置中心，各服务 `spring.config.import=optional:nacos:...`；无 Nacos 时以 `SimpleDiscoveryClient` 静态实例解析 `lb://`，`NACOS_REGISTER_ENABLED=true` 或 docker profile 切换到真实注册。
- **OpenFeign**：四个客户端均配置 `fallbackFactory`，统一超时 3s/5s；`TraceIdRequestInterceptor` 透传 traceId 与用户上下文头。
- **Sentinel**：网关与五个业务服务接入 dashboard `:8080`，流控与降级规则由 Nacos `SENTINEL_GROUP` 数据源下发，种子规则在 `configs/sentinel/`。
- **Gateway**：4 条路由 + 6 个全局过滤器（traceId、CORS、访问日志、限流、JWT、错误改写）；本地以静态实例解析，docker profile 走 Nacos 发现。
- **Seata**：AT 模式，`erp-tx-group` 事务组经 Nacos 发现 `seata-server`；undo_log 表已建，事务入口为 `POST /api/receives/create-with-stock`；本地 `SEATA_ENABLED=false` 时注解为 no-op。

`configs/mysql/init.sql` 初始化四个业务库与 Seata 全局表，`configs/seata/` 提供注册与配置中心参数，`docker compose up -d` 一键拉起 14 个容器。网关锁定 gateway-server 4.1.3 以匹配 Spring Framework 6.1.6。

Spring Cloud 2023.0.x 已结束开源支持。本工程用于微服务架构实践与本地验收，生产部署需升级到受支持的 Spring Boot / Spring Cloud release train。

## 快速启动

构建依赖与产物全部落在 D 盘：`.mvn/maven.config` 将 Maven 本地仓库指向 `D:/java/m2-repository`，JDK 17 位于 `D:/java/jdk-17`，Maven 位于 `D:/java/maven`。

```bash
cd backend
mvn -B -q -DskipTests clean package
```

启动 Nacos `:8848`（`docker compose up -d nacos`）后，按下列顺序启动各服务，每个服务一个终端：

```bash
java -jar auth-service/target/erp-auth-service-1.0.0.jar --server.port=8086
java -jar catalog-service/target/erp-catalog-service-1.0.0.jar --server.port=8082
java -jar order-service/target/erp-order-service-1.0.0.jar --server.port=8083
java -jar inventory-service/target/erp-inventory-service-1.0.0.jar --server.port=8084
java -jar report-service/target/erp-report-service-1.0.0.jar --server.port=8085
java -jar gateway/target/erp-gateway-1.0.0.jar --server.port=8081
```

启动命令显式写出 `--server.port`。部分终端会注入 `SERVER__PORT` 环境变量，Spring Boot 的松弛绑定会用它覆盖 `application.yml` 中的端口，导致服务绑定到随机高位端口而启动失败；命令行参数优先级最高，可避免这种覆盖。带 HTTP 代理的终端执行 curl 时需加 `--noproxy '*'`。

完整链路：`Nacos(:8848) → auth(:8086) → catalog(:8082) → order(:8083) / inventory(:8084) / report(:8085) → gateway(:8081) → MCP(:9000) → FastAPI(:8000) → 前端(:3000)`。`catalog-service` 需先于 `order-service` 与 `inventory-service` 就绪，否则 Feign 调用无可用实例；Nacos 未启动时各服务以 `optional:nacos:` 回退本地配置，注册动作失败但不阻断启动。

启动后可直接冒烟：

```bash
curl "http://localhost:8081/api/suppliers/search?name=大众"
curl "http://localhost:8081/api/parts/page?current=1&size=5"
curl "http://localhost:8081/api/orders/get/1"
curl "http://localhost:8081/api/inventory/warning"
curl "http://localhost:8081/api/inventory/check"
```

## 数据

每个服务持有独立库，默认使用 H2 内存模式，互不共享表：

| 服务 | 库 | 表 |
|---|---|---|
| auth-service | `mem:auth` | `sys_user`、`sys_role`、`sys_permission`、`sys_user_role`、`sys_role_permission`、`sys_client` |
| catalog-service | `mem:catalog` | `supplier`、`part`、`supplier_quote`、`supplier_performance` |
| order-service | `mem:orders` | `purchase_order`、`purchase_order_detail`、`purchase_requisition`、`requisition_approval`、`purchase_receive`、`order_status_log`、`outbox_event` |
| inventory-service | `mem:inventory` | `part_snapshot`、`inventory`、`inventory_log` |

`schema.sql` 使用 H2 MySQL 兼容模式与 MySQL 8 均支持的语法子集，四库共 20 张表，除 `part_snapshot` 外均继承 `BaseEntity` 审计字段。`data.sql` 载入 6 家汽车供应商（一汽大众、上汽大众、博世、采埃孚、大陆集团、华域汽车）、8 种零部件、24 条报价、6 条绩效、2 张示例订单、8 条库存记录（含预警样本）与 RBAC 种子。`spring.sql.init.encoding=UTF-8` 使中文种子数据按 UTF-8 读入，避免 Windows 平台默认编码造成的乱码。`spring.sql.init.mode=always` 会在每次启动重置数据，适用于演示与验收，生产环境应改为 `never` 并使用版本化迁移。

切换 MySQL 时设置环境变量 `DB_URL`、`DB_DRIVER=com.mysql.cj.jdbc.Driver`、`DB_USER`、`DB_PASS`，并先手动执行各服务的 `schema.sql`。DDL 未建二级索引，生产环境需按查询条件补索引。

## 接口契约

路径与 `src/mcp_server/tools/*.py` 中 23 个工具定义逐条对应，响应统一为 `{code, message, data}`。

| 分组 | 条数 | 路径 |
|---|---|---|
| 供应商 | 5 | `/api/suppliers/{search,page,get/{id},create,update-status/{id}}` |
| 零部件 | 5 | `/api/parts/{get/{id},search,supplier/{supplier_id},page,create}` |
| 订单 | 7 | `/api/orders/{create,update/{id},page,get/{id},search-details,statistics,update-status/{id}}` |
| 库存 | 6 | `/api/inventory/{warning,page,check,inbound,outbound,get/{id}}` |

契约相关的三条硬约束：业务错误返回 HTTP 200 且 `code` 非零，`http_base.py` 仅在 `status_code == 200` 时原样返回 `data`；`inbound`、`outbound`、两个 `update-status` 用 query 参数，Controller 不使用 `@RequestBody`；所有分页接口的 `data` 固定为 `records / total / current / size / pages`，`inventory/warning` 与 `inventory/page` 每条记录带 `partDetail`。

## 验收清单

- [x] 12 个模块 `mvn -DskipTests clean package` 构建通过，产出 6 个可执行 fat jar 与 6 个公共库 jar
- [ ] Nacos 控制台 `http://localhost:8848/nacos` 可见 6 个注册实例
- [ ] 23 条接口经 `:8081` 返回 HTTP 200 且 `code=200`
- [ ] `orders/get/{id}` 返回 `orderDetail[]`，`orders/search-details` 带 `partName / supplierName`
- [ ] `inventory/warning` 与 `inventory/page` 每条带 `partDetail`
- [ ] `inventory/outbound` 超量返回 `code=409` 且库存不变
- [ ] `orders/create` 重复订单号返回非 200 `code`，`totalAmount` 以明细重算为准
- [ ] MCP 网关 `python -m src.mcp_server.server_main` 加载 23 个工具并返回真实数据
- [ ] `python -m evals.agent_live` 跑通真实订单审批链路

## 核验口径

工程交付时执行的验证为一次全模块构建（`mvn -B -q -DskipTests clean package`，退出码 0）、一次静态契约核对（23 条 Controller 路径与 `src/mcp_server/tools/*.py` 逐条比对，无缺漏、无多出），以及一次单服务启动冒烟（`catalog-service` 在 `:8082` 启动，验证供应商查询、零件分页、零件详情、按供应商查询、内部批量接口的响应结构与中文数据，业务错误保持 HTTP 200）。未运行测试套件、未启动注册中心与网关、未发起服务间 Feign 调用、未执行 MCP 端到端联调，也未连接真实 MySQL。构建依赖落在 `D:/java/m2-repository`，C 盘 `.m2` 未写入。验收清单中除构建项外均为待执行项，需要按「快速启动」逐条实测后方可宣称通过。

接口路径与响应结构的依据为 `src/mcp_server/tools/` 下四个工具定义文件、`src/mcp_server/http_base.py` 与项目根 `.env.example`；整体架构、启动顺序与文档索引见根 [README.md](../README.md)。

## 边界

本工程覆盖供应商、零部件、采购订单、库存四个采购域，并补齐报价比价、供应商绩效、采购申请与审批、收货、订单状态机、Outbox 补偿与报表聚合。鉴权为网关与服务双层 JWT 校验，MCP 侧以服务账号换取 token；`ERP_SECURITY_ENABLED=false` 仅用于本地排障。注册与配置走 Nacos，Redis 与 MySQL 为可选增强，缺省以本地实现与 H2 运行。跨服务一致性用 Outbox 补偿，收货入库事件由定时任务扫描发送，不引入分布式事务组件。
