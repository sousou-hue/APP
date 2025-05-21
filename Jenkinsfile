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

    // ----------------------------
    // Stage de debug du socket SSH
    // ----------------------------
    stage('Check SSH_AUTH_SOCK') {
      steps {
        sshagent (credentials: ['ansible-ssh-key']) {
          sh '''
            echo "=== DEBUG SSH_AGENT ==="
            echo "SSH_AUTH_SOCK = $SSH_AUTH_SOCK"
            ls -l "$SSH_AUTH_SOCK" || echo ">> Socket file not found"
            file "$SSH_AUTH_SOCK" || echo ">> Not a socket"
          '''
        }
      }
    }

    // ----------------------------
    // Stage de déploiement avec Ansible
    // ----------------------------
    stage('Deploy with Ansible') {
      steps {
        sshagent (credentials: ['ansible-ssh-key']) {
          script {
            // Chemin de l'APK dans le workspace
            def apkPath = "${env.WORKSPACE}/app/build/outputs/apk/debug/app-debug.apk"

            // Lancement du conteneur Ansible en montant directement le socket
            docker.image('soumiael774/my-ansible:latest').inside(
              "--entrypoint '' " +                          // Désactive l’ENTRYPOINT de l’image
              "-u root " +                                  // Exécuter en root pour accéder au socket
              "-v ${env.SSH_AUTH_SOCK}:/ssh-agent.sock:ro " + // Monte le fichier socket
              "-e SSH_AUTH_SOCK=/ssh-agent.sock"              // Pointe SSH dessus
            ) {
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
