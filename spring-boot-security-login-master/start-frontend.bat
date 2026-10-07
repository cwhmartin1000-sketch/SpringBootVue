@echo off
cd /d "%~dp0\vue-3-authentication-jwt-master"
npm install
npm run serve -- --host 0.0.0.0
