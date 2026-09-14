@echo off
setlocal
title Dem01 - avvio
cd /d "%~dp0"

echo ============================================
echo  Dem01 - avvio Backend + Frontend
echo ============================================
echo.

rem ---------- Credenziali Gmail: vivono in variabili d'ambiente, non nel codice ----------
if "%MAIL_USERNAME%"=="" (
  echo [gmail] MAIL_USERNAME non impostata: il backend NON partira'.
  echo         Impostala per questa sessione:  set MAIL_USERNAME=nome.cognome@gmail.com
  echo         Oppure in modo permanente:      setx MAIL_USERNAME "nome.cognome@gmail.com"
)
if "%MAIL_PASSWORD%"=="" (
  echo [gmail] MAIL_PASSWORD non impostata: il backend NON partira'.
  echo         E' la password per le app da 16 caratteri, non quella dell'account.
  echo         set MAIL_PASSWORD=abcdefghijklmnop
)
if not "%MAIL_USERNAME%"=="" if not "%MAIL_PASSWORD%"=="" echo [gmail] credenziali presenti per %MAIL_USERNAME%.
echo.

rem ---------- La porta di invio deve essere aperta in uscita (slide 8) ----------
powershell -NoProfile -Command "$c=New-Object Net.Sockets.TcpClient; try { $c.Connect('smtp.gmail.com',587); exit 0 } catch { exit 1 }" >nul 2>&1
if errorlevel 1 (
  echo [smtp] smtp.gmail.com:587 NON raggiungibile: la rete filtra la porta in uscita.
  echo        Prova la 465 prima di rinunciare:  set MAIL_PORT=465
) else (
  echo [smtp] smtp.gmail.com:587 raggiungibile.
)
echo.
rem ---------- Frontend: dipendenze ----------
if not exist "FE\node_modules" (
  echo [FE] node_modules assente, eseguo npm install...
  pushd FE
  call npm install
  popd
  echo.
)

rem ---------- Backend Spring Boot su 8080 ----------
echo [BE] avvio in una nuova finestra...
start "Dem01 BE (8080)" /D "%~dp0BE" cmd /k .\mvnw.cmd spring-boot:run

rem ---------- Frontend Vite su 5173 ----------
echo [FE] avvio in una nuova finestra...
start "Dem01 FE (5173)" /D "%~dp0FE" cmd /k npm run dev

echo.
echo --------------------------------------------
echo  Applicazione : http://localhost:5173
echo  API          : http://localhost:8080
echo  Casella posta: https://mail.google.com
echo --------------------------------------------
echo  Chiudi le due finestre per fermare tutto.
echo.
endlocal
