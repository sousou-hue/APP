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
            // Chemin fixe de l'APK généré
            def apkPath = "${env.WORKSPACE}/app/build/outputs/apk/debug/app-debug.apk"

            // Lancement du conteneur Ansible
            docker.image('soumiael774/my-ansible-agent:latest').inside(
              "--entrypoint '' " +                  // Désactive l'ENTRYPOINT de l'image
              "-v \$WORKSPACE:/workspace " +        // Monte le workspace
              "-v ${ANSIBLE_KEY}:/root/.ssh/id_rsa:ro " +  // Monte la clé privée
              "-w /workspace"                       // Définit le répertoire de travail
            ) {
              // On force HOME pour que les tmp d'Ansible se créent dans /tmp
              withEnv(["HOME=/tmp"]) {
                sh """
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
