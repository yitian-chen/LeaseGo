# LeaseGo | 公寓租赁管理系统

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0.5-green.svg)](https://spring.io/projects/spring-boot)
[![MyBatis Plus](https://img.shields.io/badge/MyBatis%20Plus-3.5.3.1-blue.svg)](https://baomidou.com/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

## 项目简介
LeaseGo 是一款现代化的全栈公寓租赁管理系统，基于 **Spring Boot 3** 和 **Java 17** 开发。项目采用前后端分离架构，分为**后台管理系统 (web-admin)** 和 **移动端应用 (web-app)**。管理端实现了房源全生命周期管理、配套设施动态配置及租约审批；移动端则为用户提供便捷的找房、预约看房及合同管理服务。

## 系统架构
项目采用 Maven 多模块化开发，确保代码的高内聚、低耦合与易扩展性：
- `common`: 核心公共模块。封装了全局异常处理、统一结果返回 (`Result`)、Redis 自定义序列化 及 Minio 工具类。
- `model`: 数据模型模块。包含所有数据库实体类 (Entity)、业务视图对象 (VO) 以及业务枚举类(Enum)。
- `web-admin`: 后台管理端后端接口。负责公寓、房间、配套、标签、租约及系统用户与移动端用户的权限管理。
- `web-app`: 用户移动端后端接口。支持手机验证码登录、房源检索、浏览记录、预约看房及个人租约查询。

## 技术栈
- **核心框架:** Java 17, Spring Boot 3.0.5
- **持久层:** MyBatis-Plus 3.5.3.1 + MySQL 8.0
- **中间件:** - **Redis:** 用于验证码存储、热门房源详情缓存
    - **Minio:** 分布式对象存储，统一管理房源图片
- **安全与验证:** - **认证:** JWT (JSON Web Token) 实现无状态登录
    - **验证:** EasyCaptcha (图形验证码) + 阿里云 SMS 服务(短信验证码)
- **文档工具:** Knife4j 4.1.0 (基于 OpenAPI 3)
- **压力测试工具** Apifox 2.8.20

## 核心功能
- **房源管理:** 支持公寓与房间的联动管理，包含配套、杂费及标签的动态增删改查、图片上传与地址查找(调用高德地图接口)。
- **登录鉴权:**
    - 管理端：用户名 + 密码 + 图形验证码校验。
    - 移动端：手机号 + 短信验证码快捷登录，自动注册。
- **租赁全流程:** 涵盖房源搜索（省市区/价格/支付方式多维过滤）、预约看房提交、租约签订及状态自动流转。
- **系统工具:** 实现图片上传至 Minio 存储桶、基于 Spring Task 的租约到期自动结束。

## 项目亮点（简历重点）
1. **高性能缓存架构:** 在移动端房源详情接口引入 **Redis 缓存策略**。通过“先查缓存，穿透查库”的逻辑减少数据库 IO 压力，并配合管理端修改时的失效机制确保数据一致性。
2. **异步处理:** 使用 Spring **`@Async` 异步线程池**处理用户房源浏览历史记录，避免繁重的插入操作阻塞主线程请求，提升系统响应速度。
3. **健壮的权限体系:** 封装 **`ThreadLocal` 上下文持有者 (`LoginUserHolder`)**，配合自定义拦截器实现用户信息的无感传递，有效隔离各线程间的用户信息。
4. **统一工程化规范:** 建立**全局异常拦截器 (`GlobalExceptionHandler`)**，统一捕获业务异常并返回标准 Result 格式。
5. **类型安全与转换:** 针对业务中大量的状态枚举，自定义 **`StringToBaseEnumConverterFactory`**，实现了 Web 层请求参数到数据库枚举类的自动映射，增强了代码的可维护性。

## 快速开始
### 1. 环境准备
- JDK 17
- MySQL 8.0+
- Redis 6.2+
- Minio

### 2. 配置文件说明
项目配置文件均提供了模板。在启动前，请将各模块 `resources` 下的 `application.yml.example` 重命名为 `application.yml`，并配置：
- 数据库连接信息
- Redis 地址
- Minio Endpoint/AccessKey/SecretKey
- 阿里云短信验证服务 key

### 3. 编译启动
```bash
# 根目录下执行打包
mvn clean install

# 启动管理端 (端口 8080)
java -jar web/web-admin/target/web-admin-1.0-SNAPSHOT.jar

# 启动移动端接口 (端口 8081)
java -jar web/web-app/target/web-app-1.0-SNAPSHOT.jar