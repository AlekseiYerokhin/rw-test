# =============================================================
# Сборка: Maven + JDK 11, внутри собирается и Angular (frontend-maven-plugin)
# =============================================================
FROM maven:3.9-eclipse-temurin-11 AS build
WORKDIR /src

# Манифесты отдельным слоем — кэш зависимостей Maven при пересборке
COPY backend/pom.xml backend/pom.xml

# Исходники (node_modules/target исключены через .dockerignore)
COPY frontend frontend
COPY backend backend

# Одна команда: npm ci + ng build (Node скачает плагин) + упаковка WAR
RUN mvn -B -f backend/pom.xml clean package

# =============================================================
# Runtime: официальный Tomcat 9 (Java 11)
# =============================================================
FROM tomcat:9.0-jdk11-temurin

# Демо-приложения Tomcat не нужны; приложение разворачивается в корень
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /src/backend/target/app.war /usr/local/tomcat/webapps/ROOT.war

# Пароль БД задаётся в docker-compose (APP_DB_PASSWORD)
ENV APP_DB_PASSWORD=""

EXPOSE 8080
CMD ["catalina.sh", "run"]
