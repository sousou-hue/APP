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
            def apkPath = "${env.WORKSPACE}/app/build/outputs/apk/debug/app-debug.apk"
            // 1) On récupère la valeur exacte de SSH_AUTH_SOCK
            echo "SSH_AUTH_SOCK is at: ${env.SSH_AUTH_SOCK}"

            // 2) On monte **le dossier contenant** le socket, 
            //    et non juste le fichier, au même endroit.
            def sockDir = env.SSH_AUTH_SOCK.substring(0, env.SSH_AUTH_SOCK.lastIndexOf('/'))

            docker.image('soumiael774/my-ansible:latest').inside(
              "--entrypoint '' " +
              "-u root " +
              // Montre le répertoire entier
              "-v ${sockDir}:${sockDir} " +
              // On n'a pas besoin de -e, l'agent SSH injecte déjà SSH_AUTH_SOCK
              ""
            ) {
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
