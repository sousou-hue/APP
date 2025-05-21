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
            // Chemin de l’APK
            def apkPath = "${env.WORKSPACE}/app/build/outputs/apk/debug/app-debug.apk"

            docker.image('soumiael774/my-ansible-agent:latest').inside(
              "--entrypoint '' " +             // Désactive ENTRYPOINT
              "-v \$WORKSPACE:/workspace " +   // Monte le workspace sous /workspace
              "-v ${ANSIBLE_KEY}:/root/.ssh/id_rsa:ro"
            ) {
              withEnv(["HOME=/tmp"]) {
                sh '''
                  # On se place dans /workspace où votre code et vos playbooks résident
                  cd /workspace

                  # (Optionnel) debug : lister pour vérifier
                  ls -R .

                  # Enfin, exécuter le playbook
                  ansible-playbook \
                    -i inventory/k8s_hosts.ini \
                    playbooks/deploy_apk.yml \
                    --extra-vars "apk_src='${apkPath}'"
                '''
              }
            }
          }
        }
      }
    }
  }
}
