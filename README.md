# 基于 Spring Boot 秒杀系统
## 主要技术栈
### 前端
+ 后台 js 框架：jquery
+ 用户端 UI 框架：Bootstrap
### 后端
+ 数据库：MySQL
+ 持久层框架：mybatis
+ 数据库连接池管理：druid
+ mvc 框架：Spring MVC
+ 应用层容器：Spring Boot
+ json 序列化工具：fastjson
+ 服务端页面渲染：thymeleaf
+ 缓存管理：redis
+ 消息队列：rabbitMQ

## 主要问题解决思路

| ID | Problem  | Article | 
| --- | ---   | :--- |
| 000 | 分布式 session 与接口限流 | [解决思路](/docs/solution.md) |
| 001 | redis 键设计 | [解决思路](/docs/solution.md) |
| 002 | 前后端分离 | [解决思路](/docs/solution.md) |
| 003 | 页面与对象缓存 | [解决思路](/docs/solution.md) |
| 004 | 隐藏秒杀地址和图形验证码 | [解决思路](/docs/solution.md) |

## 核心秒杀逻辑

1. 检测用户是否已登录
2. 检测图形验证码是否正确
3. 检测生成的 path uuid 是否正确
4. 本地内存检测秒杀是否已结束
5. redis 预减库存是否成功
6. 数据库中是否有重复订单
7. 将秒杀请求加入消息队列
8. 消费者开始处理秒杀请求，数据库判断是否有库存
9. 再次判断数据库中是否有重复订单
10. 数据库事务操作：减库存，写入订单信息表和秒杀订单表

## 鸣谢
本项目基础思路来自若鱼 1919 老师！老师讲得很不错，答疑也比较及时。课程地址：https://coding.imooc.com/class/168.html

