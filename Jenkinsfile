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

    stage('Debug SSH in Container') {
      steps {
        sshagent (credentials: ['ansible-ssh-key']) {
          script {
            // 1) calcule le dossier parent du socket
            def sockDir = env.SSH_AUTH_SOCK.substring(0, env.SSH_AUTH_SOCK.lastIndexOf('/'))

            docker.image('soumiael774/my-ansible:latest').inside(
              "--entrypoint '' " +
              "-u root " +
              // 2) monte le dossier parent exactement au même endroit
              "-v ${sockDir}:${sockDir}:ro " +
              // 3) on n’a pas besoin de changer SSH_AUTH_SOCK
              "-e SSH_AUTH_SOCK=${env.SSH_AUTH_SOCK}"
            ) {
              sh '''
                echo "== SSH identities =="
                ssh-add -l || echo ">> Aucune identité SSH trouvée !"

                echo "== Test connexion brute =="
                ssh -o StrictHostKeyChecking=no -o ForwardAgent=yes kali_lunix@192.168.0.9 echo PONG || echo ">> Auth SSH échouée !"
              '''
            }
          }
        }
      }
    }

    stage('Deploy with Ansible') {
      steps {
        sshagent (credentials: ['ansible-ssh-key']) {
          script {
            def apkPath = "${env.WORKSPACE}/app/build/outputs/apk/debug/app-debug.apk"
            def sockDir = env.SSH_AUTH_SOCK.substring(0, env.SSH_AUTH_SOCK.lastIndexOf('/'))

            docker.image('soumiael774/my-ansible:latest').inside(
              "--entrypoint '' " +
              "-u root " +
              // monte le répertoire parent du socket
              "-v ${sockDir}:${sockDir}:ro " +
              "-e SSH_AUTH_SOCK=${env.SSH_AUTH_SOCK}"
            ) {
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
