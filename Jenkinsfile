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
            def sockFile = env.SSH_AUTH_SOCK
            def sockDir = sockFile.substring(0, sockFile.lastIndexOf('/'))
            docker.image('soumiael774/my-ansible:latest').inside(
              "--entrypoint '' " +
              "-u root " +
              "-v ${sockDir}:${sockDir} " +
              "-e SSH_AUTH_SOCK=${sockFile} "
            ) {
              sh '''
                echo "==== DEBUG SSH SOCKET ===="
                echo SSH_AUTH_SOCK  : $SSH_AUTH_SOCK
                echo -n "SSH_AUTH_SOCK (exists?): "
                [ -S "$SSH_AUTH_SOCK" ] && echo OK || echo NOK
                echo "ls -l du dossier socket :"
                ls -l $(dirname $SSH_AUTH_SOCK) || echo "ls failed"
                echo "ssh-add -l (identité présente ?):"
                ssh-add -l || echo ">> Aucune identité SSH trouvée !"
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
            def sockFile = env.SSH_AUTH_SOCK
            def sockDir = sockFile.substring(0, sockFile.lastIndexOf('/'))
            def apkPath = "${env.WORKSPACE}/app/build/outputs/apk/debug/app-debug.apk"

            docker.image('soumiael774/my-ansible:latest').inside(
              "--entrypoint '' " +
              "-u root " +
              "-v ${sockDir}:${sockDir} " +
              "-e SSH_AUTH_SOCK=${sockFile} "
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
