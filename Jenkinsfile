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
        // 1) On démarre un agent SSH dans Jenkins (avec votre credential SSH)
        sshagent(['ansible-ssh-key']) {
          script {
            def apkPath = "${env.WORKSPACE}/app/build/outputs/apk/debug/app-debug.apk"

            // 2) On forwarde le socket SSH dans le conteneur
            docker.image('soumiael774/my-ansible:latest').inside(
              "--entrypoint '' " +
              "-u root " +
              "-v ${env.SSH_AUTH_SOCK}:/ssh-agent " +
              "-e SSH_AUTH_SOCK=/ssh-agent"
            ) {
              // 3) On redirige les tmp d'Ansible vers /tmp
              withEnv(["HOME=/tmp"]) {
                sh """
                  cd "${env.WORKSPACE}"
                  # On désactive la vérification stricte de la clé hôte
                  export ANSIBLE_HOST_KEY_CHECKING=False

                  # On lance le playbook : Ansible utilisera le SSH agent
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
