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
        credentialsId: 'github-jenkins'
      ]],
      extensions: [[$class: 'WipeWorkspace']]
    ])
  }
}


      stage('Build & Test') {
        steps {
          script {
            docker.image('maven:3.9-eclipse-temurin-17').inside {
              dir('Backend') {
                sh 'mvn -B -DskipTests=false test'
              }
            }
          }
        }
      }

      stage('Package') {
        steps {
          script {
            docker.image('maven:3.9-eclipse-temurin-17').inside {
              dir('Backend') {
                sh 'mvn -B -DskipTests package'
              }
            }
          }
        }
      }

      stage('Docker Build & Push') {
        steps {
          withCredentials([usernamePassword(credentialsId: 'docker-registry-creds', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
            sh """
              echo "$PASS" | docker login ${REGISTRY} -u "$USER" --password-stdin
              docker build -t ${IMAGE}:${GIT_COMMIT} Backend/
              docker push ${IMAGE}:${GIT_COMMIT}
            """
          }
        }
      }

      stage('Move Tag by branch') {
        steps {
          script {
            def envTag = (env.BRANCH_NAME == 'develop') ? 'dev'
                        : (env.BRANCH_NAME.startsWith('release/')) ? 'qa'
                        : (env.BRANCH_NAME == 'staging') ? 'staging'
                        : (env.BRANCH_NAME == 'main') ? 'prod'
                        : null

            if (envTag) {
              withCredentials([usernamePassword(credentialsId: 'docker-registry-creds', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                sh """
                  docker tag  ${IMAGE}:${GIT_COMMIT} ${IMAGE}:${envTag}
                  echo "$PASS" | docker login ${REGISTRY} -u "$USER" --password-stdin
                  docker push ${IMAGE}:${envTag}
                """
              }
            } else {
              echo "Rama ${env.BRANCH_NAME} sin tag móvil"
            }
          }
        }
      }
    }

    post {
      always {
        sh 'docker logout || true'
      }
    }
  }
