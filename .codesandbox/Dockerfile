# codesandbox/csb is the base image for CodeSandbox
FROM codesandbox/csb

# Install Node.js 16
RUN apt-get update && \
    apt-get install -y curl && \
    curl -sL https://deb.nodesource.com/setup_16.x | bash - && \
    apt-get install -y nodejs

# Install Yarn
RUN npm install -g yarn

# Install Java 17
RUN apt-get install -y openjdk-17-jdk

# Install Maven
RUN apt-get install -y maven
