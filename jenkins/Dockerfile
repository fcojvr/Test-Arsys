FROM jenkins/jenkins:2.492.1-lts-jdk17

USER root

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        ca-certificates curl gnupg lsb-release && \
    install -m 0755 -d /etc/apt/keyrings && \
    curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg && \
    chmod a+r /etc/apt/keyrings/docker.gpg && \
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
          https://download.docker.com/linux/debian $(lsb_release -cs) stable" \
          > /etc/apt/sources.list.d/docker.list && \
    apt-get update && \
    apt-get install -y --no-install-recommends docker-ce-cli && \
    rm -rf /var/lib/apt/lists/*


COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

RUN mkdir -p /var/jenkins_home/casc_configs
COPY casc/jenkins.yaml /var/jenkins_home/casc_configs/jenkins.yaml

RUN mkdir -p /usr/share/jenkins/ref/init.groovy.d
COPY init.groovy.d/create-jobs.groovy /usr/share/jenkins/ref/init.groovy.d/create-jobs.groovy

RUN chown -R jenkins:jenkins /var/jenkins_home/casc_configs /usr/share/jenkins/ref/

USER jenkins
ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"

ENV CASC_JENKINS_CONFIG="/var/jenkins_home/casc_configs/jenkins.yaml"