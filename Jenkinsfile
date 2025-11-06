pipeline {
  agent any

  environment {
    ENV_FILE = 'Backend/Devops/develop/.env.develop'
    COMPOSE_FILE = 'Backend/Devops/develop/docker-compose.yml'
    DOCKERFILE_PATH = 'Backend/Devops/develop/Dockerfile.app'
    DOCKER_IMAGE_NAME = 'backend-develop:latest'
    IMAGE_BASE = 'backend'              
    NETWORK_PREFIX = 'myproject-net'    
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
            # Rutas relativas dentro del repo (similares a tu ejemplo)
            echo "ENV_DIR=Backend/Devops/$ENVIRONMENT" >> env.properties
            echo "COMPOSE_FILE=Backend/Devops/$ENVIRONMENT/docker-compose.yml" >> env.properties
            echo "ENV_FILE=Backend/Devops/$ENVIRONMENT/.env" >> env.properties
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
    // 2️⃣ Compilar backend (maven inside container)
    // =====================================================
    stage('Compilar Backend') {
      steps {
        dir('Backend') {
          script {
            echo "📦 Compilando Backend con maven..."
            docker.image('maven:3.9.4-eclipse-temurin-17').inside {
              sh 'mvn -B clean package -DskipTests'
            }
          }
        }
      }
    }

    // =====================================================
    // 3️⃣ Construir imagen Docker del backend
    // =====================================================
    stage('Construir imagen Docker') {
      steps {
        dir('Backend') {
          script {
            echo "🐳 Construyendo imagen Docker del backend..."
            // tag con commit hash corto
            def commit = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
            env.IMAGE_TAG = "${IMAGE_BASE}:${env.ENVIRONMENT}-${commit}"
            // Copiar jar al devops dir para construir (Dockerfile.app espera app.jar)
            sh '''
              JARFILE=$(ls target/*.jar | head -n 1)
              if [ -z "$JARFILE" ]; then
                echo "No se encontró jar en Backend/target"
                exit 1
              fi
              mkdir -p Devops/develop
              cp ${JARFILE} Devops/develop/app.jar
            '''
            // construir
            sh "docker build --no-cache -t ${env.IMAGE_TAG} -f Devops/develop/Dockerfile.app Devops/develop"
            echo "Imagen creada: ${env.IMAGE_TAG}"
          }
        }
      }
    }

    // =====================================================
    // 4️⃣ Preparar red y base de datos
    // =====================================================
    stage('Preparar red y base de datos') {
      steps {
        script {
          def netName = "${NETWORK_PREFIX}-${env.ENVIRONMENT}"
          echo "🌐 Verificando red ${netName} ..."
          sh "docker network create ${netName} || echo '✅ Red ya existe'"

          // si existe un DB compose en la ruta esperada y el ambiente no es prod, levanta DB
          if (env.ENVIRONMENT == 'develop' || env.ENVIRONMENT == 'staging' || env.ENVIRONMENT == 'qa') {
            if (fileExists(env.COMPOSE_FILE)) {
              echo "🗄️ Base de datos definida en compose pero omitida (sin Docker Compose instalado)"
              echo "⚠️ Saltando setup de DB - puede ser necesario configurar manualmente"
            } else {
              echo "⚠️ No existe ${env.COMPOSE_FILE} — saltando DB local"
            }
          } else {
            echo "🛑 Ambiente prod detected: no levantamos DB local (asumir DB remota)"
          }
        }
      }
    }

    // =====================================================
    // 5️⃣ Desplegar backend (docker compose)
    // =====================================================
    stage('Desplegar Backend') {
      steps {
        script {
          if (env.ENVIRONMENT == 'prod') {
            echo "🚀 Despliegue remoto en producción (via SSH) - debes configurar credenciales"
            // este bloque es opcional: requiere credenciales (ssh) configuradas en Jenkins
            // withCredentials([...]) { sh """ ssh -i $SSH_KEY ... """ }
            echo "Implementa SSH deploy aquí si lo necesitas"
          } else {
            echo "🚀 Desplegando backend con Docker directo (${env.ENVIRONMENT})"
            // No usar docker compose, deployment directo con Docker
            sh '''
              # Verificar y liberar puerto 8080
              echo "🔍 Verificando puerto 8080..."
              
              # Detener contenedor anterior si existe
              docker stop urbantracker-backend-develop || true
              docker rm urbantracker-backend-develop || true
              
              # Esperar un momento para que el puerto se libere
              sleep 3
              
              # Ejecutar contenedor backend con la imagen
              echo "🚀 Iniciando contenedor backend..."
              docker run -d \\
                --name urbantracker-backend-develop \\
                --network ${NETWORK_PREFIX}-${env.ENVIRONMENT} \\
                -p 8080:8080 \\
                --restart unless-stopped \\
                ${env.IMAGE_TAG}
            '''
          }
        }
      }
    }

    // =====================================================
    // 6️⃣ Verificaciones rápidas
    // =====================================================
    stage('Healthchecks & Status') {
      steps {
        script {
          echo "🔎 Esperando servicios..."
          // esperar DB si está definido DB_HOST/DB_PORT en env file
          if (fileExists(env.ENV_FILE)) {
            def dbHost = sh(returnStdout: true, script: "grep -E '^DB_HOST=' ${env.ENV_FILE} | cut -d'=' -f2 || echo db").trim()
            def dbPort = sh(returnStdout: true, script: "grep -E '^DB_PORT=' ${env.ENV_FILE} | cut -d'=' -f2 || echo 5432").trim()
            echo "Comprobando postgres ${dbHost}:${dbPort}"
            sh """
              for i in \$(seq 1 40); do
                docker run --rm --network ${NETWORK_PREFIX}-${env.ENVIRONMENT} postgres:15 pg_isready -h ${dbHost} -p ${dbPort} && break || sleep 1
              done || echo "pg_isready timeout (la DB puede tardar)"
            """
          }

          // comprobar health del backend (intenta localhost:8080)
          sh """
            for i in \$(seq 1 30); do
              curl -sS --fail http://localhost:8080/actuator/health && break || sleep 2
            done || echo "Healthcheck app falló (revisa logs del contenedor backend)"
          """
        }
      }
    }
  }

  post {
    success {
      echo "🎉 Deploy completado para ${env.ENVIRONMENT}"
    }
    failure {
      echo "💥 Error durante deploy en ${env.ENVIRONMENT}"
    }
    always {
      script {
        // si quieres limpiar solo en develop
        if (env.ENVIRONMENT == 'develop') {
          echo "🧹 Limpieza: contenedores Docker directos (develop)"
          // Limpieza de contenedores Docker directos
          sh """
            # Detener y remover contenedor backend
            docker stop urbantracker-backend-develop || true
            docker rm urbantracker-backend-develop || true
            # Remover imagen
            docker rmi ${env.IMAGE_TAG} || true
            # Remover red
            docker network rm ${NETWORK_PREFIX}-${env.ENVIRONMENT} || true
          """
        } else {
          echo "No se realiza down automático para ${env.ENVIRONMENT}"
        }
      }
    }
  }
}
