FROM ubuntu:22.04

# --- Basic dependencies ---
RUN apt-get update && apt-get install -y \
    curl \
    git \
    gnupg \
    ca-certificates \
    apt-transport-https \
    software-properties-common \
    python3 \
    python3-pip \
    supervisor

# --- Install Java (OpenJDK 21) ---
RUN apt-get update && apt-get install -y openjdk-21-jdk

# --- Install Maven ---
RUN apt-get update && apt-get install -y maven

# --- Install Node.js 18 ---
RUN curl -fsSL https://deb.nodesource.com/setup_18.x | bash - \
    && apt-get install -y nodejs

# --- App workspace ---
WORKDIR /app

# --- Copy backend source ---
COPY Backend/teacher-service /app/Backend/teacher-service

# --- Copy frontend source ---
COPY frontend/eduflow-hub /app/frontend/eduflow-hub

# --- Install frontend dependencies (vite) ---
RUN cd /app/frontend/eduflow-hub && npm install

# --- Build backend jar ---
RUN mvn -f /app/Backend/teacher-service/pom.xml clean package -DskipTests

# --- Copy jar to universal location ---
RUN cp /app/Backend/teacher-service/target/*.jar /app/app.jar

# --- Expose ports for frontend + backend ---
EXPOSE 8081 8082

# --- Add supervisor config ---
COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf

# --- Start supervisor (runs both services) ---
CMD ["/usr/bin/supervisord", "-n"]
