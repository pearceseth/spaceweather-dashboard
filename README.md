# Solarion

A monorepo containing a space weather dashboard application.

## Structure

```
/backend      - Scala API server
/frontend     - React/TypeScript web application
/api          - OpenAPI schema and generated clients
/ops          - Docker and environment configuration
/scripts      - Development and CI utility scripts
```

## Prerequisites

- JDK 21+
- sbt 1.10+
- Node.js 20+
- pnpm 9+
- Docker & Docker Compose

## Quick Start

```bash
# Bootstrap the entire project
make bootstrap

# Start development environment
make dev

# Run all tests
make test

# Lint and format
make lint
make format
```

## Development

### Backend

```bash
cd backend
sbt compile
sbt run
sbt test
```

### Frontend

```bash
cd frontend
pnpm install
pnpm dev
pnpm test
```

### API Code Generation

```bash
cd api
./codegen.sh
```

## License

Proprietary
