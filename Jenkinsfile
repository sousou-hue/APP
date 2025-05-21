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
        withCredentials([sshUserPrivateKey(
          credentialsId: 'ansible-ssh-key',
          keyFileVariable: 'ANSIBLE_KEY'
        )]) {
          script {
            // Chemin absolu de l'APK généré
            def apkPath = "${env.WORKSPACE}/app/build/outputs/apk/debug/app-debug.apk"

            // Debug: afficher le chemin exact de la clé injectée
            echo "ANSIBLE_KEY file is at: ${ANSIBLE_KEY}"

            docker.image('soumiael774/my-ansible:latest').inside(
              "--entrypoint '' " +           // ignore l’ENTRYPOINT de l’image
              "-u root " +                   // exécute en root pour avoir accès à /tmp
              "-v ${ANSIBLE_KEY}:/tmp/id_rsa:ro" // monte UNIQUEMENT le fichier de clé
            ) {
              withEnv(["HOME=/tmp"]) {
                sh """
                  cd "${env.WORKSPACE}"

                  # Vérifier que /tmp/id_rsa est bien un fichier
                  ls -l /tmp/id_rsa
                  file /tmp/id_rsa

                  # Sécuriser la clé, au cas où
                  chmod 600 /tmp/id_rsa

                  # Exécuter le playbook avec la clé privée
                  ansible-playbook \\
                    -i inventory/k8s_hosts.ini \\
                    playbooks/deploy_apk.yml \\
                    --private-key=/tmp/id_rsa \\
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
