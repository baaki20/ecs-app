# Use a lightweight OpenJDK base image
FROM eclipse-temurin:21-jre

# Set the working directory in the container
WORKDIR /app

# Copy the compiled application JAR file into the container
COPY target/personal-app.jar app.jar

# Expose the port the application will run on
EXPOSE 8080

# Define the command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]