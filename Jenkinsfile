pipeline {

    agent any

    tools {
        maven 'Maven'
        jdk 'Java11'

    }
    environment {
        GIT_REPO = 'https://github.com/Gab27x/ecommerce-microservice-backend-app.git'
        DOCKERHUB_USER = 'gab27x'
        SERVICES = 'api-gateway cloud-config favourite-service order-service payment-service product-service proxy-client service-discovery shipping-service user-service'
        DOCKER_CREDENTIALS_ID = 'docker-hub-creds'
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

        stage('Build & Push Docker Images') {
            when {
                anyOf {
                    branch 'develop'
                    branch 'stage'
                    branch 'master'
                }
            }
            steps {
                script {

                    // LOGIN correcto a Docker Hub
                    withCredentials([usernamePassword(
                        credentialsId: "${DOCKER_CREDENTIALS_ID}",
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )]) {
                        sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
                    }

                    // Build & Push MULTI-SERVICIO correcto
                    SERVICES.split().each { service ->
                        sh """
                            docker buildx build --platform linux/amd64,linux/arm64 \
                            -t ${DOCKERHUB_USER}/${service}:${IMAGE_TAG} \
                            --build-arg SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} \
                            --push ./${service}
                        """
                    }
                }
            }
        }





        stage('Trivy Vulnerability Scan & Report') {
            when { branch 'develop' }

            environment{
                TRIVY_PATH = '/opt/homebrew/bin'
            }
            steps {
                script {
                    env.PATH = "${TRIVY_PATH}:${env.PATH}"


                    def services = [
                            'api-gateway',
                            'cloud-config',
                            'favourite-service',
                            'order-service',
                            'payment-service',
                            'product-service',
                            'proxy-client',
                            'service-discovery',
                            'shipping-service',
                            'user-service'
                    ]

                    sh "mkdir -p trivy-reports"

                    sh '''
                        curl -L https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/html.tpl -o html.tpl
                    '''

                    services.each { service ->
                        def reportPath = "trivy-reports/${service}.html"

                        sh"""
                            trivy image --format template --scanners vuln \\
                            --template "@html.tpl" \\
                            --severity HIGH,CRITICAL \\
                            -o ${reportPath} \\
                            ${DOCKERHUB_USER}/${service}:${IMAGE_TAG}
                        """
                    }

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