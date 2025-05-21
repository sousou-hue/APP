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
        // Démarrage de l’agent SSH et chargement de la clé
        sshagent(['ansible-ssh-key']) {
          script {
            // 1) Chemin complet de l'APK dans le workspace
            def apkPath = "${env.WORKSPACE}/app/build/outputs/apk/debug/app-debug.apk"
            // 2) Répertoire parent du socket SSH
            def sockDir = env.SSH_AUTH_SOCK.substring(0, env.SSH_AUTH_SOCK.lastIndexOf('/'))

            // 3) Lancement du conteneur Ansible
            docker.image('soumiael774/my-ansible:latest').inside(
              "--entrypoint '' " +      // Ignore l’ENTRYPOINT de l’image
              "-u root " +              // Pour que /tmp et sockDir soient accessibles
              "-v ${sockDir}:${sockDir} " +  // Monte le dossier contenant SSH_AUTH_SOCK
              "-e SSH_AUTH_SOCK=${env.SSH_AUTH_SOCK}"
            ) {
              // 4) Environnement Ansible
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
