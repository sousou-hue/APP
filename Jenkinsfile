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
            // chemin absolu de l'APK
            def apkPath = "${env.WORKSPACE}/app/build/outputs/apk/debug/app-debug.apk"

            docker.image('soumiael774/my-ansible:latest').inside(
              "--entrypoint '' " +          // ignore l’ENTRYPOINT de l’image
              "-u root " +                  // exécute en root pour accéder à /tmp
              "-v ${ANSIBLE_KEY}:/tmp/id_rsa" // monte la clé dans /tmp/id_rsa
            ) {
              withEnv(["HOME=/tmp"]) {
                sh """
                  cd "${env.WORKSPACE}"

                  # sécurise la clé pour SSH
                  chmod 600 /tmp/id_rsa

                  # debug : vérifie la présence et les droits
                  ls -l /tmp/id_rsa

                  # lance le playbook en utilisant la clé et la variable apk_src
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
