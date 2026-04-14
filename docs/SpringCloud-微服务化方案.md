# LeaseGo 微服务化方案

## 一、项目现状分析

### 1.1 当前架构
```
┌─────────────────────────────────────────┐
│              LeaseGo Monolith           │
├─────────────────────────────────────────┤
│  web-admin (管理系统)                    │
│    - 公寓/房间管理                        │
│    - 用户管理                            │
│    - 预约管理                            │
│    - 系统配置                            │
├─────────────────────────────────────────┤
│  web-app (用户端)                        │
│    - 公寓/房间浏览                        │
│    - 预约看房                            │
│    - 租赁签约                            │
│    - 聊天功能                            │
│    - 用户中心                            │
├─────────────────────────────────────────┤
│  common (公共模块)                        │
│  model (实体模块)                        │
└─────────────────────────────────────────┘
```

### 1.2 现有数据库表
| 模块 | 表名 | 说明 |
|------|------|------|
| 区域 | province_info, city_info, district_info | 省市县三级区域 |
| 公寓 | apartment_info, room_info | 公寓和房间信息 |
| 设施 | facility_info, apartment_facility, room_facility | 配套设置 |
| 标签 | label_info, apartment_label, room_label | 公寓/房间标签 |
| 属性 | attr_key, attr_value, room_attr_value | 属性配置 |
| 费用 | fee_key, fee_value, apartment_fee_value | 费用项目 |
| 租赁 | lease_agreement, lease_term, payment_type | 租赁相关 |
| 预约 | view_appointment | 看房预约 |
| 浏览 | browsing_history | 浏览历史 |
| 用户 | user_info, system_user | C端用户/系统用户 |
| 聊天 | chat_conversation, chat_message, chat_conversation_read | 即时通讯 |
| 图片 | graph_info | 图片管理 |

### 1.3 现有业务模块
- **web-admin**: 17个Controller，提供后台管理API
- **web-app**: 13个Controller，提供用户端API
- **Spring Boot 3.0.5 + Mybatis-Plus 3.5.3.1**

---

## 二、微服务拆分方案

### 2.1 服务架构图
```
                                    ┌──────────────────┐
                                    │   API Gateway    │
                                    │  (Spring Cloud   │
                                    │   Gateway +      │
                                    │   Sentinel)      │
                                    └────────┬─────────┘
                                             │
        ┌────────────────────────────────────┼────────────────────────────────────┐
        │                                    │                                    │
        ▼                                    ▼                                    ▼
┌───────────────┐                    ┌───────────────┐                    ┌───────────────┐
│  UPMS Service │                    │   Chat Service│                    │  SMS Service  │
│  (用户权限)    │                    │   (即时通讯)   │                    │   (短信服务)  │
└───────────────┘                    └───────────────┘                    └───────────────┘
        │                                    │                                    │
        └────────────────────────────────────┼────────────────────────────────────┘
                                             │
                                             ▼
                                    ┌──────────────────┐
                                    │   Nacos Config   │
                                    │   & Registry     │
                                    └────────┬─────────┘
                                             │
        ┌────────────────────────────────────┼────────────────────────────────────┐
        │           Service Layer            │                                    │
        ▼                   ▼                ▼                ▼                    ▼
┌───────────────┐  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐
│  Auth Service │  │ Region Service│  │Apartment Svc  │  │Appointment Svc│  │  Lease Service│
│  (认证服务)    │  │  (区域服务)    │  │  (公寓服务)    │  │  (预约服务)    │  │  (租赁服务)   │
└───────────────┘  └───────────────┘  └───────────────┘  └───────────────┘  └───────────────┘
        │                   │                   │                  │                    │
        └───────────────────┴───────────────────┴──────────────────┴────────────────────┘
                                             │
                                             ▼
                                    ┌──────────────────┐
                                    │   MySQL Cluster  │
                                    │   (分库分表)      │
                                    └──────────────────┘
```

### 2.2 服务详细拆分

