# Use an official Maven image as the base image
FROM registry.access.redhat.com/ubi8/openjdk-17:1.10 AS build
#FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
USER 100001
# Set the working directory in the container
WORKDIR /app
# Copy the pom.xml and the project files to the container
COPY pom.xml .
COPY src ./src
# Build the application using Maven
RUN mvn clean package -DskipTests

FROM provectuslabs/kafka-ui:latest
USER 100001
WORKDIR /serde
COPY --from=build /app/target/kafka-ui-apicurio-avro-serde-jar-with-dependencies.jar .


