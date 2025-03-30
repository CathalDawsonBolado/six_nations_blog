pipeline {
    agent any

    environment {
        SONAR_TOKEN = credentials('sonar-token')              // SonarQube token
        DOCKER_CREDENTIALS = credentials('dockerhub-credentials')   // Replace with your DockerHub Jenkins credentials ID
        IMAGE_NAME = 'cdb97/rugby-blog'                       // Your DockerHub repo name
    }

    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/CathalDawsonBolado/six_nations_blog.git', branch: 'feature/testing'
            }
        }

        stage('Build') {
            steps {
                bat 'mvn clean install -DskipTests'
            }
        }

        stage('Test') {
            steps {
                bat 'mvn test'
            }
        }

        stage('Package') {
            steps {
                bat 'mvn package -DskipTests'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                bat '''
                    mvn clean verify sonar:sonar ^
                    -Dsonar.projectKey=rugby_blog ^
                    -Dsonar.projectName="rugby_blog" ^
                    -Dsonar.coverage.jacoco.xmlReportPaths=target\\jacoco-report-merged\\jacoco.xml ^
                    -Dsonar.host.url=http://localhost:9000 ^
                    -Dsonar.token=%SONAR_TOKEN%
                '''
            }
        }

        stage('Build Docker Image') {
            steps {
                bat "docker build -t %IMAGE_NAME% ."
            }
        }

        stage('Push Docker Image to DockerHub') {
            steps {
                bat '''
                    docker login -u %DOCKER_CREDENTIALS_USR% -p %DOCKER_CREDENTIALS_PSW%
                    docker push %IMAGE_NAME%
                '''
            }
        }

        stage('Run Docker Container') {
            steps {
                bat '''
                    docker stop rugby-blog || echo "No container to stop"
                    docker rm rugby-blog || echo "No container to remove"
                    docker run -d -p 8081:8081 --name rugby-blog %IMAGE_NAME%
                '''
            }
        }
    }

    post {
        success {
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
        }
    }
}




