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
            // Le chemin absolu vers l'APK dans le workspace
            def apkPath = "${env.WORKSPACE}/app/build/outputs/apk/debug/app-debug.apk"

            docker.image('soumiael774/my-ansible-agent:latest').inside(
              "--entrypoint '' " +                 // désactive l’ENTRYPOINT de l’image
              "-v ${ANSIBLE_KEY}:/root/.ssh/id_rsa:ro"  // monte la clé privée
            ) {
              // Forcer home pour éviter les problèmes de permission sur ~/.ansible
              withEnv(["HOME=/tmp"]) {
                // On est déjà dans le workspace monté (même chemin que Jenkins)
                sh """
                  # (Optionnel) Vérifions que nos fichiers sont bien là
                  pwd
                  ls -R .

                  # On lance le playbook
                  ansible-playbook -i inventory/k8s_hosts.ini playbooks/deploy_apk.yml \\
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
