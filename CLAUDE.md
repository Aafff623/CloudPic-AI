# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**yu-picture** - 智能协同云图库平台 (Enterprise-level Image Management System)

A full-stack image management platform with four main features:
1. Public image library (like表情包/壁纸 site)
2. Admin panel for image review and management
3. Personal cloud storage for individual users
4. Team collaboration with real-time image editing

## Project Structure

```
yu-picture/
├── yu-picture-backend/       # Spring Boot 后端 (经典架构)
├── yu-picture-backend-ddd/   # Spring Boot 后端 (DDD架构)
└── yu-picture-frontend/      # Vue 3 前端
```

## Tech Stack

### Backend (yu-picture-backend)
- **Framework**: Spring Boot 2.7.6 + Java 8
- **Database**: MySQL + MyBatis-Plus 3.5.9
- **Cache**: Redis + Caffeine (multi-level caching)
- **Auth**: Sa-Token + Redis
- **Sharding**: ShardingSphere (dynamic table sharding by spaceId)
- **Real-time**: WebSocket + Disruptor (high-performance lock-free queue)
- **Storage**: Tencent Cloud COS (对象存储)
- **AI**: Aliyun AI Image Synthesis
- **Tools**: Hutool, Knife4j (API docs)

### Frontend (yu-picture-frontend)
- **Framework**: Vue 3 + Vite
- **UI**: Ant Design Vue 4.2.6
- **State**: Pinia
- **HTTP**: Axios with interceptors
- **Charts**: ECharts + ECharts WordCloud
- **TypeScript**: Strict type checking

## Key Architecture Patterns

### Backend Architecture
1. **Layered Architecture**: Controller → Service → Manager → Mapper/External API
2. **DDD Variant**: Domain-Driven Design with domain model separation
3. **Permission Model**: RBAC with custom `@SaSpaceCheckPermission` for space-level auth
4. **Real-time Collaboration**: WebSocket + Disruptor for live picture editing
5. **Multi-level Caching**: Redis (distributed) + Caffeine (local)
6. **File Upload**: Template pattern with `PictureUploadTemplate` implementations

### Important Concepts
- **Space**: Isolated storage space (user's private space or team space)
- **Picture**: Image entity with metadata, stored in sharded tables
- **SpaceUser**: User permissions within a space (OWNER/ADMIN/VIEWER)

## Build & Run Commands

### Backend
```bash
# Run backend (classic)
cd yu-picture-backend
mvn spring-boot:run

# Run backend (DDD)
cd yu-picture-backend-ddd
mvn spring-boot:run

# Test
mvn test
```

**Main Class**: `com.yupi.yupicturebackend.YuPictureBackendApplication`

### Frontend
```bash
cd yu-picture-frontend

# Development
npm run dev        # Vite dev server at port 5173

# Production build
npm run build

# Code quality
npm run lint       # ESLint
npm run format     # Prettier

# API client generation
npm run openapi    # Generate API client from OpenAPI spec
```

## Configuration Requirements

### Backend (application.yml)
```yaml
server:
  port: 8123
  servlet:
    context-path: /api

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/yu_picture
    username: root
    password: 123456

  redis:
    host: 127.0.0.1
    port: 6379

  shardingsphere:
    # Configures dynamic table sharding by spaceId
    rules:
      sharding:
        tables:
          picture:
            table-strategy:
              standard:
                sharding-column: spaceId
```

### Required Services
- MySQL 5.7+ (database: `yu_picture`)
- Redis (host: `127.0.0.1`, port: `6379`)
- Tencent Cloud COS account (for file storage)
- Aliyun AI API key (for AI image synthesis)

### Frontend Dev Server
- Default: `http://localhost:8123` (backend) configured in `src/request.ts`

## crucial Design Decisions

1. **Table Sharding**: Pictures are sharded by `spaceId` to handle large-scale data
2. **Permission Context**: `SpaceUserAuthContext` manages user permissions within spaces
3. **Real-time Editing**: WebSocket for communication + Disruptor for high-concurrency event processing
4. **Upload Strategy**: Template pattern allows easy addition of new upload methods (file, URL, batch)
5. **Cache Strategy**: Caffeine for hot data, Redis for distributed caching

## API Documentation

Knife4j is enabled at: `http://localhost:8123/api/doc.html`

## Frontend Code Style

Based on `.prettierrc.json`:
- No semicolons (`semi: false`)
- Single quotes (`singleQuote: true`)
- Max line width: 100 chars

## Common Development Tasks

1. **Add new API endpoint**: Create DTO in `model/dto/`, Controller in `controller/`, Service in `service/`
2. **Add new permission check**: Use `@SaSpaceCheckPermission` with appropriate `SpaceUserPermission`
3. **Add new file upload type**: Extend `PictureUploadTemplate` abstract class
4. **Add new space analyze feature**: Create analyzer in `manager/analyze/` and Controller

## Important Files

| Path | Purpose |
|------|---------|
| `YuPictureBackendApplication.java` | Main entry point |
| `CosManager.java` | COS file storage operations |
| `PictureShardingAlgorithm.java` | Dynamic table sharding logic |
| `PictureEditHandler.java` | WebSocket real-time editing handler |
| `SpaceUserAuthManager.java` | Space permission authentication |
| `FilePictureUpload.java` | Local file upload implementation |

## Claude Code AI Collaboration

This project uses **Claude Code** (claude.ai/code) for AI-assisted development. Claude Code acts as your pair programmer, helping with:

### Capabilities

- **Code Generation**: Write new features, endpoints, or components
- **Code Review**: Automatic security, performance, and quality checks
- **Bug Fixing**: Diagnose and resolve issues quickly
- **Refactoring**: Improve code structure while maintaining functionality
- **Testing**: Write and run unit/integration tests using TDD
- **Architecture Planning**: Design system improvements and new features

### AI-Patterns Used

The project leverages several learned architecture patterns stored in `.claude/skills/`:

| Pattern | Description |
|---------|-------------|
| `pattern-high-concurrency-realtime-edit` | WebSocket + Disruptor for real-time collaborative editing |
| `pattern-dynamic-table-sharding` | ShardingSphere runtime table creation for multi-tenancy |
| `pattern-multi-level-authorization` | Sa-Token + space context for hierarchical permissions |
| `pattern-object-storage-pipeline` | COS PicOperations for upload-time image processing |

### Development Workflow

1. **Planning**: Use `/plan` to design new features with risk assessment
2. **TDD**: Use `/tdd` to write tests first, then implement (enforces 80%+ coverage)
3. **Code Review**: Use `/code-review` to verify quality and security
4. **Build Verification**: Use `/verify` to run build and static analysis

### Key Commands

| Command | Purpose |
|---------|---------|
| `/plan` | Create implementation plan with risk assessment |
| `/tdd` | Test-Driven Development: write tests first |
| `/code-review` | Quality and security review of code changes |
| `/build-fix` | Fix build errors or type issues |
| `/e2e` | Generate end-to-end tests with Playwright |
| `/learn` | Extract reusable patterns from sessions |

### Project-Specific Patterns

For architectural decisions, refer to the learned skills in `.claude/skills/`:
- High-concurrency real-time editing with Disruptor
- Dynamic table sharding for space-based isolation
- Multi-level authorization with Sa-Token
- Object storage optimization with COSPicOperations
