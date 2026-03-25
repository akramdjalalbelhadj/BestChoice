# 📦 Structure Docker du Projet

## 📋 Fichiers créés

```
BestChoice/
├── 📄 docker-compose.yml          ← Configuration principale (services)
├── 🐳 Dockerfile.backend          ← Image du backend Spring Boot
├── 🐳 Dockerfile.frontend         ← Image du frontend Angular + Nginx
├── ⚙️  nginx.conf                 ← Configuration web Nginx
├── 🛡️  .dockerignore              ← Fichiers ignorés par Docker
├── 📜 docker-manage.sh            ← Script shell (macOS/Linux)
├── 🪟 docker-manage.bat           ← Script batch (Windows)
├── 📖 Makefile                    ← Commandes Make (optionnel)
├── 📘 DOCKER.md                   ← Documentation complète
├── ⚡ QUICKSTART.md               ← Démarrage rapide
│
├── .env.example                   ← Variables d'environnement exemple
└── src/main/resources/
    └── application-docker.properties ← Config Spring Boot pour Docker
```

---

## 🏗️ Architecture Docker

```
┌─────────────────────────────────────────────────────┐
│                  DOCKER COMPOSE                      │
├─────────────────────────────────────────────────────┤
│                                                      │
│  ┌──────────────────────────────────────────────┐  │
│  │  Frontend Service (Nginx + Angular)          │  │
│  │  - Port: 80                                  │  │
│  │  - Image: node:20 → nginx:alpine             │  │
│  │  - Proxy /api/* vers backend                 │  │
│  └──────────────────────────────────────────────┘  │
│                        ↓                             │
│  ┌──────────────────────────────────────────────┐  │
│  │  Backend Service (Spring Boot)               │  │
│  │  - Port: 8080                                │  │
│  │  - Image: maven → eclipse-temurin:21         │  │
│  │  - Health check: /actuator/health            │  │
│  └──────────────────────────────────────────────┘  │
│                        ↓                             │
│  ┌──────────────────────────────────────────────┐  │
│  │  MySQL Database                              │  │
│  │  - Port: 3306                                │  │
│  │  - Image: mysql:8.0                          │  │
│  │  - Volume: mysql-data (persistant)           │  │
│  └──────────────────────────────────────────────┘  │
│                                                      │
└─────────────────────────────────────────────────────┘
```

---

## 🚀 Démarrage rapide

### Commande unique pour tout lancer:
```bash
docker-compose up -d
```

### Vérifier que tout fonctionne:
```bash
docker-compose ps
```

### Voir les logs:
```bash
docker-compose logs -f
```

### Arrêter:
```bash
docker-compose down
```

---

## 📊 Services et Ports

| Service | Port | URL | Container |
|---------|------|-----|-----------|
| **Frontend** | 80 | http://localhost | bestchoice-frontend |
| **Backend** | 8080 | http://localhost:8080 | bestchoice-backend |
| **MySQL** | 3306 | localhost:3306 | bestchoice-mysql |

---

## 🔄 Flux de communication

```
Client (Browser)
     ↓
http://localhost (Nginx)
     ├─→ Fichiers statiques (Angular app)
     └─→ /api/* → http://backend:8080 (Proxy)
          ↓
       Backend (Spring Boot)
          ├─→ Business Logic
          └─→ MySQL (jdbc:mysql://mysql:3306/bestchoice)
```

---

## 🛠️ Scripts de gestion

### Utiliser le script shell (macOS/Linux):
```bash
./docker-manage.sh start              # Démarrer
./docker-manage.sh stop               # Arrêter
./docker-manage.sh restart            # Redémarrer
./docker-manage.sh logs               # Voir les logs
./docker-manage.sh logs:backend       # Logs du backend
./docker-manage.sh ps                 # État
./docker-manage.sh health             # Health check
./docker-manage.sh clean              # Nettoyer
./docker-manage.sh clean:all          # Nettoyer tout
./docker-manage.sh shell:backend      # Bash dans backend
./docker-manage.sh shell:mysql        # MySQL CLI
```

### Utiliser Make (si disponible):
```bash
make start              # Démarrer
make stop               # Arrêter
make logs               # Logs
make health             # Health check
make clean              # Nettoyer
make help               # Aide
```

### Utiliser le script batch (Windows):
```cmd
docker-manage.bat start
docker-manage.bat logs
docker-manage.bat stop
```

---

## 📝 Build Multi-stage

### Backend:
```dockerfile
Stage 1: maven:3.9-eclipse-temurin-21
  → Télécharge les dépendances
  → Compile le code source
  → Crée un JAR

Stage 2: eclipse-temurin:21-jre-alpine
  → Image légère avec JRE
  → Copie le JAR
  → Lance l'app
```

### Frontend:
```dockerfile
Stage 1: node:20-alpine
  → Installe les dépendances npm
  → Compile l'app Angular
  → Crée dist/

Stage 2: nginx:alpine
  → Image web légère
  → Copie les fichiers compilés
  → Configure le routing
```

---

## 🔐 Sécurité

### Variables d'environnement:
```
❌ Ne committez pas les credentials!
✅ Utilisez .env (ignoré par git)
✅ Utilisez les secrets Docker en production
```

### Base de données:
```
❌ Mot de passe "root" en dev uniquement!
✅ En production: utilisez des secrets forts
✅ Volume MySQL persiste les données
```

### CORS et Nginx:
```
✅ Proxy /api/* configuré dans nginx.conf
✅ Headers X-Forwarded-* correctement passés
✅ HTTPS/SSL à ajouter en production
```

---

## 📈 Health Checks

Chaque service a des vérifications de santé:

```yaml
Frontend:
  - Test: GET http://localhost/
  - Interval: 30s
  - Timeout: 10s
  
Backend:
  - Test: GET http://localhost:8080/actuator/health
  - Interval: 30s
  - Timeout: 10s
  
MySQL:
  - Test: mysqladmin ping
  - Interval: 10s
  - Timeout: 5s
```

---

## 🐛 Dépannage courant

| Problème | Solution |
|----------|----------|
| Port déjà utilisé | Modifiez `ports:` dans docker-compose.yml |
| Base de données ne démarre pas | `docker-compose logs mysql` |
| Frontend ne voit pas le backend | Vérifiez nginx.conf et `proxy_pass` |
| Images trop lourdes | Utilisez `.dockerignore` |
| Permissions refusées sur scripts | `chmod +x docker-manage.sh` |

---

## 📚 Documentation

- **DOCKER.md** - Documentation complète
- **QUICKSTART.md** - Démarrage rapide  
- **Makefile** - Commandes de gestion
- **docker-compose.yml** - Configuration

---

## 🎯 Prochaines étapes

1. ✅ Vérifiez que Docker est installé
2. ✅ Lancez `docker-compose up -d`
3. ✅ Ouvrez http://localhost
4. ✅ Consultez les logs avec `docker-compose logs -f`

**C'est prêt!** 🚀

---

## 📞 Besoin d'aide?

```bash
# Aide du script
./docker-manage.sh help

# Aide Make
make help

# Documentation complète
cat DOCKER.md
```

