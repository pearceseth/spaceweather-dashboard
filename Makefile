.PHONY: bootstrap dev test lint format clean build \
        backend-dev backend-test backend-build \
        frontend-dev frontend-test frontend-build \
        codegen docker-up docker-down

# =============================================================================
# Main targets
# =============================================================================

bootstrap:
	@echo "Bootstrapping Solarion monorepo..."
	./scripts/bootstrap.sh

dev: docker-up
	@echo "Starting development servers..."
	$(MAKE) -j2 backend-dev frontend-dev

test: backend-test frontend-test
	@echo "All tests passed!"

lint:
	./scripts/lint.sh

format:
	./scripts/format.sh

clean:
	cd backend && sbt clean
	cd frontend && rm -rf node_modules dist .next

build: backend-build frontend-build

# =============================================================================
# Backend targets
# =============================================================================

backend-dev:
	cd backend && sbt ~run

backend-test:
	cd backend && sbt test

backend-build:
	cd backend && sbt assembly

# =============================================================================
# Frontend targets
# =============================================================================

frontend-dev:
	cd frontend && pnpm dev

frontend-test:
	cd frontend && pnpm test

frontend-build:
	cd frontend && pnpm build

# =============================================================================
# API / Codegen
# =============================================================================

codegen:
	cd api && ./codegen.sh

# =============================================================================
# Docker / Infrastructure
# =============================================================================

docker-up:
	cd ops && docker compose up -d

docker-down:
	cd ops && docker compose down

# =============================================================================
# Help
# =============================================================================

help:
	@echo "Solarion Makefile targets:"
	@echo ""
	@echo "  bootstrap     - Initial project setup"
	@echo "  dev           - Start all development servers"
	@echo "  test          - Run all tests"
	@echo "  lint          - Lint all code"
	@echo "  format        - Format all code"
	@echo "  clean         - Clean build artifacts"
	@echo "  build         - Build all projects"
	@echo ""
	@echo "  backend-dev   - Start backend dev server"
	@echo "  backend-test  - Run backend tests"
	@echo "  backend-build - Build backend"
	@echo ""
	@echo "  frontend-dev  - Start frontend dev server"
	@echo "  frontend-test - Run frontend tests"
	@echo "  frontend-build- Build frontend"
	@echo ""
	@echo "  codegen       - Generate API clients from OpenAPI"
	@echo "  docker-up     - Start Docker services"
	@echo "  docker-down   - Stop Docker services"
