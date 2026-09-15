@echo off
setlocal
title Dem03 - Avvio BE + FE
cd /d "%~dp0"

echo ====================================
echo  Dem03 - avvio Backend + Frontend
echo ====================================
echo.

if not exist "%~dp0BE\mvnw.cmd" (
  echo [ERRORE] BE\mvnw.cmd non trovato in %~dp0
  pause
  exit /b 1
)

if not exist "%~dp0FE\node_modules" (
  echo [FE] node_modules assente: eseguo npm install...
  pushd "%~dp0FE"
  call npm install
  if errorlevel 1 (
    echo [ERRORE] npm install fallito.
    popd
    pause
    exit /b 1
  )
  popd
)

echo [BE] avvio Spring Boot su http://localhost:8080 ...
start "Dem03 - BE" /d "%~dp0BE" cmd /k mvnw.cmd spring-boot:run

echo [FE] avvio Vite su http://localhost:5173 ...
start "Dem03 - FE" /d "%~dp0FE" cmd /k npm run dev

echo.
echo Due finestre aperte. Chiudile (o CTRL+C) per fermare i servizi.
echo Frontend: http://localhost:5173
echo Backend : http://localhost:8080
echo.
timeout /t 5 >nul
endlocal
