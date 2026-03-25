# 🚀 Quick Start - Docker Compose

## ⚡ Lancer en 3 commandes

### 1. Cloner et aller dans le dossier
```bash
cd /Users/macbook/Documents/Git/Github/BestChoice
```

### 2. Lancer l'application
```bash
docker-compose up -d
```

### 3. Ouvrir dans le navigateur
```
http://localhost
```

**C'est tout!** ✅

---

## 📊 Vérifier le statut

```bash
# Voir les services en cours d'exécution
docker-compose ps

# Voir les logs en temps réel
docker-compose logs -f
```

---

## 🛑 Arrêter l'application

```bash
docker-compose down
```

---

## 🔧 Utiliser les scripts

### Sur macOS / Linux:
```bash
./docker-manage.sh start
./docker-manage.sh logs
./docker-manage.sh restart
./docker-manage.sh stop
```

### Sur Windows:
```cmd
docker-manage.bat start
docker-manage.bat logs
docker-manage.bat restart
docker-manage.bat stop
```

---

## 📚 Utiliser Make (si disponible)

```bash
make start              # Démarrer
make stop               # Arrêter
make restart            # Redémarrer
make logs               # Voir les logs
make logs-backend       # Logs du backend
make clean              # Nettoyer
```

---

## 🌐 URLs

| Service | URL |
|---------|-----|
| Frontend | http://localhost |
| Backend | http://localhost:8080 |
| MySQL | localhost:3306 |

---

## 📁 Fichiers de configuration

| Fichier | Description |
|---------|-------------|
| `docker-compose.yml` | Configuration des services |
| `Dockerfile.backend` | Image du backend |
| `Dockerfile.frontend` | Image du frontend |
| `nginx.conf` | Configuration web |
| `application-docker.properties` | Config Spring Boot |
| `docker-manage.sh` | Script de gestion (Unix) |
| `docker-manage.bat` | Script de gestion (Windows) |
| `Makefile` | Commandes Make |

---

## 🐛 Dépannage rapide

**Les ports sont déjà utilisés?**
```bash
# Modifier dans docker-compose.yml
ports:
  - "8000:80"        # Frontend
  - "8081:8080"      # Backend
  - "3307:3306"      # MySQL
```

**Tout supprimer et recommencer?**
```bash
docker-compose down -v --rmi all
docker-compose up -d
```

**Voir les logs du backend?**
```bash
docker-compose logs -f backend
```

---

## 📖 Documentation complète

Voir `DOCKER.md` pour la documentation détaillée!

---

**Besoin d'aide?** 📞
```bash
# Voir toutes les commandes
./docker-manage.sh help
# ou
make help
```

