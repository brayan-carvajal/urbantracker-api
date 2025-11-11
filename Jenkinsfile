pipeline {
  agent any

  environment {
    IMAGE_BASE = 'backend'
    NETWORK_PREFIX = 'myproject-net'
  }

  stages {
    
    stage('Permisos workspace') {
      steps {
        sh '''
          chmod -R 777 $WORKSPACE || true
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
            ENVIRONMENT=$(grep -E '^ENVIRONMENT=' .env | cut -d'=' -f2 | tr -d '\\r\\n')
            echo "ENVIRONMENT=$ENVIRONMENT" > env.properties
            echo "ENV_DIR=Backend/Devops/$ENVIRONMENT" >> env.properties
            echo "COMPOSE_FILE=Backend/Devops/$ENVIRONMENT/docker-compose.yml" >> env.properties
          '''
          def props = readProperties file: 'env.properties'
          env.ENVIRONMENT = props['ENVIRONMENT']

          echo "✅ Entorno detectado: ${env.ENVIRONMENT}"
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
            
            def commit = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
            env.IMAGE_TAG = "${IMAGE_BASE}:${env.ENVIRONMENT}-${commit}"
            
            // Verificar que el jar existe
            sh '''
              JARFILE=$(ls target/*.jar 2>/dev/null | head -n 1)
              if [ -z "$JARFILE" ]; then
                echo "❌ No se encontró jar en Backend/target"
                exit 1
              fi
              echo "✅ JAR encontrado: $JARFILE"
              
              mkdir -p Devops/develop
              cp ${JARFILE} Devops/develop/app.jar
            '''
            
            // construir imagen
            sh """
              docker build --no-cache -t ${env.IMAGE_TAG} -f Devops/develop/Dockerfile.app Devops/develop
            """
            echo "✅ Imagen creada: ${env.IMAGE_TAG}"
          }
        }
      }
    }

    // =====================================================
    // 5️⃣ Preparar red y base de datos
    // =====================================================
    stage('Preparar servicios') {
      steps {
        script {
          def netName = "${NETWORK_PREFIX}-${env.ENVIRONMENT}"
          echo "🌐 Creando red ${netName} ..."
          sh "docker network create ${netName} || echo '✅ Red ya existe'"

          if (env.ENVIRONMENT != 'prod') {
            echo "🗄️ Iniciando PostgreSQL..."
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

            echo "📡 Iniciando Mosquitto MQTT..."
            sh """
              docker run -d \\
                --name urbantracker-mosquitto-${env.ENVIRONMENT} \\
                --network ${netName} \\
                -p 1883:1883 \\
                -p 9001:9001 \\
                --restart unless-stopped \\
                eclipse-mosquitto:2
            """
          } else {
            echo "🛑 Ambiente prod: saltando servicios locales"
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
            echo "🚀 Despliegue remoto en producción"
          } else {
            script {
              echo "🚀 Desplegando backend local (${env.ENVIRONMENT})"
              
              def networkName = "${NETWORK_PREFIX}-${env.ENVIRONMENT}"
              def containerName = "urbantracker-backend-${env.ENVIRONMENT}"
              
              sh """
                # Detener contenedor anterior si existe
                docker stop ${containerName} || true
                docker rm ${containerName} || true
                
                sleep 3
                
                # Ejecutar contenedor backend
                docker run -d \\
                  --name ${containerName} \\
                  --network ${networkName} \\
                  -p 8081:8080 \\
                  -e SPRING_PROFILES_ACTIVE=${env.ENVIRONMENT} \\
                  --restart unless-stopped \\
                  ${env.IMAGE_TAG}
                
                echo "✅ Contenedor backend iniciado"
              """
            }
          }
        }
      }
    }

    // =====================================================
    // 7️⃣ Health checks simples
    // =====================================================
    stage('Health Checks') {
      steps {
        script {
          echo "🔎 Esperando backend..."
          
          // Esperar un poco antes de hacer health check
          sh '''
            sleep 15
            echo "⏱️ Esperando 15 segundos para que el backend inicie..."
          '''
          
          // Health check simple
          sh '''
            echo "🔍 Verificando health del backend..."
            curl -sS --fail --connect-timeout 10 --max-time 30 http://localhost:8081/actuator/health || {
              echo "⚠️ Health check falló"
              echo "📋 Logs del contenedor:"
              docker logs urbantracker-backend-develop || true
              exit 1
            }
            echo "✅ Health check exitoso"
          '''
        }
      }
    }
  }

  post {
    success {
      echo "🎉 Deploy completado para ${env.ENVIRONMENT}"
      echo "📊 Servicios disponibles:"
      echo "   - Backend: http://localhost:8081"
    }
    failure {
      echo "💥 Error durante deploy"
      sh '''
        docker logs urbantracker-backend-develop --tail 20 2>/dev/null || true
      '''
    }
    always {
      script {
        if (env.ENVIRONMENT == 'develop') {
          echo "🧹 Limpiando contenedores..."
          sh """
            docker stop urbantracker-backend-${env.ENVIRONMENT} || true
            docker rm urbantracker-backend-${env.ENVIRONMENT} || true
            docker stop urbantracker-postgres-${env.ENVIRONMENT} || true
            docker rm urbantracker-postgres-${env.ENVIRONMENT} || true
            docker stop urbantracker-mosquitto-${env.ENVIRONMENT} || true
            docker rm urbantracker-mosquitto-${env.ENVIRONMENT} || true
            docker network rm ${NETWORK_PREFIX}-${env.ENVIRONMENT} || true
          """
        }
      }
    }
  }
}