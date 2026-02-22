FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY gradle gradle
COPY gradlew build.gradle.kts settings.gradle.kts ./
RUN ./gradlew dependencies --no-daemon || true
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre
RUN groupadd -r -g 1001 appgroup && useradd -r -u 1001 -g appgroup appuser
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
RUN chown 1001:1001 app.jar
USER 1001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
