# Base image
FROM node:16

# Install Yarn
RUN npm install -g yarn

# Install Java 17
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk

# Install Maven
RUN apt-get install -y maven
