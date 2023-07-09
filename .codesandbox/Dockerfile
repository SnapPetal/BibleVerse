# codesandbox/codesandbox-base is the base image for CodeSandbox
FROM codesandbox/codesandbox-base

# Install Node.js 16
RUN apt-get update && \
    apt-get install -y curl && \
    curl -sL https://deb.nodesource.com/setup_16.x | bash - && \
    apt-get install -y nodejs

# Install Yarn
RUN npm install -g yarn

# Install Java 17
# You may need to adjust this depending on CodeSandbox's supported Java versions
RUN apt-get install -y openjdk-17-jdk

# Install Maven
# You may need to adjust this depending on CodeSandbox's supported Maven versions
RUN apt-get install -y maven
