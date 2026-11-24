# 汽车零部件采购 ERP 微服务后端

本目录是仓库自带的 ERP 后端子系统，Spring Cloud 微服务实现，对外提供 44 条 `/api` 接口，其中 23 条为 `src/mcp_server` 的 MCP 契约。网关监听 `8081` 并保持 `/api/**` 路径不变，因此 Python 侧的 `ERP_BASE_URL`、23 个工具定义、FastAPI 与前端均无需改动。项目总览、整体架构与完整启动顺序见根 [README.md](../README.md)。

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
| `gateway` | 8081 | Spring Cloud Gateway：TraceId / CORS / 访问日志 / 限流 / JWT 校验 / 错误改写，五条 `lb://` 路由（catalog / order / inventory / report / auth） |
| `auth-service` | 8086 | 登录、服务账号换 token、刷新、当前用户、登出；六张权限表 |
| `catalog-service` | 8082 | 供应商 5 + 零部件 5 + 报价比价 3 + 供应商绩效 2；`/internal/parts/batch` |
| `order-service` | 8083 | 订单 7 + 采购申请 5 + 收货 3 + Outbox 2；状态机与状态流水 |
| `inventory-service` | 8084 | 库存 6；Lua 条件原子扣减 + 本地锁降级 |
| `report-service` | 8085 | 报表聚合，Feign 汇总订单与库存统计 |

注册与配置由 Nacos `:8848` 提供，`registry` 模块已移除。Nacos 不是本地启动的前置条件：`NACOS_DISCOVERY_ENABLED=false` 时不加载 Nacos 发现客户端，`lb://` 由 `SimpleDiscoveryClient` 的静态实例解析；`application-docker.yml` 反向打开 Nacos 发现与注册并排除两个静态发现自动配置。业务服务依赖 `erp-common-web` / `erp-common-data` / `erp-common-cache` / `erp-common-security`，网关只依赖 `erp-common-core`（不引入 Spring MVC，避免与 WebFlux 冲突）。

Gateway 只路由 `/api/**`，`/internal/**` 不对外暴露。订单明细的 `partName / supplierName` 与库存 VO 的嵌套 `partDetail` 由 OpenFeign 调用 `catalog-service` 批量组装，一次请求取回全部零件信息，避免逐条 N+1 调用。

23 条原 MCP 契约中不存在跨服务写操作；`POST /api/receives/create-with-stock` 是收货与库存入库的跨服务写场景，由 Seata AT 模式保证两库一致。

## 技术栈

Java 17、Spring Boot 3.2.5、Spring Cloud 2023.0.5 + Alibaba 2023.0.3.2（Nacos 注册与配置、Sentinel 1.8.8 流控熔断、Seata 2.1.0 AT 事务）、Spring Cloud Gateway 4.1.3、OpenFeign + LoadBalancer + Resilience4j、Spring Security 6 + jjwt 0.12.5（HS256 双层校验）、MyBatis-Plus 3.5.9（分页上限 100、乐观锁、逻辑删除、防全表更新）、MapStruct 1.5.5、Redis（Lettuce）/ Caffeine 双实现、H2（默认）/ MySQL 8、Actuator + Micrometer + Prometheus + Zipkin、Maven 3.9。父 POM 统一版本与插件管理，编译期挂 Lombok 与 MapStruct 注解处理器。

### 五项核心组件

- **Nacos**：注册中心与配置中心，各服务 `spring.config.import=optional:nacos:...`；本地 `NACOS_DISCOVERY_ENABLED=false` 由 `SimpleDiscoveryClient` 静态实例解析 `lb://`，docker profile 置 `true` 并排除静态发现自动配置，走真实注册与发现。
- **OpenFeign**：五个客户端均配置 `fallbackFactory`，统一超时 3s/5s；`TraceIdRequestInterceptor` 透传 traceId、用户上下文与 `Authorization`，`InternalSignatureInterceptor` 为 `/internal/**` 补签 `X-Internal-Token`。
- **Sentinel**：五个业务服务接入 dashboard `:8080`，流控与降级规则由 Nacos `SENTINEL_GROUP` 数据源下发，`configs/sentinel/` 的 12 份种子规则由 `nacos-seed` 一次性任务写入 Nacos。
- **Gateway**：5 条路由 + 6 个全局过滤器（traceId、CORS、访问日志、限流、JWT、错误改写）；本地以静态实例解析，docker profile 走 Nacos 发现，Responses 统一回写 `X-Trace-Id`。
- **Seata**：AT 模式，`erp-tx-group` 事务组经 Nacos 发现 `seata-server`；undo_log 表已建，事务入口为 `POST /api/receives/create-with-stock`；本地 `SEATA_ENABLED=false` 时注解为 no-op。

