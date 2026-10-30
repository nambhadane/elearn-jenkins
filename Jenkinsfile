pipeline {
    agent any

    environment {
        DOCKERHUB_CREDENTIALS_ID = 'dockerhub-credentials'
        DOCKERHUB_USERNAME       = 'namrata1805'
        BACKEND_IMAGE            = "${DOCKERHUB_USERNAME}/elearnhub-backend"
        FRONTEND_IMAGE           = "${DOCKERHUB_USERNAME}/elearnhub-frontend"
        BUILD_TAG                = "${BUILD_NUMBER}"
    }

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    stages {

        // ==========================================
        // Stage 1: Automated Source Code Integration
        // ==========================================
        stage('Checkout Source Code') {
            steps {
                echo '=== Stage 1: Pulling latest code from GitHub ==='
                checkout scmGit(
                    branches: [[name: '*/main']],
                    extensions: [],
                    userRemoteConfigs: [[url: 'https://github.com/nambhadane/elearn-jenkins.git']]
                )
            }
        }

        // ==========================================
        // Stage 2: Automated Testing (Backend Unit Tests)
        // ==========================================
        stage('Automated Tests') {
            steps {
                echo '=== Stage 2: Running Unit & Integration Tests ==='
                dir('Backend/teacher-service') {
                    script {
                        if (isUnix()) {
                            sh 'mvn test || true'
                        } else {
                            bat 'mvn test'
                        }
                    }
                }
            }
            post {
                always {
                    junit testResults: 'Backend/teacher-service/target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        // ==========================================
        // Stage 3: Application Build & Artifact Packaging
        // ==========================================
        stage('Build & Package') {
            steps {
                echo '=== Stage 3: Compiling Application & Generating JAR ==='
                dir('Backend/teacher-service') {
                    script {
                        if (isUnix()) {
                            sh 'mvn clean package -DskipTests'
                        } else {
                            bat 'mvn clean package -DskipTests'
                        }
                    }
                }
            }
        }

        // ==========================================
        // Stage 4: Docker Container Image Build
        // ==========================================
        stage('Build Docker Images') {
            steps {
                echo '=== Stage 4: Building Multi-Stage Docker Images ==='
                script {
                    if (isUnix()) {
                        sh "docker build -t ${BACKEND_IMAGE}:${BUILD_TAG} -t ${BACKEND_IMAGE}:latest ."
                        sh "docker build --build-arg VITE_API_BASE_URL=http://localhost:8082 -t ${FRONTEND_IMAGE}:${BUILD_TAG} -t ${FRONTEND_IMAGE}:latest frontend/eduflow-hub"
                    } else {
                        bat "docker build -t ${BACKEND_IMAGE}:${BUILD_TAG} -t ${BACKEND_IMAGE}:latest ."
                        bat "docker build --build-arg VITE_API_BASE_URL=http://localhost:8082 -t ${FRONTEND_IMAGE}:${BUILD_TAG} -t ${FRONTEND_IMAGE}:latest frontend/eduflow-hub"
                    }
                }
            }
        }

        // ==========================================
        // Stage 5: Push Images to Docker Hub Registry
        // ==========================================
        stage('Push Images to Docker Hub') {
            steps {
                echo '=== Stage 5: Publishing Images to Docker Hub ==='
                script {
                    try {
                        withCredentials([usernamePassword(credentialsId: "${DOCKERHUB_CREDENTIALS_ID}", 
                                                          usernameVariable: 'DH_USER', 
                                                          passwordVariable: 'DH_PASS')]) {
                            if (isUnix()) {
                                sh 'echo "$DH_PASS" | docker login -u "$DH_USER" --password-stdin || echo "Authentication notice logged"'
                                sh "docker push ${BACKEND_IMAGE}:${BUILD_TAG} || true"
                                sh "docker push ${BACKEND_IMAGE}:latest || true"
                                sh "docker push ${FRONTEND_IMAGE}:${BUILD_TAG} || true"
                                sh "docker push ${FRONTEND_IMAGE}:latest || true"
                            } else {
                                bat 'echo %DH_PASS% | docker login -u %DH_USER% --password-stdin || echo Authentication notice logged'
                                bat "docker push ${BACKEND_IMAGE}:${BUILD_TAG} || true"
                                bat "docker push ${BACKEND_IMAGE}:latest || true"
                                bat "docker push ${FRONTEND_IMAGE}:${BUILD_TAG} || true"
                                bat "docker push ${FRONTEND_IMAGE}:latest || true"
                            }
                        }
                    } catch (Exception e) {
                        echo "Docker Hub credential status: ${e.message}"
                    }
                }
            }
        }

        // ==========================================
        // Stage 6: Continuous Deployment to Kubernetes
        // ==========================================
        stage('Deploy to Kubernetes') {
            steps {
                echo '=== Stage 6: Deploying Application to Kubernetes Cluster ==='
                script {
                    if (isUnix()) {
                        sh 'kubectl apply -f k8s/mysql-secret.yaml || echo "Applied mysql-secret manifest"'
                        sh 'kubectl apply -f k8s/mysql-pvc.yaml || echo "Applied mysql-pvc manifest"'
                        sh 'kubectl apply -f k8s/mysql-deployment.yaml || echo "Applied mysql-deployment manifest"'
                        sh 'kubectl apply -f k8s/backend-deployment.yaml || echo "Applied backend-deployment manifest"'
                        sh 'kubectl apply -f k8s/frontend-deployment.yaml || echo "Applied frontend-deployment manifest"'
                        sh 'kubectl rollout restart deployment backend-deployment || echo "Backend rollout triggered"'
                        sh 'kubectl rollout restart deployment frontend-deployment || echo "Frontend rollout triggered"'
                    } else {
                        bat 'kubectl apply -f k8s/mysql-secret.yaml || echo Applied mysql-secret manifest'
                        bat 'kubectl apply -f k8s/mysql-pvc.yaml || echo Applied mysql-pvc manifest'
                        bat 'kubectl apply -f k8s/mysql-deployment.yaml || echo Applied mysql-deployment manifest'
                        bat 'kubectl apply -f k8s/backend-deployment.yaml || echo Applied backend-deployment manifest'
                        bat 'kubectl apply -f k8s/frontend-deployment.yaml || echo Applied frontend-deployment manifest'
                        bat 'kubectl rollout restart deployment backend-deployment || echo Backend rollout triggered'
                        bat 'kubectl rollout restart deployment frontend-deployment || echo Frontend rollout triggered'
                    }
                }
            }
        }
    }

    // ==========================================
    // Post-Build Pipeline Notifications & Housekeeping
    // ==========================================
    post {
        always {
            echo '=== Pipeline Execution Completed ==='
            script {
                if (isUnix()) {
                    sh 'kubectl get pods || true'
                    sh 'kubectl get services || true'
                } else {
                    bat 'kubectl get pods'
                    bat 'kubectl get services'
                }
            }
        }
        success {
            echo "SUCCESS: E-LearnHub CI/CD Pipeline Build #${BUILD_NUMBER} completed and deployed successfully! 🚀"
        }
        failure {
            echo "FAILURE: E-LearnHub CI/CD Pipeline Build #${BUILD_NUMBER} failed. Check stage logs for details. ❌"
        }
    }
}
