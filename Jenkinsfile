pipeline {
  agent { label 'android-build' }

  environment {
    ANSIBLE_INVENTORY = 'inventory.ini'
    ANSIBLE_PLAYBOOK  = 'deploy-k8s.yml'
  }

  stages {
    stage('Build APK') {
      steps {
        sh 'chmod +x gradlew && ./gradlew assembleDebug'
      }
    }

    stage('Deploy to Kubernetes') {
      steps {
        withEnv(["WORKSPACE=${env.WORKSPACE}"]) {
          sh '''
            ansible-playbook -i ${ANSIBLE_INVENTORY} ${ANSIBLE_PLAYBOOK}
          '''
        }
      }
    }

    stage('Publish URL') {
      steps {
        echo 'APK téléchargeable sur http://192.168.1.25:30081/app-debug.apk'
      }
    }
  }
}
