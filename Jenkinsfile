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
        K8S_NAMESPACE = 'microservices'

        AWS_REGION = 'us-east-1'
        CLUSTER_NAME = 'ecommerce-prod'
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

                    docker run -d --name service-discovery-container --network ecommerce-test -p 8761:8761 \
                    -e SPRING_PROFILES_ACTIVE=dev \
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 \
                    gab27x/service-discovery:dev

                    until [ "$(curl -s http://localhost:8761/actuator/health | jq -r '.status')" = "UP" ]; do
                        echo "Waiting for service discovery to be ready..."
                        sleep 10
                    done

                    docker run -d --name cloud-config-container --network ecommerce-test -p 9296:9296 \
                    -e SPRING_PROFILES_ACTIVE=dev \
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 \
                    -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://service-discovery-container:8761/eureka/ \
                    -e EUREKA_INSTANCE=cloud-config-container \
                    gab27x/cloud-config:dev

                    until [ "$(curl -s http://localhost:9296/actuator/health | jq -r '.status')" = "UP" ]; do
                        echo "Waiting for cloud config to be ready..."
                        sleep 10
                    done

                    docker run -d --name api-gateway-container --network ecommerce-test -p 8080:8080 \
                    -e SPRING_PROFILES_ACTIVE=dev \
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 \
                    -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296 \
                    -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://service-discovery-container:8761/eureka \
                    -e EUREKA_INSTANCE_HOSTNAME=api-gateway-container \
                    gab27x/api-gateway:dev

                    until [ "$(curl -s http://localhost:8080/actuator/health | jq -r '.status')" = "UP" ]; do
                        echo "Waiting for API Gateway to be ready..."
                        sleep 10
                    done

                    docker run -d --name order-service-container --network ecommerce-test -p 8300:8300 \
                    -e SPRING_PROFILES_ACTIVE=dev \
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 \
                    -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296 \
                    -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://service-discovery-container:8761/eureka \
                    -e EUREKA_INSTANCE=order-service-container \
                    gab27x/order-service:dev

                    until [ "$(curl -s http://localhost:8300/order-service/actuator/health | jq -r '.status')" = "UP" ]; do
                        echo "Waiting for order service to be ready..."
                        sleep 10
                    done

                    docker run -d --name payment-service-container --network ecommerce-test -p 8400:8400 \
                    -e SPRING_PROFILES_ACTIVE=dev \
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 \
                    -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296 \
                    -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://service-discovery-container:8761/eureka \
                    -e EUREKA_INSTANCE=payment-service-container \
                    gab27x/payment-service:dev

                    until [ "$(curl -s http://localhost:8400/payment-service/actuator/health | jq -r '.status')" = "UP" ]; do
                        echo "Waiting for payment service to be ready..."
                        sleep 10
                    done

                    docker run -d --name product-service-container --network ecommerce-test -p 8500:8500 \
                    -e SPRING_PROFILES_ACTIVE=dev \
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 \
                    -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296 \
                    -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://service-discovery-container:8761/eureka \
                    -e EUREKA_INSTANCE=product-service-container \
                    gab27x/product-service:dev

                    until [ "$(curl -s http://localhost:8500/product-service/actuator/health | jq -r '.status')" = "UP" ]; do
                        echo "Waiting for product service to be ready..."
                        sleep 10
                    done

                    docker run -d --name shipping-service-container --network ecommerce-test -p 8600:8600 \
                    -e SPRING_PROFILES_ACTIVE=dev \
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 \
                    -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296 \
                    -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://service-discovery-container:8761/eureka \
                    -e EUREKA_INSTANCE=shipping-service-container \
                    gab27x/shipping-service:dev

                    until [ "$(curl -s http://localhost:8600/shipping-service/actuator/health | jq -r '.status')" = "UP" ]; do
                        echo "Waiting for shipping service to be ready..."
                        sleep 10
                    done

                    docker run -d --name user-service-container --network ecommerce-test -p 8700:8700 \
                    -e SPRING_PROFILES_ACTIVE=dev \
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 \
                    -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296 \
                    -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://service-discovery-container:8761/eureka \
                    -e EUREKA_INSTANCE=user-service-container \
                    gab27x/user-service:dev

                    until [ "$(curl -s http://localhost:8700/user-service/actuator/health | jq -r '.status')" = "UP" ]; do
                        echo "Waiting for user service to be ready..."
                        sleep 10
                    done

                    docker run -d --name favourite-service-container --network ecommerce-test -p 8800:8800 \
                    -e SPRING_PROFILES_ACTIVE=dev \
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 \
                    -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296 \
                    -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://service-discovery-container:8761/eureka \
                    -e EUREKA_INSTANCE=favourite-service-container \
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
                            locust -f locustfile.py --headless -u 5 -r 1 -t 30s --only-summary --html /mnt/locust/locust-report.html \
                        "

                    '''
                }
            }
        }


        stage('Publish Locust Report') {
            when { branch 'stage' }
            steps {
                echo '==> Publicando reporte de Locust en Jenkins'
                publishHTML([
                    allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'tests/locust',        // ruta relativa al workspace
                    reportFiles: 'locust-report.html',
                    reportName: 'Locust Load Test Report',
                    reportTitles: 'Load Test Results'
                ])
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


        stage('Upload Artifacts') {
        when { branch 'master' }
        steps {
            script {
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

                def version = "${env.BUILD_ID}-${new Date().format('yyyyMMdd-HHmmss')}"

                def artifacts = services.collect { service ->
                    [
                        artifactId: service,
                        classifier: '',
                        file: "${service}/target/${service}-v0.1.0.jar",
                        type: 'jar'
                    ]
                }

                nexusArtifactUploader(
                    nexusVersion: 'nexus3',
                    protocol: 'http',
                    nexusUrl: 'localhost:8081',
                    groupId: 'com.ecommerce',
                    version: version,
                    repository: 'ecommerce-app',
                    credentialsId: 'nexusLogin',
                    artifacts: artifacts
                )
            }
        }
    }


    stage('Configure kubeconfig') {
            when { branch 'master' }
            steps {
                withAWS(credentials: 'aws-cred', region: "${AWS_REGION}") {
                    sh '''
                export KUBECONFIG=$WORKSPACE/kubeconfig
                    aws eks update-kubeconfig \\
                        --name ecommerce-prod \\
                        --region us-east-1 \\
                        --role-arn arn:aws:iam::393177628930:role/software5 \\
                        --kubeconfig $PWD/kubeconfig
                kubectl --kubeconfig $KUBECONFIG get nodes
                kubectl get pods -A
            '''
                }
            }
        }

        stage('Create Namespace') {
            when { branch 'master' }
            steps {
                withAWS(credentials: 'aws-cred', region: 'us-east-1') {
                    sh '''
                export KUBECONFIG=$WORKSPACE/kubeconfig
                aws eks update-kubeconfig \
                    --name ecommerce-prod \
                    --region us-east-1 \
                    --role-arn arn:aws:iam::393177628930:role/software5 \
                    --kubeconfig $KUBECONFIG

                kubectl get namespace ${K8S_NAMESPACE} || kubectl create namespace ${K8S_NAMESPACE}
            '''
                }
            }
        }




        stage('Deploy common config for microservices') {
            when { branch 'master' }
            steps {
                withAWS(credentials: 'aws-cred', region: 'us-east-1') {
                    sh '''
                export KUBECONFIG=$WORKSPACE/kubeconfig
                kubectl apply -f k8s/common-config.yaml -n ${K8S_NAMESPACE}
            '''
                }
            }
        }


        stage('Deploy Core Services') {
            when { branch 'master' }
            steps {
                withAWS(credentials: 'aws-cred', region: 'us-east-1') {
                    sh '''
                export KUBECONFIG=$WORKSPACE/kubeconfig

                kubectl apply -f k8s/zipkin/ -n ${K8S_NAMESPACE}
                kubectl rollout status deployment/zipkin -n ${K8S_NAMESPACE} --timeout=200s

                kubectl apply -f k8s/service-discovery/ -n ${K8S_NAMESPACE}
                kubectl set image deployment/service-discovery service-discovery=${DOCKERHUB_USER}/service-discovery:dev -n ${K8S_NAMESPACE}
                kubectl set env deployment/service-discovery SPRING_PROFILES_ACTIVE=dev -n ${K8S_NAMESPACE}
                kubectl rollout status deployment/service-discovery -n ${K8S_NAMESPACE} --timeout=200s

                kubectl apply -f k8s/cloud-config/ -n ${K8S_NAMESPACE}
                kubectl set image deployment/cloud-config cloud-config=${DOCKERHUB_USER}/cloud-config:dev -n ${K8S_NAMESPACE}
                kubectl set env deployment/cloud-config SPRING_PROFILES_ACTIVE=dev -n ${K8S_NAMESPACE}
                kubectl rollout status deployment/cloud-config -n ${K8S_NAMESPACE} --timeout=300s
            '''
                }
            }
        }

        stage('Deploy Ingress') {
            when { branch 'master' }
            steps {
                withAWS(credentials: 'aws-cred', region: 'us-east-1') {
                    sh '''
                export KUBECONFIG=$WORKSPACE/kubeconfig

                kubectl apply -f k8s/ingress.yaml -n ${K8S_NAMESPACE}
                kubectl get ingress -n ${K8S_NAMESPACE}
            '''
                }
            }
        }



        stage('Deploy Microservices') {
            when { branch 'master' }
            steps {
                withAWS(credentials: 'aws-cred', region: 'us-east-1') {
                    script {
                        sh '''
                    export KUBECONFIG=$WORKSPACE/kubeconfig

                    aws eks update-kubeconfig \
                        --name ecommerce-prod \
                        --region us-east-1 \
                        --role-arn arn:aws:iam::393177628930:role/software5 \
                        --kubeconfig $KUBECONFIG
                '''

                        def appServices = [
                                'product-service','user-service','order-service','payment-service','favourite-service','shipping-service','proxy-client','api-gateway'
                        ]

                        for (svc in appServices) {
                            def image = "${DOCKERHUB_USER}/${svc}:dev"

                            sh "kubectl apply -f k8s/${svc}/ -n ${K8S_NAMESPACE}"
                            sh "kubectl set image deployment/${svc} ${svc}=${image} -n ${K8S_NAMESPACE}"
                            sh "kubectl set env deployment/${svc} SPRING_PROFILES_ACTIVE=dev -n ${K8S_NAMESPACE}"
                            sh "kubectl rollout status deployment/${svc} -n ${K8S_NAMESPACE} --timeout=200s"
                        }
                    }
                }
            }
        }

        stage('Generate SemVer Release & Tag') {
                    when { branch 'master' }
                    steps {
                        script {
                            sh '''
                                git config user.name "gab27x"
                                git config user.email "gabrielernestoeb@outlook.com"
                            '''

                            def lastTag = sh(script: 'git describe --tags --abbrev=0', returnStdout: true).trim()
                            def commitMessages = sh(script: "git log ${lastTag}..HEAD --pretty=%B", returnStdout: true).trim()

                            def bumpType = ''
                            if (commitMessages.contains('BREAKING CHANGE')) {
                                bumpType = 'major'
                            } else if (commitMessages.split('\n').any { it.startsWith('feat') }) {
                                bumpType = 'minor'
                            } else if (commitMessages.split('\n').any { it.startsWith('fix') }) {
                                bumpType = 'patch'
                            } else {
                                echo 'No version bump detected'
                            }

                            if (bumpType) {
                                // Calcular nueva versión
                                def versionParts = lastTag.replace('v','').tokenize('.').collect { it.toInteger() }
                                switch (bumpType) {
                                    case 'major': versionParts[0] += 1; versionParts[1] = 0; versionParts[2] = 0; break
                                    case 'minor': versionParts[1] += 1; versionParts[2] = 0; break
                                    case 'patch': versionParts[2] += 1; break
                                }
                                env.NEW_VERSION = versionParts.join('.')
                                env.CREATE_TAG = true
                                echo "New version: ${env.NEW_VERSION}"
                            } else {
                                env.NEW_VERSION = lastTag.replace('v','')
                                env.CREATE_TAG = false
                                echo "Using current version: ${env.NEW_VERSION}"
                            }

                            // Generar release notes
                            def commitsRaw = sh(script: "git log ${lastTag}..HEAD --pretty=format:'%h %s (%an)'", returnStdout: true).trim()
                            def features = [], fixes = [], chores = [], docs = [], breaking = []

                            commitsRaw.split('\n').each { line ->
                                def lower = line.toLowerCase()
                                if (lower.contains('breaking change')) { breaking << line }
                                else if (lower.startsWith('feat')) { features << line }
                                else if (lower.startsWith('fix')) { fixes << line }
                                else if (lower.startsWith('chore')) { chores << line }
                                else if (lower.startsWith('docs')) { docs << line }
                                else { chores << line }
                            }

                            def releaseNotes = "# Release v${env.NEW_VERSION}\n"
                            releaseNotes += "## Changes since ${lastTag}\n\n"

                            if (breaking) { releaseNotes += "### BREAKING CHANGES\n" + breaking.collect { "- ${it}" }.join('\n') + "\n\n" }
                            if (features) { releaseNotes += "### Features\n" + features.collect { "- ${it}" }.join('\n') + "\n\n" }
                            if (fixes) { releaseNotes += "### Bug Fixes\n" + fixes.collect { "- ${it}" }.join('\n') + "\n\n" }
                            if (docs) { releaseNotes += "### Documentation\n" + docs.collect { "- ${it}" }.join('\n') + "\n\n" }
                            if (chores) { releaseNotes += "### Chores / Misc\n" + chores.collect { "- ${it}" }.join('\n') + "\n\n" }

                            writeFile file: 'RELEASE_NOTES.md', text: releaseNotes
                            archiveArtifacts artifacts: 'RELEASE_NOTES.md', fingerprint: true

                            withCredentials([usernamePassword(credentialsId: 'github_cred', usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD')]) {
                                sh """
                            git add RELEASE_NOTES.md
                            git commit -m "chore(release): v${env.NEW_VERSION}" || true
                        """

                                if (env.CREATE_TAG.toBoolean()) {
                                    sh """
                                git tag -d v${env.NEW_VERSION} || true
                                git tag -a v${env.NEW_VERSION} -m "Release v${env.NEW_VERSION}"
                                git push https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/gab27x/ecommerce-microservice-backend-app.git ${env.BRANCH_NAME} --tags
                            """
                                } else {
                                    sh """
                                git push https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/gab27x/ecommerce-microservice-backend-app.git ${env.BRANCH_NAME}
                            """
                                }
                            }
                        }
                    }
                }

            stage('Create GitHub Release') {
                when { branch 'master' }
                steps {
                    script {
                        if (env.CREATE_TAG.toBoolean()) {
                            // Leer release notes completos
                            def releaseNotes = readFile('RELEASE_NOTES.md').trim()

                            // Crear JSON de request usando JsonOutput para escapar correctamente
                            def jsonBody = groovy.json.JsonOutput.toJson([
                                    tag_name: "v${env.NEW_VERSION}",
                                    target_commitish: "master",
                                    name: "v${env.NEW_VERSION}",
                                    body: releaseNotes,
                                    draft: false,
                                    prerelease: false,
                                    generate_release_notes: false
                            ])

                            withCredentials([string(credentialsId: 'gh_create_release', variable: 'GIT_TOKEN')]) {
                                def response = httpRequest(
                                        url: 'https://api.github.com/repos/gab27x/ecommerce-microservice-backend-app/releases',
                                        httpMode: 'POST',
                                        customHeaders: [
                                                [name: 'Accept', value: 'application/vnd.github+json'],
                                                [name: 'Authorization', value: "Bearer ${GIT_TOKEN}"],
                                                [name: 'X-GitHub-Api-Version', value: '2022-11-28'],
                                                [name: 'Content-Type', value: 'application/json']
                                        ],
                                        requestBody: jsonBody,
                                        validResponseCodes: '201'
                                )

                                echo "Release creation response: ${response.content}"
                            }
                        } else {
                            echo "No new version bump. Skipping GitHub Release creation."
                        }
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