
pipeline {
    agent any

    tools {
        maven 'maven-3.9.9'
    }

    stages {

        stage('Git Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/Sunilreddy277/Board_game_DBproject.git'
            }
        }

        stage('Check Nexus URL') {
            steps {
                sh '''
                    echo "Checking Nexus configuration..."
                    grep -n "distributionManagement" -A10 pom.xml
                '''
            }
        }

        stage('Compile') {
            steps {
                sh 'mvn compile'
            }
        }

        stage('Test') {
            steps {
                withCredentials([
                    string(
                        credentialsId: 'BUGS_USER_PASSWORD',
                        variable: 'BUGS_USER_PASSWORD'
                    ),
                    string(
                        credentialsId: 'DAFFY_USER_PASSWORD',
                        variable: 'DAFFY_USER_PASSWORD'
                    )
                ]) {
                    sh 'mvn test'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonarqube') {
                    sh '''
                        mvn sonar:sonar \
                        -Dsonar.projectKey=BoardGame \
                        -Dsonar.projectName=BoardGame
                    '''
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }

        stage('Nexus Deploy') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'nexus-credentials',
                    usernameVariable: 'NEXUS_USERNAME',
                    passwordVariable: 'NEXUS_PASSWORD'
                )]) {

                    configFileProvider([
                        configFile(
                            fileId: 'nexus-maven-settings',
                            variable: 'MAVEN_SETTINGS'
                        )
                    ]) {

                        sh '''
                            mvn -s "$MAVEN_SETTINGS" deploy -DskipTests
                        '''
                    }
                }
            }
        }

        stage('Docker Build & Push') {
            agent {
                label 'wsl-minikube'
            }

            steps {
                git branch: 'main',
                    url: 'https://github.com/Sunilreddy277/Board_game_DBproject.git'

                sh '''
                    echo "Building application..."
                    mvn package -DskipTests

                    echo "Building Docker image..."
                '''

                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKERHUB_USERNAME',
                    passwordVariable: 'DOCKERHUB_TOKEN'
                )]) {
                    sh '''
                        echo "$DOCKERHUB_TOKEN" | docker login \
                            -u "$DOCKERHUB_USERNAME" --password-stdin

                        docker build -t sunilreddy7/boardgame:latest .

                        docker push sunilreddy7/boardgame:latest

                        docker logout
                    '''
                }
            }
        }

        stage('Kubernetes Deploy') {
            agent {
                label 'wsl-minikube'
            }

            steps {
                git branch: 'main',
                    url: 'https://github.com/Sunilreddy277/Board_game_DBproject.git'

                sh '''
                    echo "Deploying BoardGame to Minikube..."

                    kubectl apply -f deployment.yaml

                    kubectl rollout restart deployment/boardgame-deployment

                    kubectl rollout status deployment/boardgame-deployment --timeout=180s

                    echo "Checking Kubernetes Pods..."
                    kubectl get pods

                    echo "Checking Kubernetes Service..."
                    kubectl get svc boardgame-service
                '''
            }
        }

        stage('Success') {
            steps {
                echo 'Build completed: SonarQube, Nexus, Docker Hub and Kubernetes!'
            }
        }
    }
}

