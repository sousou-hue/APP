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

    stage('Debug SSH in Container (all checks)') {
      steps {
        sshagent (credentials: ['ansible-ssh-key']) {
          script {
            def sockDir = env.SSH_AUTH_SOCK.substring(0, env.SSH_AUTH_SOCK.lastIndexOf('/'))
            def sockFile = env.SSH_AUTH_SOCK

            docker.image('soumiael774/my-ansible:latest').inside(
              "--entrypoint '' " +
              "-u root " +
              "-v ${sockDir}:${sockDir}:ro " +
              "-e SSH_AUTH_SOCK=${sockFile}"
            ) {
              sh """
                echo "==== DEBUG SSH SOCKET ===="
                echo "HOSTNAME       : \$(hostname)"
                echo "USER           : \$(whoami)"
                echo "PWD            : \$PWD"
                echo "SSH_AUTH_SOCK  : \$SSH_AUTH_SOCK"
                echo "SSH_AUTH_SOCK (exists?):"; [ -S "\$SSH_AUTH_SOCK" ] && echo OK || echo NOK
                echo "ls -l du dossier socket :"
                ls -l ${sockDir} || echo "ls failed"

                echo ""
                echo "==== whoami, id ===="
                whoami
                id

                echo ""
                echo "==== file/sshd/ssh ===="
                command -v ssh || echo "ssh not found"
                ssh -V || echo "No ssh version"

                echo ""
                echo "==== ssh-add -l (identité présente ?) ===="
                ssh-add -l || echo ">> Aucune identité SSH trouvée !"

                echo ""
                echo "==== Test brute SSH (echo PONG) ===="
                ssh -vvv -o StrictHostKeyChecking=no -o ForwardAgent=yes kali_lunix@192.168.0.9 echo PONG || echo ">> Auth SSH échouée !"

                echo ""
                echo "==== Ansible version ===="
                ansible --version || echo "No ansible"

                echo ""
                echo "==== Test ansible simple (ping) ===="
                echo "[target]" > /tmp/test_hosts
                echo "192.168.0.9 ansible_user=kali_lunix" >> /tmp/test_hosts
                ansible -i /tmp/test_hosts all -m ping || echo ">> Ansible ping failed !"

                echo ""
                echo "==== SSH agent process dans conteneur ===="
                ps aux | grep [s]sh-agent || echo "Pas de ssh-agent dans conteneur (normal : forward via socket)"

                echo ""
                echo "==== Fin des tests ===="
              """
            }
          }
        }
      }
    }
  }
}
