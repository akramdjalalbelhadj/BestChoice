.PHONY: start stop build clean fresh logs

# Variables
DC = docker-compose

# Lancer l'app
start:
	./mvnw clean package -DskipTests
	$(DC) up -d
	@echo "🚀 http://localhost (Front) | http://localhost:8081 (Back)"

# Arrêter proprement
stop:
	$(DC) down

# Reconstruction totale sans cache
fresh:
	$(DC) down -v --rmi all
	./mvnw clean package -DskipTests
	$(DC) build --no-cache
	$(DC) up -d

# Voir uniquement ce qui compte
logs:
	$(DC) logs -f

# Nettoyer l'espace disque Docker
clean:
	$(DC) down
	docker system prune -f