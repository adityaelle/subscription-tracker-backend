# Start from a lightweight image that already has Java 17 installed
FROM eclipse-temurin:17-jre-alpine
# Set the working folder inside the container
WORKDIR app

# Copy your built jar into the container
COPY target/*.jar app.jar
# Tell Docker this container listens on port 8080
EXPOSE 8080

# The command to run when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]