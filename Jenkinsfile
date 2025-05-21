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
        sshagent(['ansible-ssh-key']) {
          script {
            // 1) Chemin absolu de l'APK
            def apkPath = "${env.WORKSPACE}/app/build/outputs/apk/debug/app-debug.apk"
            // 2) Récupérer le dossier parent du socket SSH
            def sockDir = env.SSH_AUTH_SOCK.substring(0, env.SSH_AUTH_SOCK.lastIndexOf('/'))

            // 3) Lancer le conteneur Ansible en forwardant le dossier du socket et la variable
            docker.image('soumiael774/my-ansible:latest').inside(
              "--entrypoint '' " +
              "-u root " +
              "-v ${sockDir}:${sockDir} " +
              "-e SSH_AUTH_SOCK=${env.SSH_AUTH_SOCK}"
            ) {
              // 4) Forcer HOME pour les tmp d'Ansible et désactiver le host-key-checking
              withEnv(["HOME=/tmp", "ANSIBLE_HOST_KEY_CHECKING=False"]) {
                sh """
                  cd "${env.WORKSPACE}"
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
}
