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
    stage('Start containers for load and stress testing') {
            when { branch 'stage' }
            steps {
                script {
                    sh '''
                    docker network create ecommerce-test || true

                    docker run -d --name zipkin-container --network ecommerce-test -p 9411:9411 openzipkin/zipkin

                    docker run -d --name service-discovery-container --network ecommerce-test -p 8761:8761 \\
                    -e SPRING_PROFILES_ACTIVE=dev \\
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 \\
                    gab27x/service-discovery:dev

                    until curl -s http://localhost:8761/actuator/health | grep '"status":"UP"' > /dev/null; do
                        echo "Waiting for service discovery to be ready..."
                        sleep 10
                    done

                    docker run -d --name cloud-config-container --network ecommerce-test -p 9296:9296 \\
                    -e SPRING_PROFILES_ACTIVE=dev \\
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 \\
                    -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://service-discovery-container:8761/eureka/ \\
                    -e EUREKA_INSTANCE=cloud-config-container \\
                    gab27x/cloud-config:dev

                    until curl -s http://localhost:9296/actuator/health | grep '"status":"UP"' > /dev/null; do
                        echo "Waiting for cloud config to be ready..."
                        sleep 10
                    done

                    # Iniciar api-gateway-container
                    docker run -d --name api-gateway-container --network microservices_network -p 8080:8080 \
                    -e SPRING_PROFILES_ACTIVE=dev \
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin:9411 \
                    -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296/ \
                    -e SPRING_CLOUD_CONFIG_URI=http://cloud-config-container:9296 \
                    -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://service-discovery-container:8761/eureka/ \
                    -e EUREKA_CLIENT_REGISTER_WITH_EUREKA=true \
                    -e EUREKA_CLIENT_FETCH_REGISTRY=true \
                    -e SPRING_JPA_HIBERNATE_DDL_AUTO=update \
                    -e EUREKA_INSTANCE_PREFER_IP_ADDRESS=true \
                    -e EUREKA_INSTANCE_HOSTNAME=api-gateway-container \
                    gab27x/api-gateway:dev

                    # Esperar a que api-gateway esté listo
                    until curl -s http://localhost:8080/actuator/health | grep '"status":"UP"' > /dev/null; do
                        echo "Waiting for API Gateway to be ready..."
                        sleep 10
                    done

                    docker run -d --name order-service-container --network ecommerce-test -p 8300:8300 \\
                    -e SPRING_PROFILES_ACTIVE=dev \\
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 \\
                    -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296 \\
                    -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://service-discovery-container:8761/eureka \\
                    -e EUREKA_INSTANCE=order-service-container \\
                    gab27x/order-service:dev

                    until [ "$(curl -s http://localhost:8300/order-service/actuator/health | jq -r '.status')" = "UP" ]; do
                        echo "Waiting for order service to be ready..."
                        sleep 10
                    done

                    docker run -d --name payment-service-container --network ecommerce-test -p 8400:8400 \\
                    -e SPRING_PROFILES_ACTIVE=dev \\
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 \\
                    -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296 \\
                    -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://service-discovery-container:8761/eureka \\
                    -e EUREKA_INSTANCE=payment-service-container \\
                    gab27x/payment-service:dev

                    until [ "$(curl -s http://localhost:8400/payment-service/actuator/health | jq -r '.status')" = "UP" ]; do
                        echo "Waiting for payment service to be ready..."
                        sleep 10
                    done

                    docker run -d --name product-service-container --network ecommerce-test -p 8500:8500 \\
                    -e SPRING_PROFILES_ACTIVE=dev \\
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 \\
                    -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296 \\
                    -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://service-discovery-container:8761/eureka \\
                    -e EUREKA_INSTANCE=product-service-container \\
                    gab27x/product-service:dev

                    until [ "$(curl -s http://localhost:8500/product-service/actuator/health | jq -r '.status')" = "UP" ]; do
                        echo "Waiting for product service to be ready..."
                        sleep 10
                    done

                    docker run -d --name shipping-service-container --network ecommerce-test -p 8600:8600 \\
                    -e SPRING_PROFILES_ACTIVE=dev \\
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 \\
                    -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296 \\
                    -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://service-discovery-container:8761/eureka \\
                    -e EUREKA_INSTANCE=shipping-service-container \\
                    gab27x/shipping-service:dev

                    until [ "$(curl -s http://localhost:8600/shipping-service/actuator/health | jq -r '.status')" = "UP" ]; do
                        echo "Waiting for shipping service to be ready..."
                        sleep 10
                    done

                    docker run -d --name user-service-container --network ecommerce-test -p 8700:8700 \\
                    -e SPRING_PROFILES_ACTIVE=dev \\
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 \\
                    -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296 \\
                    -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://service-discovery-container:8761/eureka \\
                    -e EUREKA_INSTANCE=user-service-container \\
                    gab27x/user-service:dev

                    until [ "$(curl -s http://localhost:8700/user-service/actuator/health | jq -r '.status')" = "UP" ]; do
                        echo "Waiting for user service to be ready..."
                        sleep 10
                    done

                    docker run -d --name favourite-service-container --network ecommerce-test -p 8800:8800 \\
                    -e SPRING_PROFILES_ACTIVE=dev \\
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 \\
                    -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296 \\
                    -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://service-discovery-container:8761/eureka \\
                    -e EUREKA_INSTANCE=favourite-service-container \\
                    gab27x/favourite-service:dev

                    until [ "$(curl -s http://localhost:8800/favourite-service/actuator/health | jq -r '.status')" = "UP" ]; do
                        echo "Waiting for favourite service to be ready..."
                        sleep 10
                    done

                    '''
                }
            }
        }



        stage('Run Load Tests with Locust') {
            when { branch 'stage' }
            steps {
                script {
                    sh '''
                    mkdir -p locust-reports

                    # Ejecutar Locust en un contenedor temporal de Python
                        docker run --rm --network ecommerce-test \
                        -v $PWD/tests/locust:/mnt/locust \
                        -w /mnt/locust \
                        -e GATEWAY_HOST=http://api-gateway-container:8080 \
                        -e FAVOURITE_HOST_DIRECT=http://favourite-service-container:8800 \
                        python:3.11-slim bash -c "\
                            pip install --no-cache-dir -r requirements.txt locust && \
                            locust -f locustfile.py --headless -u 10 -r 2 -t 1m --only-summary --html /mnt/locust/locust-report.html \
                        "
                    '''
                }
            }
        }

        stage('OWASP ZAP Scan') {
            when { branch 'stage' }
            steps {
                script {
                    echo '==> Iniciando escaneos con OWASP ZAP'

                    def targets = [
                            [name: 'order-service', url: 'http://order-service-container:8300/order-service'],
                            [name: 'payment-service', url: 'http://payment-service-container:8400/payment-service'],
                            [name: 'product-service', url: 'http://product-service-container:8500/product-service'],
                            [name: 'shipping-service', url: 'http://shipping-service-container:8600/shipping-service'],
                            [name: 'user-service', url: 'http://user-service-container:8700/user-service'],
                            [name: 'favourite-service', url: 'http://favourite-service-container:8800/favourite-service']
                    ]

                    sh 'mkdir -p zap-reports'

                    targets.each { service ->
                        def reportFile = "zap-reports/report-${service.name}.html"
                        echo "==> Escaneando ${service.name} (${service.url})"
                        sh """
                            docker run --rm \
                            --network ecommerce-test \
                            -v ${env.WORKSPACE}:/zap/wrk \
                            zaproxy/zap-stable \
                            zap-baseline.py \
                            -t ${service.url} \
                            -r ${reportFile} \
                            -I
                        """
                    }
                }
            }
        }


        stage('Publicar Reportes de Seguridad') {
            when { branch 'stage' }
            steps {
                echo '==> Publicando reportes HTML en interfaz Jenkins'
                publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'zap-reports',
                        reportFiles: 'report-*.html',
                        reportName: 'ZAP Security Reports',
                        reportTitles: 'OWASP ZAP Full Scan Results'
                ])
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
                                    sh '''
                docker rm -f $(docker ps -aq)
                '''
                    echo 'Staging deployment completed successfully!'

                } else {
                                    sh '''
                docker rm -f $(docker ps -aq)
                '''
                    echo 'Development tests completed successfully!'
                }

            }
        }
        failure {

             script {
                sh '''
                docker rm -f $(docker ps -aq)
                '''

            }


        }



    }



}