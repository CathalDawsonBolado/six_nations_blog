stage('Package') {
            steps {
                dir('') {
                    bat 'mvn package -DskipTests'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                dir('') {
                    bat '''
                        mvn verify sonar:sonar ^
                        -Dsonar.projectKey=rugby_blog ^
                        -Dsonar.projectName="rugby_blog" ^
                        -Dsonar.coverage.jacoco.xmlReportPaths=target\\jacoco-report-merged\\jacoco.xml ^
                        -Dsonar.host.url=http://localhost:9000 ^
                        -Dsonar.token=%SONAR_TOKEN%
                    '''
                }
            }
        }
    }

    post {
        success {
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
        }
    }
}


