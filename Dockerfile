FROM eclipse-temurin:17-jre

WORKDIR /app

# copia o jar gerado pelo Maven
COPY target/bemestar-0.0.1-SNAPSHOT.jar app.jar

# perfil dev (H2 em memória)
ENV SPRING_PROFILES_ACTIVE=dev

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
