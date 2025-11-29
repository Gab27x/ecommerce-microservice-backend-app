pipeline {

    agent any

    tools {
        maven 'Maven'
        jdk 'Java11'
    }
    environment {
        GIT_REPO = 'https://github.com/Gab27x/ecommerce-microservice-backend-app.git'
 
    }

    stages {

        stage('clean'){
            steps{
                cleanWs()
            
            }
        }

        stage('Scanning Branch') {
            steps {
                script {
                    echo "Detected branch: ${env.BRANCH_NAME}"
                    def profileConfig = [
                            master : ['prod', '-prod'],
                            stage: ['stage', '-stage']
                    ]
                    def config = profileConfig.get(env.BRANCH_NAME, ['dev', '-dev'])

                    env.SPRING_PROFILES_ACTIVE = config[0]
                    env.IMAGE_TAG = config[0]
                    env.DEPLOYMENT_SUFFIX = config[1]

                    env.IS_MASTER = env.BRANCH_NAME == 'master' ? 'true' : 'false'
                    env.IS_STAGE = env.BRANCH_NAME == 'stage' ? 'true' : 'false'
                    env.IS_DEV = env.BRANCH_NAME == 'dev' ? 'true' : 'false'
                    env.IS_FEATURE = env.BRANCH_NAME.startsWith('feature/') ? 'true' : 'false'

                    echo "Spring profile: ${env.SPRING_PROFILES_ACTIVE}"
                    echo "Image tag: ${env.IMAGE_TAG}"
                    echo "Deployment suffix: ${env.DEPLOYMENT_SUFFIX}"
                    echo "Flags: IS_MASTER=${env.IS_MASTER}, IS_STAGE=${env.IS_STAGE}, IS_DEV=${env.IS_DEV}, IS_FEATURE=${env.IS_FEATURE}"
                }
            }
        }


        stage('Checkout') {
            steps {
                git branch: "${env.BRANCH_NAME}", url:  "${GIT_REPO}"
            }
        }



        stage('BUILD AND TEST') {
            when {
                anyOf {
                    branch 'develop'
                    branch 'stage'
                    branch 'master'
                }
            }

            steps {
                sh "mvn clean package"
            }
        }

        stage('SONARQUBE SCAN') {
            when { branch 'develop' }

        
            steps {
                script {
                    sh '''
                    set -e

                    echo "=== Levantando SonarQube en Docker ==="
                    docker network create sonar-net || true
                    docker rm -f sonarqube || true
                    docker run -d \
                    --name sonarqube \
                    --network sonar-net \
                    -p 9000:9000 \
                    --platform linux/arm64 \
                    sonarqube:lts-community

                    echo "=== Esperando a que SonarQube esté listo ==="
                    until curl -s http://localhost:9000/api/system/status | grep -q '"status":"UP"'; do
                        echo "Esperando 5s..."
                        sleep 5
                    done

                    echo "=== Generando token automático ==="
                    SONAR_TOKEN=$(curl -s -u admin:admin -X POST "http://localhost:9000/api/user_tokens/generate?name=pipeline-token" | grep -o '"token":"[^"]*"' | cut -d':' -f2 | tr -d '"')
                    echo "Token generado: $SONAR_TOKEN"

                    echo "=== Ejecutando análisis Maven Sonar ==="
                    mvn clean package -DskipTests sonar:sonar \
                        -Dsonar.host.url=http://localhost:9000 \
                        -Dsonar.login=${SONAR_TOKEN} || true

                    echo "=== Limpiando SonarQube ==="
                    docker rm -f sonarqube || true
                    docker network rm sonar-net || true

                    echo "=== FIN DEL STAGE ==="
                    '''
                }
            }
        }


        stage('TRIVY SCAN') {
            when { branch 'develop' }
            steps {
                script {
                    sh "mkdir -p trivy-reports"

                    sh '''
                        curl -L https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/html.tpl \
                        -o html.tpl
                    '''

                    sh '''
                        docker run --rm \
                            -v ${PWD}:/repo \
                            aquasec/trivy:latest fs /repo \
                            --template "@/repo/html.tpl" \
                            --exit-code 1 \
                            --severity HIGH,CRITICAL \
                            --scanners vulnerability,secret > trivy-reports/report.html || true
                    '''

                    publishHTML(target: [
                        allowMissing: true,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'trivy-reports',
                        reportFiles: '*.html',
                        reportName: 'Trivy Scan Report'
                    ])
                }
            }
        }


    }





    post {

        success {
            script {
                echo "Pipeline completed successfully for ${env.BRANCH_NAME} branch."

                if (env.BRANCH_NAME == 'master') {
                    echo 'Production deployment completed successfully!'
                } else if (env.BRANCH_NAME == 'stage') {
                    echo 'Staging deployment completed successfully!'

                } else {
                    echo 'Development tests completed successfully!'
                }
            }
        }

    }



}