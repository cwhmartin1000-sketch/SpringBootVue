@echo off
where java >nul 2>nul
if errorlevel 1 (
  echo Please install JDK 17 and make sure java is on PATH.
  pause
  exit /b 1
)
cd /d "%~dp0\spring-boot-security-login-master"
.\mvnw.cmd spring-boot:run
