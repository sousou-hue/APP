pipeline {
  agent { label 'android-build' }

  environment {
    APK_PATH = "${env.WORKSPACE}/app/build/outputs/apk/debug/app-debug.apk"
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
        archiveArtifacts artifacts: '**/*.apk', fingerprint: true
      }
    }

    stage('Deploy with Ansible') {
      steps {
        withCredentials([
          sshUserPrivateKey(credentialsId: 'ansible-deploy-key', keyFileVariable: 'KEY_FILE')
        ]) {
          sh """
            ansible-playbook -i inventory/k8s_hosts.ini playbooks/deploy_apk.yml \
              --private-key $KEY_FILE \
              --extra-vars "apk_src=${APK_PATH}"
          """
        }
      }
    }
  }
}
