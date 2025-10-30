pipeline {
  agent any
  environment {
    REGISTRY = "registry.com"
    IMAGE    = "${REGISTRY}/premium/back"
  }
  stages {
    stage('Checkout') {
      steps { checkout scm }
    }
    stage('Build & Test') {
      steps {
        sh 'mvn -B -DskipTests=false test'
      }
    }
    stage('Package') {
      steps {
        sh 'mvn -B -DskipTests package'
      }
    }
    stage('Docker Build & Push') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'docker-registry-creds', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
          sh """
            echo "$PASS" | docker login ${REGISTRY} -u "$USER" --password-stdin
            docker build -t ${IMAGE}:${GIT_COMMIT} .
            docker push ${IMAGE}:${GIT_COMMIT}
          """
        }
      }
    }
    stage('Move Tag by branch') {
      steps {
        script {
          // Etiqueta móvil por rama→ambiente
          def envTag = (env.BRANCH_NAME == 'develop') ? 'dev'
                     : (env.BRANCH_NAME.startsWith('release/')) ? 'qa'
                     : (env.BRANCH_NAME == 'staging') ? 'staging'
                     : (env.BRANCH_NAME == 'main') ? 'prod'
                     : null
          if (envTag) {
            sh """
              docker tag  ${IMAGE}:${GIT_COMMIT} ${IMAGE}:${envTag}
              echo "$PASS" | docker login ${REGISTRY} -u "$USER" --password-stdin
              docker push ${IMAGE}:${envTag}
            """
          } else {
            echo "Rama ${env.BRANCH_NAME} sin tag móvil"
          }
        }
      }
    }
  }
  post {
    always { sh 'docker logout || true' }
  }
}
