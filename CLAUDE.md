# LeaseGo 项目指南

## 项目结构

```
lease/
├── common/           # 公共模块（异常处理、Redis、MinIO、JWT、RabbitMQ 配置）
├── model/            # 数据模型（Entity、VO、Enum）
├── web/
│   ├── web-admin/    # 后台管理 API（端口 8080）
│   └── web-app/      # 用户移动端 API（端口 8081）
├── gateway/          # API 网关（端口 8083）
├── chat-service/     # 聊天服务，WebSocket（端口 8082）
└── agent-service/    # AI Agent 智能检索（端口 8084）
```

## 技术栈

- Java 17, Spring Boot 3.2.7, Spring Cloud 2023.0.5
- Nacos（服务发现 + 配置中心）
- MyBatis-Plus 3.5.9 + MySQL 8.0
- Redis Stack（缓存 + 向量检索 RediSearch）
- RabbitMQ 4.0（异步消息队列）
- MinIO（对象存储）
- **Redisson 3.50（分布式锁：租约超卖、会话去重、定时任务防重复）**
- LangChain4j + MiniMax + 阿里 DashScope（AI Agent）
- Knife4j 4.5.0（API 文档）

## 模块职责

- **common**: 所有其他模块依赖它。包含全局异常处理、统一返回 Result、Redis 序列化、MinIO 客户端、JWT 工具、RabbitMQ 交换机/队列/绑定声明、Redisson 自动配置、AuthenticationInterceptor
- **model**: 纯数据模型，不包含业务逻辑。被所有模块依赖
- **web-admin**: 管理员端 API。对接管理后台前端，使用 MyBatis-Plus ServiceImpl
- **web-app**: 用户端 API。对接 H5 移动端前端
- **gateway**: Spring Cloud Gateway。统一入口，聚合 Knife4j 文档，转发请求
- **chat-service**: 基于 WebSocket 的实时聊天。使用 Redis Pub/Sub 跨实例分发消息，RabbitMQ 持久化
- **agent-service**: AI 房源检索。LangChain4j + Redis Stack 向量库 + MiniMax LLM

## 关键约定

### 包结构
每个业务模块下的标准分层：
```
com.zju.lease.{module}/
├── controller/{domain}/
├── service/ + impl/
├── mapper/  (MyBatis-Plus 接口)
├── vo/{domain}/
├── config/
└── listener/  (RabbitMQ 消费者)
```

### 数据库
- 所有实体继承 `BaseEntity`（id, createTime, updateTime, isDeleted）
- 逻辑删除：`is_deleted = 0` 为有效，`is_deleted = 1` 为已删除

### Redis 使用
- 缓存房间详情：`app:room:{id}`
- 登录/验证码：`app:login:{phone}`, `admin:login:{key}`
- 聊天在线用户：`chat:online_users`
- Pub/Sub 频道：`chat_msg_channel`, `chat_sys_channel`

### RabbitMQ 消息队列
| 交换机 | 队列 | 路由键 | 用途 |
|--------|------|--------|------|
| lease.room | lease.room.reindex | room.updated | 房间变更→Agent重索引 |
| lease.chat | lease.chat.persist | chat.message | 聊天消息持久化 |
| lease.history | lease.history.record | history.record | 浏览历史记录 |
| lease.lease | lease.lease.expired | lease.expired | 租约到期通知 |

所有监听器使用 `ackMode = "MANUAL"`，处理成功后调用 `channel.basicAck`，异常时 `basicNack(requeue=true)`。

### Redisson 分布式锁
| 锁 Key | 场景 | 说明 |
|--------|------|------|
| `lock:lease:room:{roomId}` | 租约签约 | 时间段重叠检测，不冲突允许多份租约 |
| `lock:conv:{minId}-{maxId}` | 聊天会话 | TOCTOU 双重检查防重复 |
| `lock:task:checkLeaseStatus` | 定时任务 | tryLock(0) 抢不到即跳过 |

### 启动顺序
Nacos → MySQL/Redis Stack/RabbitMQ/MinIO → web-app → chat-service → agent-service → gateway

### API 文档
统一入口：http://localhost:8083/doc.html (Knife4j)

### 环境变量
- `MINIMAX_API_KEY` — MiniMax API 密钥
- `DASHSCOPE_API_KEY` — 阿里 DashScope 密钥

## 新增模块流程
1. 根 pom.xml 添加 `<module>` 和依赖管理
2. 创建模块 pom.xml（继承父项目）
3. 创建 Application 主类，添加 `@EnableDiscoveryClient` 等注解
4. 创建 application.yml（端口、Nacos、数据库、Redis 等配置）
5. 如果需要 MyBatis-Plus，创建 mapper 包并配置 `@MapperScan`
6. 如果需要公共模块（Redis、RabbitMQ 等），在 `@Import` 和 `@ComponentScan` 中添加
7. 在 gateway 添加路由和 Knife4j 文档聚合
