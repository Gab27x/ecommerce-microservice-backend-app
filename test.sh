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
                    docker run -d --name api-gateway-container --network ecommerce-test -p 8080:8080 \
                    -e SPRING_PROFILES_ACTIVE=dev \\
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 \\
                    -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296 \\
                    -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://service-discovery-container:8761/eureka \\
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

                        echo "Waiting for API Gateway to be ready..."
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