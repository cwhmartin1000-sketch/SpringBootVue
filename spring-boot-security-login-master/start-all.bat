@echo off
start "Vue Frontend" cmd /k "cd /d ""%~dp0vue-3-authentication-jwt-master"" && npm install && npm run serve -- --host 0.0.0.0"
start "Spring Boot API" cmd /k "cd /d ""%~dp0spring-boot-security-login-master"" && where java >nul 2>nul && .\mvnw.cmd spring-boot:run || (echo Please install JDK 17 and make sure java is on PATH. & pause)"
