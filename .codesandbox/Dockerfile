# Base image
FROM node:16-alpine

# Install Java 17
RUN apk add --no-cache openjdk17

# Install Maven
RUN apk add --no-cache maven

# Install Zsh
RUN apk add --no-cache zsh

# Set the default shell to Zsh
SHELL ["/bin/zsh", "-c"]
