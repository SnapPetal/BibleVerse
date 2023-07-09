FROM amazoncorretto:17

# Install Zsh
RUN yum update -y && \
    yum install -y zsh && \
    chsh -s /usr/bin/zsh

# Install Yarn
RUN curl -sL https://dl.yarnpkg.com/rpm/yarn.repo -o /etc/yum.repos.d/yarn.repo && \
    yum install -y yarn

# Install Node.js 16
RUN curl -sL https://rpm.nodesource.com/setup_16.x | bash - && \
    yum install -y nodejs

# Set Zsh as the default shell
SHELL ["/usr/bin/zsh", "-c"]
