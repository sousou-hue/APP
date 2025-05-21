pipeline {
  agent { label 'android-build' }
  environment {
    APK_PATH = "app/build/outputs/apk/debug/app-debug.apk"
  }
  stages {
    stage('Install Ansible') {
      steps {
        sh '''
          if ! command -v ansible-playbook >/dev/null 2>&1; then
            sudo apt-get update
            sudo apt-get install -y python3-pip sshpass
            pip3 install --user ansible
            export PATH=$PATH:~/.local/bin
          fi
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
          sh '''
            export PATH=$PATH:~/.local/bin
            ansible-playbook -i inventory/k8s_hosts.ini playbooks/deploy_apk.yml \
              --private-key $KEY_FILE \
              --extra-vars "apk_src=${APK_PATH}"
          '''
        }
      }
    }
  }
}
