FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive

# ===============================
# Basic dependencies
# ===============================
RUN apt-get update && apt-get install -y \
    curl \
    git \
    gnupg \
    ca-certificates \
    apt-transport-https \
    software-properties-common \
    python3 \
    python3-pip \
    supervisor \
    mysql-server \
    && rm -rf /var/lib/apt/lists/*

# ===============================
# Java (OpenJDK 21)
# ===============================
RUN apt-get update && apt-get install -y openjdk-21-jdk

# ===============================
# Maven
# ===============================
RUN apt-get update && apt-get install -y maven

# ===============================
# Node.js 18
# ===============================
RUN curl -fsSL https://deb.nodesource.com/setup_18.x | bash - \
    && apt-get install -y nodejs

# ===============================
# App workspace
# ===============================
WORKDIR /app

# ===============================
# Copy backend source
# ===============================
COPY Backend/teacher-service /app/Backend/teacher-service

# ===============================
# Copy frontend source
# ===============================
COPY frontend/eduflow-hub /app/frontend/eduflow-hub

# ===============================
# Install frontend dependencies
# ===============================
RUN cd /app/frontend/eduflow-hub && npm install

# ===============================
# Build backend jar
# ===============================
RUN mvn -f /app/Backend/teacher-service/pom.xml clean package -DskipTests

# ===============================
# Copy jar to common location
# ===============================
RUN cp /app/Backend/teacher-service/target/*.jar /app/app.jar

# ===============================
# MySQL initial setup
# ===============================
RUN service mysql start && \
    mysql -u root -e "CREATE DATABASE IF NOT EXISTS elearn_teacher;" && \
    mysql -u root -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'Nikit@16burgute'; FLUSH PRIVILEGES;"

# ===============================
# Expose ports
# ===============================
EXPOSE 8081 8082 3306

# ===============================
# Supervisor config
# ===============================
COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf

# ===============================
# Start all services
# ===============================
CMD ["/usr/bin/supervisord", "-n"]