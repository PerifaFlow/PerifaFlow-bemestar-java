# 1) Stage de build: usa Maven para gerar o JAR
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# copia os arquivos do Maven
COPY pom.xml .
COPY src ./src

# gera o jar (sem rodar testes pra ficar mais rápido)
RUN mvn -U -DskipTests package

# 2) Stage final: imagem leve só com o JRE + jar
FROM eclipse-temurin:17-jre

WORKDIR /app

# copia o jar gerado no stage de build
COPY --from=build /app/target/bemestar-0.0.1-SNAPSHOT.jar app.jar

# perfil dev (H2 em memória)
ENV SPRING_PROFILES_ACTIVE=dev

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