| 服务名称 | 端口 | 职责 | 核心API |
|----------|------|------|---------|
| **gateway** | 8080 | API网关、路由、限流、认证 | /api/** |
| **auth-service** | 8081 | 用户认证、Token发放 | /auth/** |
| **upms-service** | 8082 | 系统用户管理、权限管理 | /admin/** |
| **region-service** | 8083 | 省市县区域信息 | /region/** |
| **apartment-service** | 8084 | 公寓、房间、设施、标签管理 | /apartment/** |
| **appointment-service** | 8085 | 看房预约管理 | /appointment/** |
| **lease-service** | 8086 | 租赁合同、租期、费用管理 | /lease/** |
| **chat-service** | 8087 | 即时通讯、聊天记录 | /chat/** |
| **file-service** | 8088 | 文件上传、图片管理 | /file/** |
| **sms-service** | 8089 | 短信发送 | /sms/** |

### 2.3 前端拆分
| 项目 | 说明 | 路由前缀 |
|------|------|---------|
| web-admin-front | 后台管理系统 | /admin/** |
| web-app-front | 用户端小程序/H5 | /app/** |

---

## 三、技术栈选型

### 3.1 核心组件

| 组件 | 技术选型 | 版本 | 说明 |
|------|---------|------|------|
| 基础框架 | Spring Boot | 3.0.5 | 保持一致 |
| 微服务框架 | Spring Cloud Alibaba | 2021.0.1.0 | 阿里生态集成 |
| 服务注册与配置 | Nacos | 2.2.x | 服务发现、配置中心 |
| API网关 | Spring Cloud Gateway | 3.4.x | 路由转发、限流 |
| 熔断限流 | Sentinel | 1.8.x | 流量控制、熔断降级 |
| 分布式事务 | Seata | 1.6.x | AT模式分布式事务 |
| ORM框架 | Mybatis-Plus | 3.5.3.1 | 保持一致 |
| 数据库 | MySQL | 8.0 | 分库分表 |
| 缓存 | Redis | 7.0 | Session共享、缓存 |
| 消息队列 | RocketMQ | 5.x | 异步解耦 |
| 对象存储 | MinIO | - | 文件存储 |
| 日志 | ELK | - | 分布式日志 |

### 3.2 依赖版本兼容矩阵

```
Spring Boot 3.0.5
├── Spring Cloud 2021.0.3
│   ├── Spring Cloud Alibaba 2021.0.1.0
│   │   ├── Nacos 2.2.x
│   │   ├── Sentinel 1.8.x
│   │   └── Seata 1.6.x
│   └── Spring Cloud Gateway 3.4.x
└── Mybatis-Plus 3.5.3.1
```

---

## 四、服务模块设计

### 4.1 父项目结构
```
leasego/
├── leasego-common/           # 公共模块
│   ├── leasego-common-core/  # 核心工具类
│   ├── leasego-common-mybatis/  # Mybatis增强
│   ├── leasego-common-redis/    # Redis配置
│   ├── leasego-common-swagger/  # Swagger文档
│   └── leasego-common-util/     # 工具类
├── leasego-model/            # 数据模型
├── leasego-gateway/          # API网关
├── leasego-auth/             # 认证服务
├── leasego-upms/             # 用户权限服务
├── leasego-region/           # 区域服务
├── leasego-apartment/        # 公寓服务
├── leasego-appointment/      # 预约服务
├── leasego-lease/            # 租赁服务
├── leasego-chat/             # 聊天服务
├── leasego-file/             # 文件服务
├── leasego-sms/              # 短信服务
└── pom.xml
```

### 4.2 各服务数据库分配

| 服务 | 数据库 | 说明 |
|------|--------|------|
| upms-service | lease_upms | system_user, system_post |
| region-service | lease_region | province_info, city_info, district_info |
| apartment-service | lease_apartment | apartment_info, room_info, facility_info, label_info, attr_key, attr_value, graph_info |
| appointment-service | lease_appointment | view_appointment, browsing_history |
| lease-service | lease_lease | lease_agreement, lease_term, payment_type, fee_key, fee_value |
| chat-service | lease_chat | chat_conversation, chat_message, chat_conversation_read |
| auth-service | lease_upms | user_info (共享) |
| file-service | lease_file | graph_info (图片引用) |
| sms-service | - | 无持久化 |

### 4.3 API路由设计

```
网关路由配置 (/api/{service}/{path})

/api/auth/login          → auth-service:8081
/api/auth/sms            → auth-service:8081

/api/admin/users          → upms-service:8082
/api/admin/posts          → upms-service:8082

/api/region/provinces     → region-service:8083
/api/region/cities        → region-service:8083
/api/region/districts      → region-service:8083

/api/apartment/list       → apartment-service:8084
/api/apartment/detail      → apartment-service:8084
/api/apartment/rooms       → apartment-service:8084
/api/apartment/facilities  → apartment-service:8084

/api/appointment/book     → appointment-service:8085
/api/appointment/list      → appointment-service:8085

/api/lease/agreement       → lease-service:8086
/api/lease/terms            → lease-service:8086

/api/chat/conversations     → chat-service:8087
/api/chat/messages          → chat-service:8087

/api/file/upload            → file-service:8088
/api/sms/send               → sms-service:8089
```

---

## 五、实施计划

### 阶段一：基础设施搭建（第1-2周）

**目标**：搭建微服务基础设施

| 序号 | 任务 | 详情 |
|------|------|------|
| 1.1 | 搭建父项目结构 | 创建多模块Maven项目，配置依赖管理 |
| 1.2 | 部署Nacos | 单机部署Nacos 2.2.x，配置服务注册与配置中心 |
| 1.3 | 创建Gateway服务 | 搭建网关服务，配置路由规则、限流、Sentinel |
| 1.4 | 抽取公共模块 | 将common和model拆分为独立公共模块 |
| 1.5 | 统一异常处理 | 封装全局异常、响应格式 |

**交付物**：
- 完整的Maven多模块项目结构
- Nacos服务注册与配置中心
- 可用的API网关

### 阶段二：核心服务拆分（第3-5周）

**目标**：拆分核心业务服务

| 序号 | 任务 | 服务 | 详情 |
|------|------|------|------|
| 2.1 | 认证服务 | auth-service | 登录、短信验证码、JWT Token |
| 2.2 | 用户权限服务 | upms-service | 系统用户、角色、权限 |
| 2.3 | 区域服务 | region-service | 省市县三级联动 |
| 2.4 | 公寓服务 | apartment-service | 公寓、房间、设施、标签 |
| 2.5 | 文件服务 | file-service | 图片上传、MinIO集成 |

**交付物**：
- 5个独立运行的微服务
- 完整的API文档
- 服务间调用测试

### 阶段三：业务服务拆分（第6-8周）

**目标**：完成剩余业务服务拆分

| 序号 | 任务 | 服务 | 详情 |
|------|------|------|------|
| 3.1 | 预约服务 | appointment-service | 看房预约、浏览历史 |
| 3.2 | 租赁服务 | lease-service | 租赁合同、租期、费用 |
| 3.3 | 聊天服务 | chat-service | 即时通讯、WebSocket |
| 3.4 | 短信服务 | sms-service | 阿里云短信集成 |

**交付物**：
- 4个独立运行的微服务
- WebSocket聊天功能
- 完整的业务流程

### 阶段四：高级特性（第9-10周）

**目标**：实现分布式特性

| 序号 | 任务 | 技术 | 详情 |
|------|------|------|------|
| 4.1 | 分布式事务 | Seata | 跨服务事务一致性 |
| 4.2 | 消息队列 | RocketMQ | 异步解耦、最终一致性 |
| 4.3 | 缓存优化 | Redis | Session共享、热点数据缓存 |
| 4.4 | 服务监控 | Sentinel Dashboard | 流量监控、熔断规则 |
| 4.5 | 日志中心 | ELK | 分布式日志收集 |

### 阶段五：前端拆分与联调（第11-12周）

**目标**：完成前端拆分并全流程联调

| 序号 | 任务 | 详情 |
|------|------|------|
| 5.1 | 前端项目拆分 | 分离web-admin-front和web-app-front |
| 5.2 | 网关路由配置 | 前端请求路由到网关 |
| 5.3 | 多服务联调 | 全流程功能测试 |
| 5.4 | 性能测试 | 压测、调优 |
| 5.5 | 部署文档 | 编写部署手册 |

---

## 六、关键问题与解决方案

### 6.1 数据一致性问题
**问题**：跨服务数据操作如何保证一致性？

**方案**：采用Seata AT模式
- 强一致性场景：使用Seata分布式事务（租赁签约）
- 最终一致性场景：使用RocketMQ异步消息（浏览历史）

### 6.2 服务间调用
**问题**：服务之间如何通信？

**方案**：
- 同步调用：OpenFeign声明式HTTP客户端
- 异步调用：RocketMQ消息队列
- 实时通信：WebSocket（聊天服务）

### 6.3 多租户问题
**问题**：后台管理多租户如何处理？

**方案**：
- 网关层解析租户ID
- ThreadLocal传递租户上下文
- Mybatis-Plus插件自动注入租户条件

### 6.4 配置管理
**问题**：配置如何统一管理？

**方案**：
- Nacos Config作为配置中心
- 区分application.yml和bootstrap.yml
- 支持配置热更新

### 6.5 API版本管理
**问题**：服务API版本如何管理？

**方案**：
- 网关层统一版本前缀 `/api/v1/**`
- 服务内部保持兼容性
- 重大变更再升级版本号

---

## 七、迁移风险与规避

### 7.1 风险清单

| 风险 | 影响 | 规避措施 |
|------|------|---------|
| 数据库分库性能 | 查询跨库Join困难 | 适当冗余、异步同步 |
| 服务拆分复杂度 | 业务逻辑耦合 | 按边界逐步拆分 |
| 网络延迟 | 调用链路过长 | 熔断降级、异步处理 |
| 运维复杂度 | 部署监控困难 | DevOps自动化 |

### 7.2 灰度策略
1. **蓝绿部署**：新版本并行运行，流量切换
2. **灰度发布**：按用户比例灰度
3. **回滚机制**：快速回滚到单点版本

---

## 八、预期收益

| 指标 | 现状 | 目标 |
|------|------|------|
| 模块耦合度 | 高耦合 | 低耦合，独立部署 |
| 团队协作 | 冲突频繁 | 独立迭代，互不影响 |
| 扩展性 | 整体扩展 | 按需扩展瓶颈服务 |
| 容错性 | 单点故障 | 熔断降级，局部故障 |
| 技术升级 | 牵一发动全身 | 独立升级，快速迭代 |

---

## 九、附录

### 9.1 环境要求
- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 7.0+
- Nacos 2.2.x
- Node.js 16+ (前端)

### 9.2 参考文档
- [Spring Cloud Alibaba 官方文档](https://spring-cloud-alibaba-group.github.io/github-pages/2021.0.1.0/en-us/index.html)
- [Nacos 官方文档](https://nacos.io/zh-cn/docs/quick-start.html)
- [Sentinel 官方文档](https://sentinelguard.io/zh-cn/docs/introduction.html)
- [Seata 官方文档](https://seata.io/zh-cn/docs/overview/what-is-seata.html)

### 9.3 项目结构预览
```
leasego/
├── leasego-common/
│   ├── leasego-common-core/
│   ├── leasego-common-mybatis/
│   ├── leasego-common-redis/
│   ├── leasego-common-swagger/
│   └── leasego-common-util/
├── leasego-model/
├── leasego-gateway/
├── leasego-auth/
├── leasego-upms/
├── leasego-region/
├── leasego-apartment/
├── leasego-appointment/
├── leasego-lease/
├── leasego-chat/
├── leasego-file/
└── leasego-sms/
```
