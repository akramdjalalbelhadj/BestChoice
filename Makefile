.PHONY: help start stop restart rebuild logs logs-backend logs-frontend logs-mysql ps clean clean-all shell-backend shell-mysql health build push

# Variables
DOCKER_COMPOSE := docker-compose
DOCKER := docker
REGISTRY := your-registry
IMAGE_PREFIX := bestchoice

help:
	@echo "╔═══════════════════════════════════════╗"
	@echo "║     BestChoice Docker Commands        ║"
	@echo "╚═══════════════════════════════════════╝"
	@echo ""
	@echo "Commandes disponibles:"
	@echo "  make start              - Démarrer l'application"
	@echo "  make stop               - Arrêter l'application"
	@echo "  make restart            - Redémarrer l'application"
	@echo "  make rebuild            - Reconstruire les images"
	@echo "  make logs               - Afficher tous les logs"
	@echo "  make logs-backend       - Logs du backend"
	@echo "  make logs-frontend      - Logs du frontend"
	@echo "  make logs-mysql         - Logs de MySQL"
	@echo "  make ps                 - État des services"
	@echo "  make clean              - Nettoyer l'application"
	@echo "  make clean-all          - Nettoyer complètement"
	@echo "  make shell-backend      - Shell dans le backend"
	@echo "  make shell-mysql        - MySQL CLI"
	@echo "  make health             - Vérifier la santé"
	@echo "  make build              - Builder les images"
	@echo "  make push               - Pusher les images"
	@echo ""

start:
	$(DOCKER_COMPOSE) up -d
	@echo "✓ Application démarrée!"
	@echo ""
	@echo "URLs:"
	@echo "  Frontend: http://localhost"
	@echo "  Backend:  http://localhost:8080"
	@echo "  MySQL:    localhost:3306"

stop:
	$(DOCKER_COMPOSE) down
	@echo "✓ Application arrêtée!"

restart: stop start

rebuild:
	$(DOCKER_COMPOSE) build
	@echo "✓ Images reconstruites!"

logs:
	$(DOCKER_COMPOSE) logs -f

logs-backend:
	$(DOCKER_COMPOSE) logs -f backend

logs-frontend:
	$(DOCKER_COMPOSE) logs -f frontend

logs-mysql:
	$(DOCKER_COMPOSE) logs -f mysql

ps:
	$(DOCKER_COMPOSE) ps

clean:
	$(DOCKER_COMPOSE) down
	@echo "✓ Nettoyage terminé!"

clean-all:
	$(DOCKER_COMPOSE) down -v --rmi all
	@echo "✓ Tout supprimé!"

shell-backend:
	$(DOCKER_COMPOSE) exec backend bash

shell-mysql:
	$(DOCKER_COMPOSE) exec mysql mysql -u root -proot bestchoice

health:
	@echo "Vérification de la santé des services..."
	@$(DOCKER_COMPOSE) ps | grep -E "backend|frontend|mysql"

build:
	$(DOCKER_COMPOSE) build --no-cache

build-backend:
	$(DOCKER) build -f Dockerfile.backend -t $(IMAGE_PREFIX)-backend:latest .

build-frontend:
	$(DOCKER) build -f Dockerfile.frontend -t $(IMAGE_PREFIX)-frontend:latest .

push:
	$(DOCKER) tag $(IMAGE_PREFIX)-backend:latest $(REGISTRY)/$(IMAGE_PREFIX)-backend:latest
	$(DOCKER) tag $(IMAGE_PREFIX)-frontend:latest $(REGISTRY)/$(IMAGE_PREFIX)-frontend:latest
	$(DOCKER) push $(REGISTRY)/$(IMAGE_PREFIX)-backend:latest
	$(DOCKER) push $(REGISTRY)/$(IMAGE_PREFIX)-frontend:latest
	@echo "✓ Images pushées!"

# Dev commands
dev-logs:
	$(DOCKER_COMPOSE) logs -f --tail=100

dev-reload:
	$(DOCKER_COMPOSE) restart backend frontend

dev-fresh: clean-all start
	@echo "✓ Installation fraîche complète!"

# CI/CD helpers
validate-compose:
	$(DOCKER_COMPOSE) config

pull-latest:
	$(DOCKER) pull mysql:8.0
	$(DOCKER) pull nginx:alpine
	$(DOCKER) pull node:20-alpine
	$(DOCKER) pull eclipse-temurin:21-jre-alpine
	$(DOCKER) pull maven:3.9-eclipse-temurin-21

prune:
	$(DOCKER) system prune -f
	$(DOCKER) volume prune -f
	@echo "✓ Espace disque libéré!"

# Afficher la version
version:
	@echo "Docker version:" && $(DOCKER) --version
	@echo "Docker Compose version:" && $(DOCKER_COMPOSE) --version

# Stats en temps réel
stats:
	$(DOCKER) stats

# Afficher les tailles des images
sizes:
	$(DOCKER) images | grep bestchoice

