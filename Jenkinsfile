pipeline {
  agent any

  stages {
    stage('Checkout') {
      steps {
        checkout([$class: 'GitSCM',
          branches: [[name: '*/develop']],
          userRemoteConfigs: [[
            url: 'https://github.com/brayan-carvajal/urbantracker-api.git',
            credentialsId: 'docker-registry-creds'
          ]],
          extensions: [[$class: 'WipeWorkspace']]
        ])
      }
    }

    stage('Build Backend') {
      steps {
        dir('Backend') {
          docker.image('maven:3.9.4-eclipse-temurin-17').inside {
            sh 'mvn clean package -DskipTests'
          }
        }
      }
    }

    stage('Run Backend') {
      steps {
        dir('Backend') {
          script {
            // Busca el JAR generado automáticamente
            def jarFile = sh(script: "ls target/*.jar | head -n 1", returnStdout: true).trim()
            echo "Ejecutando aplicación: ${jarFile}"

            // Ejecuta el backend dentro del contenedor Maven
            docker.image('maven:3.9.4-eclipse-temurin-17').inside {
              sh "java -jar ${jarFile} & sleep 10 && curl -f http://localhost:8080 || true"
            }
          }
        }
      }
    }
  }
}
