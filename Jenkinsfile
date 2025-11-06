pipeline {
  agent any

  environment {
    REGISTRY = "registry.com"
    IMAGE    = "${REGISTRY}/premium/back"
  }

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
      agent { docker { image 'maven:3.9.4-eclipse-temurin-17' } }
      steps {
        dir('Backend') {
          sh 'mvn -B clean package -DskipTests'
        }
      }
    }

    stage('Run Backend') {
      agent { docker { image 'maven:3.9.4-eclipse-temurin-17' } }
      steps {
        dir('Backend') {
          script {
            def jar = sh(script: "ls target/*.jar | head -n 1", returnStdout: true).trim()
            echo "Jar encontrado: ${jar}"
            // Ejecuta en primer plano para que Jenkins muestre los logs durante el job.
            sh "java -jar ${jar}"
          }
        }
      }
    }
  }
}
