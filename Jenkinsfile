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
            // 1) Repérer le chemin de l'APK généré
            def apkPath = findFiles(glob: '**/app-debug.apk')[0].path

            // 2) Lancer l'image Docker Ansible, en montant :
            //    - le workspace (code, inventory, playbooks)
            //    - la clé privée Jenkins
            docker.image('votre-user/ansible-k8s:latest').inside(
              "-v \$WORKSPACE:/workspace " +
              "-v ${ANSIBLE_KEY}:/root/.ssh/id_rsa:ro"
            ) {
              // 3) Exécuter le playbook avec la variable apk_src
              sh """
                cd /workspace
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