`configs/mysql/init.sql` 初始化四个业务库与 Seata 全局表，`configs/seata/` 提供注册与配置中心参数，`docker compose up -d` 一键拉起 15 个容器（含发布规则与 Seata 配置的 `nacos-seed` 一次性任务）。网关锁定 gateway-server 4.1.3 以匹配 Spring Framework 6.1.6。

Spring Cloud 2023.0.x 已结束开源支持。本工程用于微服务架构实践与本地验收，生产部署需升级到受支持的 Spring Boot / Spring Cloud release train。

## 快速启动

构建依赖与产物全部落在 D 盘：`.mvn/maven.config` 将 Maven 本地仓库指向 `D:/java/m2-repository`，JDK 17 位于 `D:/java/jdk-17`，Maven 位于 `D:/java/maven`。

```bash
cd backend
mvn -B -q -DskipTests clean package
```

默认不依赖外部中间件即可启动：Nacos 发现与注册均关闭（`NACOS_DISCOVERY_ENABLED=false`、`NACOS_REGISTER_ENABLED=false`），`lb://` 由静态实例解析，数据库为 H2 内存库，缓存与锁走 Caffeine / 本地实现。六个进程需共用同一个 `ERP_INTERNAL_SECRET`，否则 `/internal/**` 的签名校验不通过；不设置时该层不启用。按下列顺序启动，每个服务一个终端：

```bash
cd backend                                     # 每个终端分别进入该目录
export ERP_INTERNAL_SECRET=dev-internal-secret # 六个终端保持一致，不设置则签名层不启用
java -jar auth-service/target/erp-auth-service-1.0.0.jar --server.port=8086
java -jar catalog-service/target/erp-catalog-service-1.0.0.jar --server.port=8082
java -jar order-service/target/erp-order-service-1.0.0.jar --server.port=8083
java -jar inventory-service/target/erp-inventory-service-1.0.0.jar --server.port=8084
java -jar report-service/target/erp-report-service-1.0.0.jar --server.port=8085
java -jar gateway/target/erp-gateway-1.0.0.jar --server.port=8081
```

需要真实注册发现、Sentinel 规则下发、Seata 事务或 MySQL/Redis 时，先 `docker compose up -d` 拉起 15 个容器，再给每个服务加 `--spring.profiles.active=docker`；该 profile 打开 Nacos 发现与注册、排除静态发现自动配置、缓存切 `redis`，catalog/order/inventory 打开 Seata。

启动命令显式写出 `--server.port`。部分终端会注入 `SERVER__PORT` 环境变量，Spring Boot 的松弛绑定会用它覆盖 `application.yml` 中的端口，导致服务绑定到随机高位端口而启动失败；命令行参数优先级最高，可避免这种覆盖。带 HTTP 代理的终端执行 curl 时需加 `--noproxy '*'`。

完整链路：`auth(:8086) → catalog(:8082) → order(:8083) / inventory(:8084) / report(:8085) → gateway(:8081) → MCP(:9000) → FastAPI(:8000) → 前端(:3000)`，需要真实注册与配置下发时在链路最前面加 Nacos(:8848)。`catalog-service` 需先于 `order-service` 与 `inventory-service` 就绪，否则 Feign 调用无可用实例。发现与注册默认关闭，Nacos 不在时本地链路不受影响；以 docker profile 启动时才依赖 Nacos 与 Redis 就绪。

网关对 `/api/**` 强制校验 JWT，冒烟先换取 token：

```bash
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}' | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')

curl -H "Authorization: Bearer $TOKEN" "http://localhost:8081/api/suppliers/search?name=大众"
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8081/api/parts/page?current=1&size=5"
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8081/api/orders/statistics"
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8081/api/inventory/check"
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8081/api/reports/summary"
```

