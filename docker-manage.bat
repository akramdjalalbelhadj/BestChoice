@echo off
REM BestChoice Docker Management Script for Windows
REM Usage: docker-manage.bat [command]

setlocal enabledelayedexpansion

:main
if "%1"=="" (
    call :show_help
    exit /b 0
)

call :check_requirements

if "%1"=="start" (
    call :start
) else if "%1"=="stop" (
    call :stop
) else if "%1"=="restart" (
    call :restart
) else if "%1"=="rebuild" (
    call :rebuild
) else if "%1"=="logs" (
    call :logs_all
) else if "%1"=="logs:backend" (
    call :logs_backend
) else if "%1"=="logs:frontend" (
    call :logs_frontend
) else if "%1"=="logs:mysql" (
    call :logs_mysql
) else if "%1"=="ps" (
    call :show_status
) else if "%1"=="clean" (
    call :clean
) else if "%1"=="clean:all" (
    call :clean_all
) else if "%1"=="shell:backend" (
    call :shell_backend
) else if "%1"=="shell:mysql" (
    call :shell_mysql
) else if "%1"=="health" (
    call :health_check
) else if "%1"=="help" (
    call :show_help
) else (
    echo Commande inconnue: %1
    call :show_help
    exit /b 1
)

exit /b 0

:print_header
echo.
echo ╔═══════════════════════════════════════╗
echo ║     BestChoice Docker Manager         ║
echo ╚═══════════════════════════════════════╝
echo.
exit /b 0

:show_help
call :print_header
echo Commandes disponibles:
echo.
echo   start         - Demarrer l'application
echo   stop          - Arreter l'application
echo   restart       - Redemarrer l'application
echo   rebuild       - Reconstruire les images
echo   logs          - Voir tous les logs
echo   logs:backend  - Voir les logs du backend
echo   logs:frontend - Voir les logs du frontend
echo   logs:mysql    - Voir les logs de MySQL
echo   ps            - Afficher le statut des services
echo   clean         - Nettoyer (arreter et supprimer)
echo   clean:all     - Nettoyer completement (avec volumes)
echo   shell:backend - CMD dans le backend
echo   shell:mysql   - MySQL CLI dans la base
echo   health        - Verifier la sante des services
echo.
exit /b 0

:check_requirements
docker --version >nul 2>&1
if errorlevel 1 (
    echo Docker n'est pas installe
    exit /b 1
)

docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo Docker Compose n'est pas installe
    exit /b 1
)

echo Docker et Docker Compose trouves
echo.
exit /b 0

:start
echo Demarrage de l'application...
docker-compose up -d
echo Application demarree!
echo.
echo URLs:
echo   Frontend: http://localhost
echo   Backend:  http://localhost:8080
echo   MySQL:    localhost:3306
exit /b 0

:stop
echo Arret de l'application...
docker-compose down
echo Application arretee!
exit /b 0

:restart
call :stop
timeout /t 2 /nobreak
call :start
exit /b 0

:rebuild
echo Reconstruction des images...
docker-compose build
echo Images reconstruites!
exit /b 0

:logs_all
docker-compose logs -f
exit /b 0

:logs_backend
docker-compose logs -f backend
exit /b 0

:logs_frontend
docker-compose logs -f frontend
exit /b 0

:logs_mysql
docker-compose logs -f mysql
exit /b 0

:show_status
echo Etat des services:
docker-compose ps
exit /b 0

:clean
echo Nettoyage...
docker-compose down
echo Nettoyage termine!
exit /b 0

:clean_all
echo Suppression complete (volumes inclus)...
set /p confirm="Etes-vous sur? (y/n): "
if /i "%confirm%"=="y" (
    docker-compose down -v --rmi all
    echo Tout supprime!
)
exit /b 0

:shell_backend
echo Entree dans le backend...
docker-compose exec backend bash
exit /b 0

:shell_mysql
echo Connexion a MySQL...
docker-compose exec mysql mysql -u root -proot bestchoice
exit /b 0

:health_check
echo Health Check:
echo.
echo Verification du backend...
docker-compose exec -T backend wget -q --spider http://localhost:8080/actuator/health
if errorlevel 1 (
    echo Backend: FAIL
) else (
    echo Backend: OK
)

echo.
echo Verification du frontend...
docker-compose exec -T frontend wget -q --spider http://localhost/
if errorlevel 1 (
    echo Frontend: FAIL
) else (
    echo Frontend: OK
)

echo.
echo Verification de MySQL...
docker-compose exec -T mysql mysqladmin ping -u root -proot
if errorlevel 1 (
    echo MySQL: FAIL
) else (
    echo MySQL: OK
)

exit /b 0

