pipeline {
  agent { label 'android-build' }

  environment {
    APK_PATH = "app/build/outputs/apk/debug/app-debug.apk"
    WORKDIR = "/workspace"
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
                -v \${PWD}:${env.WORKDIR}:ro \
                -v \$KEY_FILE:/tmp/id_rsa:ro \
                -e ANSIBLE_HOST_KEY_CHECKING=False \
                soumiael774/my-ansible:latest \
                ansible-playbook -i ${env.WORKDIR}/inventory/k8s_hosts.ini \
                  ${env.WORKDIR}/playbooks/deploy_apk.yml \
                  --private-key /tmp/id_rsa \
                  --extra-vars "apk_src=${env.WORKDIR}/${env.APK_PATH}"
            """
          }
        }
      }
    }
  }
}
