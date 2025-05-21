pipeline {
  agent { label 'android-build' }
  environment {
    APK_PATH = "app/build/outputs/apk/debug/app-debug.apk"
  }
  stages {
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
          sh '''
            ansible-playbook -i inventory/k8s_hosts.ini playbooks/deploy_apk.yml \
              --private-key $KEY_FILE \
              --extra-vars "apk_src=${APK_PATH}"
          '''
        }
      }
    }
  }
}
