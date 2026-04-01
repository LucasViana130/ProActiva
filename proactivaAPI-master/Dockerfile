FROM eclipse-temurin:17
WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw clean package -DskipTests
EXPOSE 8080
CMD ["java", "-jar", "target/quarkus-app/quarkus-run.jar"]
