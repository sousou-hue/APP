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
            def apkPath = "${env.WORKSPACE}/app/build/outputs/apk/debug/app-debug.apk"

            docker.image('soumiael774/my-ansible:latest').inside(
              "--entrypoint '' " +
              "-u root " +
              "-v ${ANSIBLE_KEY}:/root/.ssh/id_rsa"
            ) {
              withEnv(["HOME=/tmp"]) {
                sh """
                  cd "${env.WORKSPACE}"

                  # Copie la clé vers un emplacement où chmod fonctionne
                  cp /root/.ssh/id_rsa /tmp/id_rsa
                  chmod 600 /tmp/id_rsa

                  # (Debug) Vérifiez que la clé a les bonnes permissions
                  ls -l /tmp/id_rsa

                  # Lancement du playbook en pointant sur la clé située en /tmp
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