不带 token 时返回 HTTP 200 与 `code=401`，这是本工程的统一约定：业务错误不占用 HTTP 状态码。

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

- [x] 12 个模块 `mvn -B -DskipTests clean package` 构建通过，产出 6 个可执行 fat jar 与 6 个公共库 jar
- [x] 六个服务在本机 8081–8086 启动，`/actuator/health` 全部 `UP`
- [x] 网关五条 `lb://` 路由经静态实例解析：`suppliers / parts / orders / inventory / reports` 均返回 `code=200`
- [x] 无 token 请求返回 `code=401`，`X-Trace-Id` 在响应头回写并在三个服务日志中一致
- [x] 账号登录、服务账号换 token、`/api/auth/me` 正常；服务账号受 scope 约束访问业务接口
- [x] 内部签名：设置 `ERP_INTERNAL_SECRET` 后，未签名 `POST /internal/parts/batch` 返回 `code=401`，签名调用可返回零件明细并完成订单与库存 VO 富化
- [x] `orders/statistics` 返回 `totalOrders / totalAmount / statusCounts / partCount`
- [x] `reports/summary` 通过 Feign 汇总订单与库存，两段 `data` 均非空
- [x] `orders/update-status` 合法流转 `code=200`，非法流转 `code=4093`
- [x] `receives/create-with-stock` 收货入库，库存数量由 120 增至 122
- [x] 请购创建、提交、审批通过，重复审批返回 `code=409`
- [x] `quotes/compare/{partId}` 按单价升序返回，`performance/recalculate` 产出评分
- [x] 网关限流：同一路径连续 45 次请求中 9 次返回 `code=429`
- [ ] Nacos 控制台可见 6 个注册实例（需 `docker compose up -d`）
- [ ] Sentinel 规则在 dashboard 生效并触发流控（需 Nacos 发布规则）
- [ ] Seata AT 全局事务在跨库失败时回滚（需 MySQL + seata-server）
- [ ] MCP 网关加载 23 个工具并端到端返回真实数据

## 核验口径

本机一次全量构建（`mvn -B -DskipTests clean package`，退出码 0）后启动 auth、catalog、order、inventory、report、gateway 六个进程，以 `ERP_INTERNAL_SECRET=dev-internal-secret` 运行，未启动 Nacos、Redis、MySQL、Sentinel dashboard 与 Zipkin，全部走 H2 内存库与静态服务发现。按上表逐项发起 HTTP 请求，覆盖鉴权、路由、跨服务调用、内部签名、状态机、收货入库、请购审批、报价比价、绩效重算、报表聚合与网关限流，结果与表中标记一致。

未验证部分为依赖外部中间件的链路：Nacos 真实注册与配置下发、Sentinel 规则加载与限流触发、Seata 全局事务回滚、MySQL 方言下的建表与查询、Docker 编排整体拉起、MCP 与 FastAPI 端到端。这些链路在 `docker-compose.yml` 中已配置，需在有 Docker 的环境执行 `docker compose up -d` 后复测。

接口路径与响应结构的依据为 `src/mcp_server/tools/` 下四个工具定义文件、`src/mcp_server/http_base.py` 与项目根 `.env.example`；整体架构、启动顺序与文档索引见根 [README.md](../README.md)。

## 边界

本工程覆盖供应商、零部件、采购订单、库存四个采购域，并补齐报价比价、供应商绩效、采购申请与审批、收货、订单状态机、Outbox 补偿与报表聚合。鉴权为网关与服务双层 JWT 校验，服务间调用 `/internal/**` 由 `X-Internal-Token` 的 HMAC-SHA256 与 60 秒时间窗校验，`ERP_INTERNAL_SECRET` 未配置时该层不启用；MCP 侧以服务账号换取 token；`ERP_SECURITY_ENABLED=false` 仅用于本地排障。注册与配置走 Nacos，Redis 与 MySQL 为可选增强，缺省以本地缓存实现与 H2 运行。收货入库的跨库写由 Seata AT 模式保证一致，其余跨服务通知走 Outbox 补偿。
