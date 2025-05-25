# Use a slim OpenJDK image for a smaller footprint
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file from the target directory of your Maven build
# The name of the JAR file should match the artifactId and version in your pom.xml
COPY target/forex-0.0.1-SNAPSHOT.jar app.jar

# Expose the port your Spring Boot application runs on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]