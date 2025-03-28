pipeline {
    agent any
 
    environment {
        SONAR_TOKEN = credentials('sonar-token') // Matches Jenkins credentials ID
    }
 
    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/CathalDawsonBolado/six_nations_blog.git', branch: 'feature/testing'
            }
        }
 
        stage('Build') {
            steps {
                dir('') {
                    bat 'mvn clean install -DskipTests'
                }
            }
        }
 
        stage('Test') {
            steps {
                dir('') {
                    bat 'mvn test'
                }
            }
        }
 
        stage('SonarQube Analysis') {
            steps {
                dir('') {
                    bat '''
                        mvn clean verify sonar:sonar \
                        -Dsonar.projectKey=rugby_blog \
                        -Dsonar.projectName="rugby_blog" \
                        -Dsonar.host.url=http://sonarqube:9000 \
                        -Dsonar.token=${SONAR_TOKEN}
                    '''
                }
            }
        }

        }
    }
}