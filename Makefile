# Makefile — convenience wrappers around docker compose
# Usage: make <target>

.DEFAULT_GOAL := help

COMPOSE = docker compose

.PHONY: help up down reset logs jenkins-logs playwright-logs test open-report

help: ## Show this help message
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
	  awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'

up: ## Start the full CI environment (build if needed)
	$(COMPOSE) up -d --build
	@echo ""
	@echo "  Jenkins → http://localhost:8080  (admin / admin)"
	@echo "  Waiting for Jenkins to be ready…"
	@until curl -s http://localhost:8080/login > /dev/null; do sleep 2; done
	@echo "  ✅ Jenkins is up!"

down: ## Stop all containers (keeps volumes)
	$(COMPOSE) down

reset: ## Destroy everything including volumes and rebuild from scratch
	$(COMPOSE) down -v --remove-orphans
	$(COMPOSE) up -d --build

logs: ## Tail logs for all services
	$(COMPOSE) logs -f

jenkins-logs: ## Tail Jenkins logs only
	$(COMPOSE) logs -f jenkins

playwright-logs: ## Tail Playwright container logs only
	$(COMPOSE) logs -f playwright

test: ## Run Playwright smoke tests locally (outside Jenkins)
	$(COMPOSE) exec playwright npx playwright test

open-report: ## Open the Playwright HTML report in your browser
	$(COMPOSE) exec playwright npx playwright show-report
