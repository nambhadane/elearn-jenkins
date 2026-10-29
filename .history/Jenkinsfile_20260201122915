pipeline {
    agent any

    environment {
        IMAGE_NAME = "mahdisomjee04/merged-docker" // lowercase is safer
        IMAGE_TAG = "2.0.0"
    }

    stages {

        stage('Try') {
            steps {
                echo "hello i have startyed"
            }
        }

        stage('Checkout Code') {
            steps {
                git branch: 'docker-demo', url: 'https://github.com/mahdisomjee123/jenkins-demo.git'
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    echo "Building Docker image..."
                    def customImage = docker.build("${IMAGE_NAME}:${IMAGE_TAG}")
                    env.IMAGE_ID = customImage.id
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', 'dockerhub-creds') {
                        docker.image("${IMAGE_NAME}:${IMAGE_TAG}").push()
                    }
                }
            }
        }
    }

    post {
        success { echo "Docker image successfully built and pushed 🚀" }
        failure { echo "Pipeline failed ❌" }
    }
}