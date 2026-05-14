# LeaseGo | 公寓租赁管理系统

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.7-green.svg)](https://spring.io/projects/spring-boot)
[![MyBatis Plus](https://img.shields.io/badge/MyBatis%20Plus-3.5.9-blue.svg)](https://baomidou.com/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

## 项目简介
LeaseGo 是一款现代化的全栈公寓租赁管理系统，基于 **Spring Boot 3** 和 **Java 17** 开发。项目采用前后端分离架构，分为**后台管理系统 (web-admin)** 和 **移动端应用 (web-app)**。管理端实现了房源全生命周期管理、配套设施动态配置及租约审批；移动端则为用户提供便捷的找房、预约看房及合同管理服务。

## 系统架构
项目采用 Maven 多模块 + Spring Cloud 微服务化架构，确保代码的高内聚、低耦合与易扩展性：

**服务架构：**
```
Client → Gateway(8083) → web-app(8081)
                   ├── chat-service(8082) [WebSocket]
                   └── agent-service(8084) [AI Agent]

Nacos(8848): 服务发现 + 配置中心
Redis Stack(6379): 缓存存储 + 向量检索
RabbitMQ(5672/15672): 异步消息队列 (房间重索引、聊天持久化、浏览历史、租约通知)
```

**模块划分：**
- `common`: 核心公共模块。封装了全局异常处理、统一结果返回 (`Result`)、Redis 自定义序列化、MinIO 工具类及 `AuthenticationInterceptor` 认证拦截器。
- `model`: 数据模型模块。包含所有数据库实体类 (Entity)、业务视图对象 (VO) 以及业务枚举类(Enum)。
- `gateway`: API 网关服务(端口 8083)。统一入口，聚合 Knife4j 文档，转发请求至后端服务并传递认证 Token。
- `web-admin`: 后台管理端后端接口。负责公寓、房间、配套、标签、租约及系统用户与移动端用户的权限管理。
- `web-app`: 用户移动端后端接口。支持手机验证码登录、房源检索、浏览记录、预约看房、个人租约查询。
- `chat-service`: 独立聊天服务(端口 8082)。基于 WebSocket 实现即时通讯，支持跨实例消息分发(Redis Pub/Sub)。
- `agent-service`: AI Agent 服务(端口 8084)。基于 LangChain4j + MiniMax + 阿里 DashScope 实现自然语言房源检索。

## 技术栈
- **核心框架:** Java 17, Spring Boot 3.2.7, Spring Cloud Alibaba 2023.0.1.0
- **服务治理:** Nacos 2.2.3 (服务发现 + 配置中心)
- **持久层:** MyBatis-Plus 3.5.9 + MySQL 8.0
- **中间件:**
    - **Redis Stack (7.4):** 缓存存储 + 向量检索 (RediSearch HNSW)
    - **RabbitMQ (4.0):** 异步消息队列（持久化投递、重试机制、死信处理）
    - **MinIO:** 分布式对象存储，统一管理房源图片、聊天文件
- **AI Agent:**
    - **框架:** LangChain4j 1.0.0 (AiServices + RAG 管道)
    - **LLM:** MiniMax-M2.7 (OpenAI 兼容 API)
    - **Embedding:** 阿里 DashScope text-embedding-v4 (1024 维)
    - **向量检索:** Redis Stack RediSearch (KNN + cosine 距离)
- **实时通讯:** WebSocket + Redis Pub/Sub 实现跨实例消息推送
- **安全与验证:**
    - **认证:** JWT (JSON Web Token) 实现无状态登录
    - **验证:** EasyCaptcha (图形验证码) + 阿里云 SMS 服务(短信验证码)
- **API 网关:** Spring Cloud Gateway (内置 ForwardAuthFilter 转发 Token)
- **文档工具:** Knife4j 4.5.0 (基于 OpenAPI 3，聚合各服务文档)

## 核心功能
- **房源管理:** 支持公寓与房间的联动管理，包含配套、杂费及标签的动态增删改查、图片上传与地址查找(调用高德地图接口)。
- **登录鉴权:**
    - 管理端：用户名 + 密码 + 图形验证码校验。
    - 移动端：手机号 + 短信验证码快捷登录，自动注册。
- **租赁全流程:** 涵盖房源搜索（省市区/价格/支付方式多维过滤）、预约看房提交、租约签订及状态自动流转。
- **即时通讯(chat-service):** 独立聊天服务，基于 WebSocket 的实时聊天，支持跨实例消息分发（Redis Pub/Sub）、消息已读未读状态、用户搜索（按用户名模糊搜索/手机号精准搜索）、文件与头像上传。
- **房源与聊天联动:** 房间可关联房东用户，移动端详情页可查看房东信息并一键发起聊天。
- **AI 智能检索 (agent-service):** 基于 LangChain4j 构建的 RAG 系统，用户输入自然语言（如"西湖区2000元以内朝南带独卫"），通过向量检索 + LLM 匹配最合适的房源。
- **异步消息队列:** RabbitMQ 驱动四项异步任务：
  - 房间数据变更 → Agent 向量索引自动更新
  - 聊天消息可靠持久化（同时保留 Redis Pub/Sub 实时分发）
  - 浏览历史异步记录
  - 租约到期通知
- **系统工具:** 实现图片上传至 MinIO 存储桶、基于 Spring Task 的租约到期自动结束。

## 项目亮点
1. **高性能缓存架构:** 在移动端房源详情接口引入 **Redis 缓存策略**，通过"先查缓存，穿透查库"的逻辑减少数据库 IO 压力，并配合管理端修改时的失效机制确保数据一致性。
2. **异步消息队列:** 集成 **RabbitMQ** 处理房间变更→Agent 自动重索引、聊天消息持久化、浏览历史记录、租约到期通知等异步任务，支持重试机制和手动确认，确保消息不丢失。
3. **健壮的权限体系:** 封装 **`ThreadLocal` 上下文持有者 (`LoginUserHolder`)**，配合自定义拦截器实现用户信息的无感传递，有效隔离各线程间的用户信息。
4. **统一工程化规范:** 建立**全局异常拦截器 (`GlobalExceptionHandler`)**，统一捕获业务异常并返回标准 Result 格式。
5. **类型安全与转换:** 针对业务中大量的状态枚举，自定义 **`StringToBaseEnumConverterFactory`**，实现 Web 层请求参数到数据库枚举类的自动映射，增强了代码的可维护性。
6. **微服务架构:** 采用 **Spring Cloud Alibaba** 将单体应用拆分为 `gateway`、`web-app`、`chat-service` 三个独立服务，配合 **Nacos** 实现服务发现与配置管理。
7. **WebSocket 集群实时通讯:** chat-service 独立部署，基于 **WebSocket + Redis Pub/Sub** 实现跨实例消息分发，支持多实例部署下的实时聊天；结合 JWT 实现消息发送者的身份认证。
8. **API 网关统一入口:** **Spring Cloud Gateway** 作为统一入口，内置 `ForwardAuthFilter` 自动转发 `access-token` 请求头，聚合 **Knife4j** 文档至 `http://localhost:8083/doc.html`。
9. **房源聊天联动:** 将传统租赁平台的信息孤岛打通，租客可直接在房源详情页发起与房东的即时通讯，降低沟通成本，提升平台粘性。
10. **AI Agent 智能检索:** 基于 LangChain4j 构建 RAG（检索增强生成）管道，将结构化房源数据转为自然语言文档后嵌入 Redis Stack 向量库，结合 MiniMax LLM 实现自然语言驱动的智能房源推荐，并返回可视化房间卡片。

## 后续计划
1. ✅ 微服务化：引入 Spring Cloud Alibaba (Nacos/OpenFeign/Sentinel) 将聊天服务、用户服务拆分为独立微服务
2. ✅ WebSocket 集群：基于 Redis Pub/Sub 实现跨实例消息分发
3. ✅ 消息队列：引入 RabbitMQ 实现异步消息通信（房间重索引、聊天持久化、浏览历史、租约通知）
4. ✅ AI Agent 智能检索：基于 LangChain4j + 向量数据库的 RAG（接入 MiniMax + 阿里 DashScope）
5. 分布式锁：处理多并发情况下的超卖问题（Redisson）
6. 缓存一致性优化：目前的缓存一致性处理比较原始，计划引入缓存双删与 Canal 监控

## 快速开始
> **注意**：本项目采用前后端分离 + 微服务架构。本仓库为后端代码（Java），前端代码请访问 [LeaseGo-Frontend](https://github.com/yitian-chen/LeaseGo-Frontend)。

### 1. 环境准备
- JDK 17
- Nacos 2.2.3 / MySQL 8.0+ / Redis 6.2+ / MinIO
- Docker & Docker Compose

### 2. 配置文件说明
项目采用 **Nacos 作为配置中心**。在启动各服务前，需在 Nacos 控制台创建对应配置文件：
- `lease-web-app.yaml`：web-app 的数据库、Redis、MinIO、阿里云短信配置
- `lease-chat-service.yaml`：chat-service 的数据库、Redis、JWT 配置
- `lease-gateway.yaml`：gateway 的路由规则和 Knife4j 配置

详细配置内容请参考 `docs/微服务化改造流程.md`。

### 3. 初始化数据库
项目使用 MySQL 作为持久化存储。在启动后端服务前，请从以下步骤选其一初始化数据库：

1.**创建数据库**：在 MySQL 中创建名为 `lease` 的数据库。
   ```sql
   CREATE DATABASE IF NOT EXISTS lease CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
   ```
2.**导入脚本**: 将根目录下的 `leaseGo.sql` 导入到上述数据库中。
- **方式 A (命令行)**:
  ```Bash
  mysql -u your_username -p lease < leaseGo.sql
  ```
- **方式 B (图形化界面)**: 使用 Navicat、DataGrip 或 IntelliJ IDEA 的 Database 插件，右键点击 lease 数据库，选择"运行 SQL 文件"并选择根目录下的脚本。

### 4. 启动服务
**启动顺序**：Nacos → MySQL/Redis Stack/MinIO → web-app → chat-service → agent-service → gateway

```bash
# 1. 启动 Nacos（首次）
docker run -d --name nacos -p 8848:8848 -e MODE=standalone nacos/nacos-server:v2.2.3

# 2. 启动 MySQL/Redis Stack/MinIO（如未启动）
docker-compose up -d

# 3. 启动 web-app (端口 8081)
cd web/web-app && mvn spring-boot:run

# 4. 启动 chat-service (端口 8082)
cd chat-service && mvn spring-boot:run

# 5. 启动 agent-service (端口 8084)
cd agent-service && mvn spring-boot:run

# 6. 启动 gateway (端口 8083)
cd gateway && mvn spring-boot:run
```

> **首次启动 agent-service 后**，需调用 `/api/agent/admin/reindex` 触发房源数据向量索引。
> agent-service 需要设置环境变量：`MINIMAX_API_KEY`（MiniMax API 密钥）、`DASHSCOPE_API_KEY`（阿里 DashScope 密钥）。

### 5. Docker 部署中间件
项目提供 `docker-compose.yml.example`，可一键启动 MySQL、Redis、MinIO 等依赖服务：

```bash
# 复制示例配置并填写隐私信息
cp docker-compose.yml.example docker-compose.yml

# 启动所有中间件服务
docker-compose up -d

# 查看运行状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 停止并移除容器
docker-compose down
```

> **注意**：首次使用请先编辑 `docker-compose.yml`，填写以下空值：
> - `MYSQL_ROOT_PASSWORD`：MySQL root 密码
> - `MINIO_ROOT_PASSWORD`：MinIO 控制台密码

**已包含服务：**
| 服务 | 端口 | 说明 |
|------|------|------|
| MySQL | 3307 | 数据库，默认账号 root |
| Redis Stack | 6379/8001 | 缓存 + 向量检索，8001 为 RedisInsight 管理界面 |
| RabbitMQ | 5672/15672 | 异步消息队列，15672 为管理控制台 (guest/guest) |
| MinIO | 9000/9001 | 对象存储，API端口9000，控制台9001 |

**统一 API 文档入口**：http://localhost:8083/doc.html (Knife4j 聚合文档)

| 服务 | 说明 |
|------|------|
| web-app | http://localhost:8081 (登录、公寓、房间、预约、支付等业务接口) |
| chat-service | http://localhost:8082 (WebSocket 聊天服务) |
| agent-service | http://localhost:8084 (AI Agent 智能房源检索) |
| gateway | http://localhost:8083 (统一入口) |