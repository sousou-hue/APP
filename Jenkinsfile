pipeline {
  agent { label 'android-build' }

  environment {
    APK_PATH = "app/build/outputs/apk/debug/app-debug.apk"
  }

  stages {
    stage('Prepare local.properties') {
      steps {
        sh '''
          cat > local.properties <<EOF
sdk.dir=/opt/android-sdk
EOF
        '''
      }
    }

    stage('Build APK') {
      steps {
        sh 'chmod +x gradlew'
        sh './gradlew assembleDebug'
      }
    }

    stage('Archive APK') {
      steps {
        archiveArtifacts artifacts: '**/app-debug.apk', fingerprint: true
      }
    }

    stage('Deploy with Ansible') {
      steps {
        withCredentials([file(credentialsId: 'ansible-deploy-key', variable: 'KEY_FILE')]) {
          script {
            sh """
              docker run --rm --network jenkins-net \
                -v ${env.WORKSPACE}:${env.WORKSPACE}:ro \
                -v \$KEY_FILE:/tmp/id_rsa:ro \
                -e ANSIBLE_HOST_KEY_CHECKING=False \
                soumiael774/my-ansible:latest \
                  -i ${env.WORKSPACE}/inventory/k8s_hosts.ini \
                  ${env.WORKSPACE}/playbooks/deploy_apk.yml \
                  --private-key /tmp/id_rsa \
                  --extra-vars "apk_src=${env.WORKSPACE}/${env.APK_PATH}"
            """
          }
        }
      }
    }
  }
}
