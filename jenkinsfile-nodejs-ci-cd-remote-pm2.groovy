pipeline {
  agent any
  environment {
    NODEJS_USER = "root"
    NODEJS_PASSWORD = "admin"
    NODEJS_IP = "172.31.38.87"
    APP_NAME = "sample_node-project"
    DEPLOY_DIR = "/${NODE_USER}/${APP_NAME}"
  }
stages {
  stage ('Checkout') {
    steps {
      git branch: 'main', url: 'https://github.com/ghodesuraj/project.git'
    }
  }
  stage ('Install Dependencies') {
    steps {
      script {
          // Install Node.js dependencies locally
          sh 'npm install'
      }
    }
  }
  stage ('Build & Package') {
    steps {
      script {
        // Optional: Run build script if the project has one
          sh 'npm run build || echo "No build script found"'
      }
    }
  }
  stage ('Deploy to Server') {
    steps {
      script {
        // Copy project files to the remote server
          sh """
          sshpass -p '${NODEJS_PASSWORD}' scp -r ${WORKSPACE}/* ${NODEJS_USER}@${NODEJS_IP}:${DEPLOY_DIR}/
          """
          // Restarting the application on the remote server using PM2
          sh """
          sshpass -p '${NODEJS_PASSWORD}' ssh -o StrictHostKeyChecking=no ${NODEJS_USER}@${NODEJS_IP} '
            cd ${DEPLOY_DIR}
            pm2 stop ${APP_NAME} || true &&
            pm2 start app.js --name ${APP_NAME}
            '
          """
      }
    }
  }
}
  post {
      success {
            echo 'Node.js Build & Deployment Successful.'
      }
      failure {
              echo 'Build or Deployment Failed.'
      }
  }
}
