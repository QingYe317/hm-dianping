# hm-dianping（黑马点评）

一个用于系统学习 Redis 实战的本地生活类点评项目后端：商铺查询、优惠券秒杀、达人探店、好友关注、附近商铺等业务场景，逐步用 Redis 解决登录态、缓存、并发、地理位置等问题。

> 学习项目，跟随《黑马程序员 Redis 实战》课程逐步实现，非完整商业项目。

![Java](https://img.shields.io/badge/Java-8-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.3.12-brightgreen)
![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.4.3-orange)
![MySQL](https://img.shields.io/badge/MySQL-5.7-4479A1)
![Redis](https://img.shields.io/badge/Redis-6.x-DC382D)

## 技术栈

| 分类 | 技术 |
| --- | --- |
| 语言 / 构建 | Java 8、Maven |
| 框架 | Spring Boot 2.3.12、Spring MVC |
| 持久层 | MyBatis-Plus 3.4.3、MySQL 5.1.47 驱动 |
| 缓存 / 中间件 | Redis（Lettuce 连接池） |
| 工具库 | Hutool 5.7.17、Lombok |

## 功能进度

登录模块
- [x] 短信验证码发送与校验（正则校验手机号）
- [x] 基于 `HttpSession` 的登录 / 注册（新用户自动创建）
- [x] 登录拦截器（Spring MVC `HandlerInterceptor`）+ `ThreadLocal` 保存当前用户
- [ ] 基于 Redis 的登录态（token 替代 Session，双拦截器刷新有效期）
- [ ] 登出

商铺模块
- [x] 商铺详情、新增、更新
- [x] 按类型 / 名称分页查询
- [x] 商铺类型列表
- [ ] 商铺信息缓存（含空值缓存防穿透）
- [ ] 缓存击穿方案（互斥锁 / 逻辑过期）

优惠券秒杀
- [x] 优惠券查询（普通券 / 秒杀券）
- [ ] 全局唯一 ID 生成（Redis）
- [ ] 秒杀下单、库存超卖问题（乐观锁）
- [ ] 一人一单限制
- [ ] Lua 脚本预扣库存 + 异步下单

达人探店 / 社交
- [ ] 发布、查看探店笔记
- [ ] 笔记点赞（Redis Set）
- [ ] 点赞排行榜（SortedSet）
- [ ] 关注 / 取关、共同关注
- [ ] 关注推送 Feed 流（滚动分页）

附近商铺与统计
- [ ] GEO 实现附近商铺
- [ ] 用户签到（BitMap）
- [ ] UV 统计（HyperLogLog）

## 项目结构

```
hm-dianping
├── src/main/java/com/hmdp
│   ├── config        # MVC、MyBatis-Plus、全局异常处理
│   ├── controller    # 接口层：shop / user / voucher / blog / follow ...
│   ├── service       # 业务接口与实现（impl）
│   ├── mapper        # MyBatis-Plus Mapper
│   ├── entity        # 数据库实体
│   ├── dto           # 统一返回结果 Result、登录表单、滚动分页等
│   └── utils         # 拦截器、ThreadLocal、Redis 常量、正则、密码工具
└── src/main/resources
    ├── application.yaml   # 数据源、Redis、MyBatis-Plus 配置
    ├── db/hmdp.sql        # 建库建表 + 初始化数据
    └── mapper/*.xml       # 自定义 SQL
```

## 快速开始

### 1. 环境要求

- JDK 8
- Maven 3.6+
- MySQL 5.7+
- Redis 5.0+（默认连接 `192.168.100.128:6379`，按需修改）

### 2. 初始化数据库

```bash
mysql -uroot -p < src/main/resources/db/hmdp.sql
```

脚本会自动创建 `hmdp` 库、所有表结构与初始数据。

### 3. 修改配置

编辑 `src/main/resources/application.yaml`，改成你自己的环境：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/hmdp?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
    username: root
    password: 你的密码
  redis:
    host: 127.0.0.1     # 默认指向虚拟机地址，需改成实际地址
    port: 6379
    password: 你的密码
```

### 4. 启动

运行 `com.hmdp.HmDianPingApplication`，服务端口 **8081**。

### 5. 前端页面（可选）

本仓库只包含后端代码，前端的 Nginx 静态资源不在此仓库内。需要页面联调时，把课程提供的 `nginx-1.18.0` 前端工程放到本地运行，并将接口代理到 `http://localhost:8081`。

## 主要接口

| 方法 | 路径 | 说明 | 状态 |
| --- | --- | --- | --- |
| POST | `/user/code` | 发送手机验证码 | ✅ |
| POST | `/user/login` | 验证码登录 | ✅ |
| GET | `/user/me` | 获取当前登录用户 | ⚠️ 依赖拦截器写入 ThreadLocal，待完善 |
| POST | `/user/logout` | 登出 | ❌ 未实现 |
| GET | `/shop/{id}` | 商铺详情 | ✅ |
| POST | `/shop` | 新增商铺 | ✅ |
| PUT | `/shop` | 更新商铺 | ✅ |
| GET | `/shop/of/type` | 按类型分页查商铺 | ✅ |
| GET | `/shop/of/name` | 按名称搜索商铺 | ✅ |
| GET | `/shop-type/list` | 商铺类型列表 | ✅ |
| GET | `/voucher/list/{shopId}` | 商铺优惠券列表 | ✅ |
| POST | `/voucher/seckill` | 新增秒杀券 | ✅ |
| POST | `/voucher-order/seckill/{id}` | 秒杀下单 | ❌ 未实现 |

统一返回结构：`{ "success": true, "errorMsg": null, "data": ..., "total": null }`

## 说明

- 代码基于《黑马程序员 Redis 实战》课程提供的初始工程，业务代码随学习进度逐步补充。
- 提交记录按课程章节推进，便于回溯每一阶段的实现。
- `application.yaml` 中的数据库与 Redis 账号密码为本地 / 虚拟机测试用配置，仅用于学习环境。
- 本项目仅供学习交流，如有侵权请联系删除。
