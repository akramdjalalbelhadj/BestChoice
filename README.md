# BestChoice — Système de Matching Étudiant-Projet

Plateforme web full-stack permettant aux enseignants de créer des campagnes d'affectation (projets PFE / matières optionnelles), aux étudiants d'exprimer leurs vœux et à l'algorithme de matching de générer des recommandations optimales.

---

## Table des matières

1. [Présentation](#présentation)
2. [Architecture](#architecture)
3. [Stack technique](#stack-technique)
4. [Prérequis](#prérequis)
5. [Installation & Lancement](#installation--lancement)
6. [Configuration](#configuration)
7. [Rôles & Fonctionnalités](#rôles--fonctionnalités)
8. [API REST](#api-rest)
9. [Algorithmes de matching](#algorithmes-de-matching)
10. [Structure du projet](#structure-du-projet)
11. [Comptes de test](#comptes-de-test)

---

## Présentation

**BestChoice** est une application de gestion des vœux et d'affectation automatique développée dans le cadre d'un projet académique à l'AMU (Aix-Marseille Université). Elle répond au besoin d'organiser et d'optimiser l'affectation des étudiants de Master aux projets ou options proposés par les enseignants.

### Flux principal

```
Enseignant                     Étudiant
    │                              │
    ├─ Crée compétences/mots-clés  │
    ├─ Crée projets/matières       │
    ├─ Crée une campagne           │
    │                              ├─ Complète son profil (skills, intérêts)
    │                              ├─ Consulte les offres
    │                              └─ Soumet ses vœux classés (1→N)
    │
    ├─ Lance le matching (WEIGHTED ou STABLE)
    └─ Consulte les résultats par offre
```

---

## Architecture

```
BestChoice/
├── src/                          # Backend Spring Boot
│   └── main/java/fr/amu/bestchoice/
│       ├── model/                # Entités JPA & Enums
│       ├── repository/           # Spring Data JPA repositories
│       ├── service/              # Logique métier (interfaces + implémentations)
│       │   └── algorithmes/      # Moteurs de matching
│       ├── web/
│       │   ├── controller/       # Contrôleurs REST
│       │   ├── dto/              # DTOs request/response
│       │   ├── mapper/           # MapStruct mappers
│       │   └── exception/        # Gestion globale des erreurs
│       └── security/             # JWT + Spring Security
│
├── bestchoice-front/             # Frontend Angular 21
│   └── src/app/
│       ├── core/                 # Auth store, guards, intercepteur JWT
│       ├── features/
│       │   ├── admin/            # Pages admin
│       │   ├── teacher/          # Pages enseignant
│       │   ├── student/          # Pages étudiant
│       │   ├── campaign/         # Service campagne partagé
│       │   ├── matching/         # Modèles matching partagés
│       │   ├── project/          # Service projet
│       │   └── subject/          # Service matière
│       └── shared/               # Composants partagés (ThemeToggle...)
│
├── Dockerfile.backend
└── docker-compose.yml
```

---

## Stack technique

### Backend

| Technologie | Version | Rôle |
|---|---|---|
| Java | 17 | Langage principal |
| Spring Boot | 3.5.9 | Framework applicatif |
| Spring Security | 6.x | Authentification & autorisation |
| Spring Data JPA | 3.x | Persistance des données |
| Hibernate | 6.x | ORM |
| H2 Database | — | Base de données en mémoire (dev) |
| JWT (jjwt) | 0.12.3 | Tokens d'authentification |
| MapStruct | 1.5.x | Mapping Entity ↔ DTO |
| Lombok | — | Réduction du boilerplate |
| Swagger / OpenAPI | 3 | Documentation API |

### Frontend

| Technologie | Version | Rôle |
|---|---|---|
| Angular | 21.0.0 | Framework SPA |
| TypeScript | 5.9.2 | Langage principal |
| Angular CDK | 21.x | Drag & Drop (classement vœux) |
| RxJS | 7.8.0 | Programmation réactive |
| Angular Signals | — | Gestion d'état réactif |

---

## Prérequis

- **Java 17+** (JDK)
- **Maven 3.8+** (ou utiliser le wrapper `./mvnw`)
- **Node.js 18+** et **npm 10+**
- **Angular CLI 21** : `npm install -g @angular/cli`

---

## Installation & Lancement

### 1. Backend (Spring Boot)

```bash
# Depuis la racine du projet
./mvnw spring-boot:run
```

Le backend démarre sur **http://localhost:8081**

> La base H2 est recréée à chaque démarrage (`ddl-auto=create-drop`).
> La console H2 est accessible sur http://localhost:8081/h2-console
> (JDBC URL: `jdbc:h2:mem:bestchoice`, user: `sa`, pas de mot de passe)

### 2. Frontend (Angular)

```bash
cd bestchoice-front
npm install
npm start
```

Le frontend démarre sur **http://localhost:4200**

### 3. Via Docker Compose (optionnel)

```bash
docker-compose up --build
```

---

## Configuration

Le fichier de configuration principal est `src/main/resources/application.properties` :

```properties
# Serveur
server.port=8081

# Base de données H2
spring.datasource.url=jdbc:h2:mem:bestchoice
spring.jpa.hibernate.ddl-auto=create-drop

# JWT
app.jwt.secret=change-me-in-production-32chars-min
app.jwt.expiration=3600000   # 1 heure

# Swagger
springdoc.swagger-ui.enabled=true
```

> **⚠️ Production** : Remplacer H2 par PostgreSQL/MySQL et changer le secret JWT.

---

## Rôles & Fonctionnalités

### ADMIN

| Fonctionnalité | URL |
|---|---|
| Dashboard statistiques | `/app/admin/dashboard` |
| Gestion des utilisateurs | `/app/admin/users` |
| Créer un utilisateur | `/app/admin/users/create` |
| Statistiques avancées | `/app/admin/stats` |

### ENSEIGNANT

| Fonctionnalité | URL |
|---|---|
| Dashboard | `/app/teacher/dashboard` |
| Gérer les projets (PFE) | `/app/teacher/projects` |
| Créer / modifier un projet | `/app/teacher/projects/create` |
| Gérer les matières (Options) | `/app/teacher/subjects` |
| Créer une campagne de matching | `/app/teacher/campaigns/create` |
| Liste des campagnes | `/app/teacher/campaigns` |
| Résultats de matching | `/app/teacher/campaigns/results/:id` |
| Pilotage (lancer le matching) | `/app/teacher/matching-control` |

### ÉTUDIANT

| Fonctionnalité | URL |
|---|---|
| Dashboard (KPIs + top matches) | `/app/student/dashboard` |
| Parcourir les campagnes | `/app/student/campaigns` |
| Consulter les offres d'une campagne | `/app/student/items/:id` |
| Mes vœux (résumé + statut) | `/app/student/preferences` |
| Classer ses vœux (drag & drop) | `/app/student/preferences?campaignId=X` |
| Mon profil (compétences, intérêts) | `/app/student/profile` |

---

## API REST

La documentation Swagger est disponible après démarrage du backend :
**http://localhost:8081/swagger-ui/index.html**

### Principaux endpoints

#### Authentification
```
POST   /api/auth/login                          Connexion (retourne JWT)
POST   /api/auth/register                       Inscription
```

#### Compétences & Mots-clés
```
GET    /api/skills/active                       Liste des compétences actives
POST   /api/skills                              Créer une compétence (ENSEIGNANT/ADMIN)
GET    /api/keywords/active                     Liste des mots-clés actifs
POST   /api/keywords                            Créer un mot-clé
```

#### Projets & Matières
```
GET    /api/projects                            Liste tous les projets
POST   /api/projects                            Créer un projet
PUT    /api/projects/:id                        Modifier un projet
GET    /api/subjects                            Liste toutes les matières
POST   /api/subjects                            Créer une matière
```

#### Campagnes
```
GET    /api/campaigns                           Liste des campagnes
POST   /api/campaigns                           Créer une campagne
GET    /api/campaigns/:id/complete              Détail complet (campagne + items)
```

#### Matching
```
POST   /api/matching/run                        Lancer le matching (ENSEIGNANT)
GET    /api/matching/campaign/:id               Résultats par campagne
GET    /api/matching/student/:id                Résultats par étudiant
GET    /api/matching/campaign/:cId/project/:pId Résultats par projet
DELETE /api/matching/campaign/:id               Supprimer les résultats
```

#### Préférences
```
POST   /api/preferences                         Soumettre un vœu
GET    /api/preferences/student/:id             Vœux d'un étudiant
GET    /api/preferences/student/:sId/campaign/:cId  Vœux par campagne
```

#### Étudiants
```
GET    /api/students/user/:userId               Profil par userId
PUT    /api/students/:id                        Mettre à jour le profil
```

---

## Algorithmes de matching

L'enseignant choisit l'algorithme lors de la création de la campagne.

### WEIGHTED (Pondéré)

Calcule un score global pour chaque paire étudiant-projet selon trois dimensions :

```
globalScore = (skillsScore × skillsWeight)
            + (interestsScore × interestsWeight)
            + (workTypeScore × workTypeWeight)
```

- **skillsScore** : proportion des compétences requises maîtrisées par l'étudiant
- **interestsScore** : proportion des mots-clés du projet correspondant aux intérêts de l'étudiant
- **workTypeScore** : correspondance entre types de travail proposés et préférés

Résultat : classement de tous les étudiants par projet, du plus compatible au moins compatible.
La préférence n°1 de chaque étudiant passe au statut **ACCEPTED**.

### STABLE (Gale-Shapley)

Algorithme d'appariement stable avec capacités :

1. Chaque étudiant propose son projet préféré (selon ses vœux manuels)
2. Chaque projet accepte provisoirement les meilleurs candidats (selon score pondéré)
3. Les candidats moins bien classés sont refusés et reproposent leur prochain choix
4. L'algorithme converge vers un appariement stable

Résultat : affectation définitive. Les préférences des étudiants affectés passent au statut **ACCEPTED**.

---

## Structure du projet

### Backend — Packages principaux

```
fr.amu.bestchoice/
├── model/
│   ├── entity/          Student, Teacher, Project, Subject, Skill, Keyword,
│   │                    MatchingCampaign, MatchingResult, StudentPreference...
│   └── enums/           WorkType, PreferenceStatus, MatchingCampaignType...
├── repository/          Interfaces Spring Data JPA
├── service/
│   ├── interfaces/      Contrats de service
│   └── implementation/
│       ├── user/        StudentService, TeacherService, UserService
│       ├── skills/      SkillService, KeywordService
│       ├── project/     ProjectService
│       ├── subject/     SubjectService
│       ├── campaign/    CampaignService
│       ├── matching/    MatchingResultService
│       └── algorithmes/ MatchingScoringService, WeightedMatchingStrategy,
│                        StableMatchingStrategy, MatchingContextService
├── web/
│   ├── controller/      Contrôleurs REST par domaine
│   ├── dto/             Records Java (request/response)
│   ├── mapper/          Interfaces MapStruct
│   └── exception/       GlobalExceptionHandler, NotFoundException...
└── security/            SecurityConfig, JwtService, JwtAuthenticationFilter
```

### Frontend — Modules principaux

```
src/app/
├── core/
│   ├── auth/            AuthStore (signals), tokenInterceptor, guards
│   └── models/          Enums partagés (WorkType, PreferenceStatus...)
├── features/
│   ├── admin/           Pages et services admin
│   ├── teacher/
│   │   ├── pages/       dashboard, projects, subjects, campaigns,
│   │   │                matching-control, matching-results
│   │   └── services/    TeacherService
│   ├── student/
│   │   ├── pages/       dashboard, campaigns, preferences, profile, items
│   │   ├── services/    StudentService
│   │   └── models/      StudentResponse, PreferenceResponse...
│   ├── campaign/        CampaignService (partagé)
│   ├── matching/        MatchingService, MatchingResultResponse
│   ├── project/         ProjectService
│   └── subject/         SubjectService
└── shared/
    └── theme-toggle/    Composant bascule clair/sombre
```

---

## Comptes de test

> Les comptes sont créés via les données de démarrage ou l'interface admin.

| Rôle | Email | Mot de passe |
|---|---|---|
| Admin | admin@amu.fr | (défini au démarrage) |
| Enseignant | jean@amu.fr | (défini au démarrage) |
| Étudiant | akram@amu.fr | (défini au démarrage) |

---

## Auteurs

Projet réalisé dans le cadre d'un cours de développement logiciel à l'**AMU — Aix-Marseille Université**.

---

## Licence

Usage académique uniquement.
