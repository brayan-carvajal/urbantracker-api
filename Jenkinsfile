pipeline {
  agent any

  environment {
    IMAGE_BASE = 'backend'              
    NETWORK_PREFIX = 'myproject-net'    
    COMPOSE_PROJECT_NAME = 'urbantracker'
  }

  stages {
    
    stage('Permisos workspace') {
      steps {
        sh '''
          echo "🔧 Corrigiendo permisos del workspace..."
          sudo chmod -R 777 $WORKSPACE || chmod -R 777 $WORKSPACE || true
        '''
      }
    }

    // =====================================================
    // 1️⃣ Leer entorno desde .env (raíz)
    // =====================================================
    stage('Leer entorno desde .env') {
      steps {
        script {
          if (!fileExists('.env')) {
            error ".env no encontrado en la raíz. Debe contener: ENVIRONMENT=<develop|staging|prod>"
          }
          sh '''
            set -e
            ENVIRONMENT=$(grep -E '^ENVIRONMENT=' .env | cut -d'=' -f2 | tr -d '\\r\\n')
            if [ -z "$ENVIRONMENT" ]; then
              echo "ENVIRONMENT no definido en .env"
              exit 1
            fi
            echo "ENVIRONMENT=$ENVIRONMENT" > env.properties
            # Rutas corregidas dentro del repo
            echo "ENV_DIR=Backend/Devops/$ENVIRONMENT" >> env.properties
            echo "COMPOSE_FILE=Backend/Devops/$ENVIRONMENT/docker-compose.yml" >> env.properties
            echo "ENV_FILE=Backend/Devops/$ENVIRONMENT/.env.$ENVIRONMENT" >> env.properties
          '''
          def props = readProperties file: 'env.properties'
          env.ENVIRONMENT = props['ENVIRONMENT']
          env.ENV_DIR = props['ENV_DIR']
          env.COMPOSE_FILE = props['COMPOSE_FILE']
          env.ENV_FILE = props['ENV_FILE']

          echo """
          ✅ Entorno detectado: ${env.ENVIRONMENT}
          📁 DevOps dir: ${env.ENV_DIR}
          🗂 Compose: ${env.COMPOSE_FILE}
          📄 Env file: ${env.ENV_FILE}
          """
        }
      }
    }

    // =====================================================
    // 2️⃣ Verificar herramientas necesarias
    // =====================================================
    stage('Verificar herramientas') {
      steps {
        sh '''
          echo "🔍 Verificando herramientas..."
          docker --version
          mvn --version
          curl --version
        '''
      }
    }

    // =====================================================
    // 3️⃣ Compilar backend (maven inside container)
    // =====================================================
    stage('Compilar Backend') {
      steps {
        dir('Backend') {
          script {
            echo "📦 Compilando Backend con maven..."
            docker.image('maven:3.9.4-eclipse-temurin-17').inside {
              sh '''
                set -e
                # Verificar que pom.xml existe
                if [ ! -f "pom.xml" ]; then
                  echo "❌ pom.xml no encontrado en Backend/"
                  exit 1
                fi
                mvn -B clean package -DskipTests
              '''
            }
          }
        }
      }
    }

    // =====================================================
    // 4️⃣ Construir imagen Docker del backend
    // =====================================================
    stage('Construir imagen Docker') {
      steps {
        dir('Backend') {
          script {
            echo "🐳 Construyendo imagen Docker del backend..."
            
            // tag con commit hash corto
            def commit = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
            env.IMAGE_TAG = "${IMAGE_BASE}:${env.ENVIRONMENT}-${commit}"
            
            echo "📂 Directorio actual: $(pwd)"
            echo "📂 Listando contenido:"
            sh 'ls -la'
            
            // Verificar que el jar existe
            sh '''
              JARFILE=$(ls target/*.jar 2>/dev/null | head -n 1)
              if [ -z "$JARFILE" ]; then
                echo "❌ No se encontró jar en Backend/target"
                echo "📂 Contenido de target/:"
                ls -la target/ || echo "Target directory no existe"
                exit 1
              fi
              echo "✅ JAR encontrado: $JARFILE"
              
              # Crear directorio Devops/develop con estructura correcta
              mkdir -p Devops/develop
              cp ${JARFILE} Devops/develop/app.jar
              echo "✅ JAR copiado a Devops/develop/app.jar"
            '''
            
            // Verificar que Dockerfile.app existe
            if (!fileExists('Devops/develop/Dockerfile.app')) {
              error "❌ Devops/develop/Dockerfile.app no encontrado"
            }
            
            // construir imagen
            sh """
              echo "🐳 Construyendo imagen con tag: ${env.IMAGE_TAG}"
              docker build --no-cache -t ${env.IMAGE_TAG} -f Devops/develop/Dockerfile.app Devops/develop
            """
            echo "✅ Imagen creada: ${env.IMAGE_TAG}"
          }
        }
      }
    }

    // =====================================================
    // 5️⃣ Preparar servicios auxiliares (DB + Mosquitto)
    // =====================================================
    stage('Preparar servicios auxiliares') {
      steps {
        script {
          def netName = "${NETWORK_PREFIX}-${env.ENVIRONMENT}"
          echo "🌐 Creando red ${netName} ..."
          sh "docker network create ${netName} || echo '✅ Red ya existe'"

          if (env.ENVIRONMENT == 'develop' || env.ENVIRONMENT == 'staging' || env.ENVIRONMENT == 'qa') {
            script {
              echo "🚀 Iniciando servicios auxiliares..."
              
              // Levantar base de datos
              if (fileExists(env.COMPOSE_FILE)) {
                echo "🗄️ Usando docker-compose para servicios DB..."
                sh """
                  cd ${env.ENV_DIR}
                  docker-compose down || true
                  docker-compose up -d postgres-develop
                  sleep 10
                """
              } else {
                echo "⚠️ docker-compose no encontrado, iniciando PostgreSQL directamente..."
                sh """
                  docker run -d \\
                    --name urbantracker-postgres-${env.ENVIRONMENT} \\
                    --network ${netName} \\
                    -e POSTGRES_DB=urbantracker_${env.ENVIRONMENT} \\
                    -e POSTGRES_USER=postgres \\
                    -e POSTGRES_PASSWORD=develop1234 \\
                    -p 5433:5432 \\
                    --restart unless-stopped \\
                    postgres:15
                """
              }

              // Levantar Mosquitto MQTT
              echo "📡 Iniciando broker Mosquitto MQTT..."
              sh """
                docker run -d \\
                  --name urbantracker-mosquitto-${env.ENVIRONMENT} \\
                  --network ${netName} \\
                  -p 1883:1883 \\
                  -p 9001:9001 \\
                  -v mosquitto/config:/mosquitto/config \\
                  --restart unless-stopped \\
                  eclipse-mosquitto:2
              """
            }
          } else {
            echo "🛑 Ambiente prod detectado: saltando servicios locales"
          }
        }
      }
    }

    // =====================================================
    // 6️⃣ Desplegar backend
    // =====================================================
    stage('Desplegar Backend') {
      steps {
        script {
          if (env.ENVIRONMENT == 'prod') {
            echo "🚀 Despliegue remoto en producción - configurar SSH"
            // TODO: Implementar deployment por SSH
          } else {
            script {
              echo "🚀 Desplegando backend local (${env.ENVIRONMENT})"
              
              def networkName = "${NETWORK_PREFIX}-${env.ENVIRONMENT}"
              def imageTag = env.IMAGE_TAG
              def containerName = "urbantracker-backend-${env.ENVIRONMENT}"
              
              sh """
                echo "🔍 Preparando despliegue..."
                
                # Detener contenedor anterior si existe
                docker stop ${containerName} || true
                docker rm ${containerName} || true
                
                # Esperar liberación
                sleep 3
                
                # Configurar variables de entorno
                DB_HOST=urbantracker-postgres-${env.ENVIRONMENT}
                DB_PORT=5432
                DB_NAME=urbantracker_${env.ENVIRONMENT}
                DB_USERNAME=postgres
                DB_PASSWORD=develop1234
                MQTT_BROKER=urbantracker-mosquitto-${env.ENVIRONMENT}
                
                # Ejecutar contenedor backend
                echo "🚀 Iniciando contenedor backend en puerto 8081..."
                docker run -d \\
                  --name ${containerName} \\
                  --network ${networkName} \\
                  -p 8081:8080 \\
                  -e SPRING_PROFILES_ACTIVE=${env.ENVIRONMENT} \\
                  -e SPRING_DATASOURCE_URL=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME} \\
                  -e SPRING_DATASOURCE_USERNAME=${DB_USERNAME} \\
                  -e SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD} \\
                  -e SPRING_MQTT_BROKER_HOST=${MQTT_BROKER} \\
                  -e SPRING_MQTT_BROKER_PORT=1883 \\
                  --restart unless-stopped \\
                  ${imageTag}
                
                echo "✅ Contenedor backend iniciado en puerto 8081"
              """
            }
          }
        }
      }
    }

    // =====================================================
    // 7️⃣ Health checks y verificaciones
    // =====================================================
    stage('Health Checks') {
      steps {
        script {
          echo "🔎 Esperando servicios..."
          
          // Esperar database
          def dbHost = "urbantracker-postgres-${env.ENVIRONMENT}"
          def dbPort = "5432"
          echo "Comprobando postgres ${dbHost}:${dbPort}"
          sh """
            for i in $(seq 1 30); do
              docker run --rm --network ${NETWORK_PREFIX}-${env.ENVIRONMENT} postgres:15 pg_isready -h ${dbHost} -p ${dbPort} && break || sleep 2
            done || echo "⚠️ pg_isready timeout - continuando de todas formas"
          """

          // Esperar Mosquitto
          echo "Comprobando Mosquitto MQTT..."
          sh """
            for i in $(seq 1 20); do
              docker run --rm --network ${NETWORK_PREFIX}-${env.ENVIRONMENT} eclipse-mosquitto:2 mosquitto_pub -h urbantracker-mosquitto-${env.ENVIRONMENT} -p 1883 -t test/topic -m "test" && break || sleep 2
            done || echo "⚠️ Mosquitto no responde - continuando de todas formas"
          """

          // Comprobar health del backend
          echo "Comprobando health del backend..."
          sh """
            for i in $(seq 1 40); do
              echo "Intento \$i/40: Verificando http://localhost:8081/actuator/health"
              curl -sS --fail --connect-timeout 5 http://localhost:8081/actuator/health && break || {
                echo "Health check fallido, esperando..."
                sleep 3
              }
            done || echo "⚠️ Health check falló - revisar logs del contenedor"
            
            # Mostrar logs del contenedor para diagnóstico
            echo "📋 Logs del contenedor backend:"
            docker logs urbantracker-backend-${env.ENVIRONMENT} || true
          """
        }
      }
    }
  }

  post {
    success {
      echo "🎉 Deploy completado exitosamente para ${env.ENVIRONMENT}"
      echo "📊 Servicios disponibles:"
      echo "   - Backend: http://localhost:8081"
      sh '''
        docker ps --filter "name=urbantracker" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
      '''
    }
    failure {
      echo "💥 Error durante deploy en ${env.ENVIRONMENT}"
      echo "🔍 Logs del contenedor backend (últimas 50 líneas):"
      sh '''
        docker logs urbantracker-backend-${env.ENVIRONMENT} --tail 50 2>/dev/null || echo "Contenedor no encontrado"
      '''
    }
    always {
      script {
        if (env.ENVIRONMENT == 'develop') {
          echo "🧹 Limpieza automática para ambiente develop"
          sh """
            # Limpiar contenedores
            docker stop urbantracker-backend-${env.ENVIRONMENT} || true
            docker rm urbantracker-backend-${env.ENVIRONMENT} || true
            docker stop urbantracker-postgres-${env.ENVIRONMENT} || true
            docker rm urbantracker-postgres-${env.ENVIRONMENT} || true
            docker stop urbantracker-mosquitto-${env.ENVIRONMENT} || true
            docker rm urbantracker-mosquitto-${env.ENVIRONMENT} || true
            
            # Limpiar imagen
            docker rmi ${env.IMAGE_TAG} || true
            
            # Limpiar red
            docker network rm ${NETWORK_PREFIX}-${env.ENVIRONMENT} || true
            
            # Limpiar volúmenes huérfanos
            docker volume prune -f || true
          """
        } else {
          echo "⏸️ Ambiente ${env.ENVIRONMENT}: servicios permanecen activos para debugging"
        }
      }
    }
  }
}