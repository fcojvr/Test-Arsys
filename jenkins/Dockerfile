# Jenkins LTS with pre-installed plugins and Docker CLI
FROM jenkins/jenkins:2.492.1-lts-jdk17

USER root

# Install Docker CLI so Jenkins can run docker commands on the host socket
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        ca-certificates \
        curl \
        gnupg \
        lsb-release && \
    install -m 0755 -d /etc/apt/keyrings && \
    curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg && \
    chmod a+r /etc/apt/keyrings/docker.gpg && \
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
         https://download.docker.com/linux/debian $(lsb_release -cs) stable" \
         > /etc/apt/sources.list.d/docker.list && \
    apt-get update && \
    apt-get install -y --no-install-recommends docker-ce-cli && \
    rm -rf /var/lib/apt/lists/*

USER jenkins

# Skip the setup wizard
ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"

# Install plugins via the Plugin Installation Manager
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

# Copy JCasC configuration
COPY casc/jenkins.yaml /var/jenkins_home/casc.yaml

# Copy Job DSL seed script
COPY init.groovy.d/create-jobs.groovy /var/jenkins_home/init.groovy.d/create-jobs.groovy
