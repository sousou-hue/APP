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
            // Chemin absolu vers l'APK
            def apkPath = "${env.WORKSPACE}/app/build/outputs/apk/debug/app-debug.apk"

            docker.image('soumiael774/my-ansible:latest').inside(
              "--entrypoint '' " +                  // désactive ENTRYPOINT
              "-v ${ANSIBLE_KEY}:/root/.ssh/id_rsa:ro"  // clé privée
            ) {
              withEnv(["HOME=/tmp"]) {
                sh """
                  # On se place dans le workspace, tel qu’il est monté par Jenkins
                  cd "${env.WORKSPACE}"

                  # (Debug) Vérifions que le playbook existe et n’est pas vide
                  ls -l playbooks/deploy_apk.yml
                  head -n 20 playbooks/deploy_apk.yml

                  # Lancement du playbook
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
