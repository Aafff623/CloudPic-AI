# 智能协同云图库

> 作者：threetwoa

基于 Vue 3 + Spring Boot + COS + WebSocket 的 **企业级智能协同云图库平台**。

## 项目介绍

平台核心功能分为 4 大类：

1. **公共图库**：所有用户都可以在平台公开上传和检索图片素材，可用作表情包网站、设计素材网站、壁纸网站等
2. **后台管理**：管理员可以上传、审核和管理图片，并对系统内的图片进行分析
3. **个人云存储**：个人用户可将图片上传至私有空间进行批量管理、检索、编辑和分析，可用作个人网盘、个人相册、作品集等
4. **团队协作**：企业可开通团队空间并邀请成员，共享图片并 **实时协同编辑图片**，可用于企业活动相册、企业内部素材库等

## 技术选型

### 后端

- Java Spring Boot 框架
- MySQL 数据库 + MyBatis-Plus 框架
- Redis 分布式缓存 + Caffeine 本地缓存
- COS 对象存储
- ShardingSphere 分库分表
- Sa-Token 权限控制
- DDD 领域驱动设计
- WebSocket 双向通信
- Disruptor 高性能无锁队列
- AI 绘图大模型接入

### 前端

- Vue 3 框架
- Vite 打包工具
- Ant Design Vue 组件库
- Pinia 全局状态管理
- TypeScript + ESLint + Prettier

## 项目结构

```
yu-picture/
├── yu-picture-backend/       # Spring Boot 后端 (经典架构)
├── yu-picture-backend-ddd/   # Spring Boot 后端 (DDD架构)
└── yu-picture-frontend/      # Vue 3 前端
```

## 快速开始

### 后端

```bash
cd yu-picture-backend
mvn spring-boot:run
```

### 前端

```bash
cd yu-picture-frontend
npm install
npm run dev
```

## 许可证

MIT License
