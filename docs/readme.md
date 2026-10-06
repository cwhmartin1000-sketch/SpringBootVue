# 🚀 專案匯入與開發環境建立標準 SOP (Onboarding Guide)

本指南適用於接手全新的 Spring Boot + Vue 3 專案，從環境檢測、專案匯入、VS Code 工作區配置到前後端啟動的完整流程。

---

## 📋 第一階段：環境前置檢查 (Environment Prerequisites)

在匯入任何專案前，請先於 Terminal / CMD 確認本機開發工具鏈版本是否符合要求：

### 1. 指令檢查清單
```bash
# 檢查 Java 版本 (Spring Boot 3.x 必須使用 JDK 17+)
java -version

# 檢查 Node.js 與 npm 版本 (建議 Node LTS v18 或 v20)
node -v
npm -v

# 檢查 Git 版本
git --version
```

### 2. 專案initial指令
```bash
# Run following SQL insert statements
INSERT INTO roles(name) VALUES('ROLE_USER');
INSERT INTO roles(name) VALUES('ROLE_MODERATOR');
INSERT INTO roles(name) VALUES('ROLE_ADMIN');
```