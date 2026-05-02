# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**CloudPic-AI** - 智能协同云图库平台 (Enterprise-level Image Management System)

A full-stack image management platform with four main features:
1. Public image library (表情包/壁纸 site)
2. Admin panel for image review and management
3. Personal cloud storage for individual users
4. Team collaboration with real-time image editing

## Project Structure

```
yu-picture/
├── yu-picture-backend/       # Spring Boot 后端 (经典架构)
└── yu-picture-frontend/      # Vue 3 前端
```

Note: A DDD (Domain-Driven Design) version of the backend is mentioned in the README but does not currently exist as a separate directory.

## Tech Stack

### Backend (yu-picture-backend)
- **Framework**: Spring Boot 2.7.6 + Java 17
- **Database**: MySQL + MyBatis-Plus 3.5.9
- **Cache**: Redis + Caffeine (multi-level caching)
- **Auth**: Sa-Token + Redis Session
- **Sharding**: ShardingSphere (dynamic table sharding by spaceId)
- **Real-time**: WebSocket + Disruptor (high-performance lock-free queue)
- **Storage**: Tencent Cloud COS (对象存储)
- **AI**: Aliyun AI Image Synthesis
- **Tools**: Hutool, Knife4j (API docs)

### Frontend (yu-picture-frontend)
- **Framework**: Vue 3.5 + Vite 6
- **UI**: Ant Design Vue 4.2.6
- **State**: Pinia 2.2
- **HTTP**: Axios
- **Charts**: ECharts 5.5 + vue-echarts
- **TypeScript**: Strict type checking

## Build & Run Commands

### Backend
```bash
cd yu-picture-backend

# Run
mvn spring-boot:run

# Run tests
mvn test

# Run single test class
mvn test -Dtest=PictureServiceImplTest

# Run single test method
mvn test -Dtest=PictureServiceImplTest#testUploadPicture
```

**Main Class**: `com.yupi.yupicturebackend.YuPictureBackendApplication`
**API Docs**: `http://localhost:8123/api/doc.html`

### Frontend
```bash
cd yu-picture-frontend

# Install dependencies
npm install

# Development server (port 5173)
npm run dev

# Production build
npm run build

# Type check
npm run type-check

# Lint & format
npm run lint
npm run format

# Generate API client from OpenAPI
npm run openapi
```

## Backend Architecture

### Layered Architecture
```
Controller → Service → Manager → Mapper
                ↓
           External API (COS, AI)
```

### Key Packages
| Package | Purpose |
|---------|---------|
| `controller/` | REST API endpoints |
| `service/impl/` | Business logic |
| `manager/` | External integrations (COS, WebSocket, Sharding) |
| `mapper/` | MyBatis-Plus database operations |
| `model/dto/` | Request DTOs |
| `model/vo/` | Response VOs (脱敏) |
| `model/entity/` | Database entities |
| `model/enums/` | Enumerations |
| `config/` | Spring configurations |
| `exception/` | Global exception handling |
| `aop/` | Aspect-oriented programming |

### Core Design Patterns

1. **Unified Response**: `BaseResponse<T>` wraps all API responses with code/data/message
2. **VO Pattern**: Never return entities directly; use VO for 脱敏 (e.g., `User` → `UserVO`/`LoginUserVO`)
3. **Template Method**: `PictureUploadTemplate` for extensible upload strategies (file/URL/batch)
4. **Multi-level Cache**: Caffeine (local) + Redis (distributed)
5. **Space-based Sharding**: Pictures sharded by `spaceId` via ShardingSphere

## Key Domain Concepts

### Space (空间)
- Isolated storage space for users (private) or teams
- `Picture.spaceId = null` → public gallery
- `Picture.spaceId != null` → private space
- Space levels (`SpaceLevelEnum`) define maxSize/maxCount limits

### Picture (图片)
- Image metadata stored in MySQL (URL, size, dimensions, tags, etc.)
- Actual file stored in Tencent COS
- Review workflow: `reviewStatus` (待审核/通过/拒绝)

### Permission Model
- **User roles**: `UserRoleEnum` (USER/VIP/ADMIN)
- **Space permissions**: `@SaSpaceCheckPermission` for space-level auth
- **SpaceUser**: User permissions within a space (OWNER/ADMIN/VIEWER)

## Configuration

### Required Services
- MySQL 5.7+ (database: `yu_picture`)
- Redis (default: 127.0.0.1:6379)
- Tencent Cloud COS account
- Aliyun AI API key (optional, for AI features)

### Backend (application.yml)
```yaml
server:
  port: 8123
  servlet:
    context-path: /api

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/yu_picture
  redis:
    host: 127.0.0.1
    port: 6379
```

### Frontend
- Backend URL configured in `src/request.ts`
- Default: `http://localhost:8123`

## Frontend Code Style

Based on `.prettierrc.json`:
- No semicolons (`semi: false`)
- Single quotes (`singleQuote: true`)
- Max line width: 100 chars

## Important Files

| Path | Purpose |
|------|---------|
| `YuPictureBackendApplication.java` | Main entry point |
| `CosManager.java` | COS file storage operations |
| `PictureShardingAlgorithm.java` | Dynamic table sharding logic |
| `PictureEditHandler.java` | WebSocket real-time editing handler |
| `SpaceUserAuthManager.java` | Space permission authentication |
| `PictureUploadTemplate.java` | Upload template pattern |
| `GlobalExceptionHandler.java` | Unified exception handling |
