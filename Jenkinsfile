pipeline {
  agent { label 'android-build' }
  environment {
    // Pour que Ansible ne fasse pas de vérification de hostkey
    ANSIBLE_HOST_KEY_CHECKING = 'False'
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
        withCredentials([
          file(credentialsId: 'ansible-deploy-key', variable: 'KEY_FILE')
        ]) {
          script {
            def apkPath = "${env.WORKSPACE}/app/build/outputs/apk/debug/app-debug.apk"
            sh """
              docker run --rm \\
                --network jenkins-net \\
                -v ${env.WORKSPACE}:${env.WORKSPACE}:ro \\
                -v \$KEY_FILE:/tmp/id_rsa:ro \\
                -e ANSIBLE_HOST_KEY_CHECKING=False \\
                soumiael774/my-ansible:latest \\
                ansible-playbook \\
                  -i ${env.WORKSPACE}/inventory/k8s_hosts.ini \\
                  ${env.WORKSPACE}/playbooks/deploy_apk.yml \\
                  --private-key /tmp/id_rsa \\
                  --extra-vars "apk_src=${apkPath}"
            """
          }
        }
      }
    }
  }
}
