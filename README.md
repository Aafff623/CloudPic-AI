# CloudPic-AI - 智能协同云图库

> 作者：threetwoa

基于 Vue 3 + Spring Boot + COS + WebSocket 的 **企业级智能协同云图库平台**。

## 功能特性

| 功能模块 | 说明 |
|---------|------|
| 公共图库 | 用户上传、检索图片素材，可用作表情包/壁纸/设计素材站 |
| 后台管理 | 管理员审核、管理图片，数据统计分析 |
| 个人云存储 | 私有空间批量管理、检索、编辑图片，可用作网盘/相册/作品集 |
| 团队协作 | 团队空间共享图片，**实时协同编辑** |

## 技术栈

### 后端
- Spring Boot 2.7.6 + Java 17
- MySQL + MyBatis-Plus 3.5.9
- Redis + Caffeine (多级缓存)
- Sa-Token (权限控制)
- ShardingSphere (分库分表)
- WebSocket + Disruptor (实时通信)
- 腾讯云 COS (对象存储)
- 阿里云 AI (图像处理)

### 前端
- Vue 3.5 + Vite 6
- Ant Design Vue 4.2
- Pinia + TypeScript
- ECharts (数据可视化)

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

# 配置 application.yml (数据库、Redis、COS)
# 创建数据库: CREATE DATABASE yu_picture;

mvn spring-boot:run
# 访问 API 文档: http://localhost:8123/api/doc.html
```

### 前端

```bash
cd yu-picture-frontend
npm install
npm run dev
# 访问: http://localhost:5173
```

## 核心架构

### 后端分层
```
Controller → Service → Manager → Mapper
                ↓
           External API (COS, AI)
```

### 核心设计
- **统一响应**: `BaseResponse<T>` 封装所有接口响应
- **VO 脱敏**: Entity 不直接返回，通过 VO 过滤敏感字段
- **多级缓存**: Caffeine (本地) + Redis (分布式)
- **空间分表**: 按 `spaceId` 动态分表，支持海量数据
- **模板方法**: `PictureUploadTemplate` 支持多种上传方式扩展

## 许可证

MIT License
