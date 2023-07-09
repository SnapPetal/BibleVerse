# Use the official Node.js 16 Alpine image as the base
FROM node:16-alpine

# Install necessary packages
RUN apk add --no-cache \
    openjdk17 \
    maven \
    zsh \
    yarn

# Set the default shell to Zsh
SHELL ["/bin/zsh", "-c"]
