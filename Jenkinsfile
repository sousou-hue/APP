pipeline {
  agent { label 'android-build' }

  environment {
    // Pour debug, on affiche toutes les variables
    DEBUG=true
  }

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

    stage('Check SSH_AUTH_SOCK on Agent') {
      steps {
        sshagent (credentials: ['ansible-ssh-nopass']) {
          sh '''
            echo "=== HOST DEBUG ==="
            echo "User: $(whoami)"
            echo "SSH_AUTH_SOCK=$SSH_AUTH_SOCK"
            ls -ld "$(dirname $SSH_AUTH_SOCK)" || echo ">> parent dir missing"
            ls -l "$SSH_AUTH_SOCK" || echo ">> socket missing"
            file "$SSH_AUTH_SOCK" || echo ">> not a socket"
          '''
        }
      }
    }

    stage('Debug SSH in Container') {
      steps {
        sshagent (credentials: ['ansible-ssh-key']) {
          script {
            // calcul du répertoire parent du socket
            def sockDir = env.SSH_AUTH_SOCK.substring(0, env.SSH_AUTH_SOCK.lastIndexOf('/'))
            docker.image('soumiael774/my-ansible:latest').inside(
              "--entrypoint '' " +
              "-u root " +
              "-v ${sockDir}:${sockDir}:ro " +
              "-e SSH_AUTH_SOCK=${env.SSH_AUTH_SOCK}"
            ) {
              sh '''
                echo "=== CONTAINER DEBUG ==="
                echo "User in container: $(whoami)"
                echo "SSH_AUTH_SOCK=$SSH_AUTH_SOCK"
                ls -ld "$(dirname $SSH_AUTH_SOCK)" || echo ">> parent dir missing"
                ls -l "$SSH_AUTH_SOCK" || echo ">> socket missing"
                # test file command
                if command -v file &>/dev/null; then
                  file "$SSH_AUTH_SOCK"
                else
                  echo ">> file not installed"
                fi

                # test ssh-add
                echo "-- identities via ssh-add:"
                if command -v ssh-add &>/dev/null; then
                  ssh-add -l || echo ">> no identities!"
                else
                  echo ">> ssh-add not found"
                fi

                # test raw ssh connectivity
                echo "-- raw SSH test (should print PONG):"
                if command -v ssh &>/dev/null; then
                  ssh -o StrictHostKeyChecking=no -o ForwardAgent=yes kali_lunix@192.168.0.9 echo PONG || echo ">> SSH auth failed"
                else
                  echo ">> ssh client missing"
                fi

                # test ansible piped dry-run
                echo "-- ansible ping test:"
                if command -v ansible &>/dev/null; then
                  ansible master -m ping -i inventory/k8s_hosts.ini --ssh-extra-args='-o ForwardAgent=yes' || echo ">> ansible ping failed"
                else
                  echo ">> ansible CLI missing"
                fi
              '''
            }
          }
        }
      }
    }

    stage('Deploy with Ansible (final)') {
      steps {
        sshagent (credentials: ['ansible-ssh-key']) {
          script {
            def apkPath = "${env.WORKSPACE}/app/build/outputs/apk/debug/app-debug.apk"
            def sockDir = env.SSH_AUTH_SOCK.substring(0, env.SSH_AUTH_SOCK.lastIndexOf('/'))
            docker.image('soumiael774/my-ansible:latest').inside(
              "--entrypoint '' " +
              "-u root " +
              "-v ${sockDir}:${sockDir}:ro " +
              "-e SSH_AUTH_SOCK=${env.SSH_AUTH_SOCK}"
            ) {
              sh """
                cd "${env.WORKSPACE}"
                ansible-playbook \\
                  -i inventory/k8s_hosts.ini \\
                  playbooks/deploy_apk.yml \\
                  --extra-vars "apk_src=${apkPath}" \\
                  --ssh-extra-args='-o ForwardAgent=yes'
              """
            }
          }
        }
      }
    }
  }
}
