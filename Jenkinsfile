pipeline {
  agent { label 'android-build' }

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
        // 1) Démarre l’agent SSH avec la clé **sans passphrase**
        sshagent (credentials: ['ansible-ssh-key']) {
          script {
            def apkPath = "${env.WORKSPACE}/app/build/outputs/apk/debug/app-debug.apk"

            // 2) Monte LE FICHIER socket directement sous un nom fixe
            docker.image('soumiael774/my-ansible:latest').inside(
              "--entrypoint '' " +
              "-u root " +
              "-v ${env.SSH_AUTH_SOCK}:/ssh-agent.sock:ro " +
              "-e SSH_AUTH_SOCK=/ssh-agent.sock"
            ) {
              // 3) Exécute Ansible
              sh """
                cd "${env.WORKSPACE}"
                export ANSIBLE_HOST_KEY_CHECKING=False

                ansible-playbook \\
                  -i inventory/k8s_hosts.ini \\
                  playbooks/deploy_apk.yml \\
                  --extra-vars "apk_src=${apkPath}"
              """
            }
          }
        }
      }
    }
  }
}
