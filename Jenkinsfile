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
        // 1) Démarre l’agent SSH Jenkins avec la clé sans passphrase
        sshagent (credentials: ['ansible-ssh-nopass']) {
          script {
            def apkPath = "${env.WORKSPACE}/app/build/outputs/apk/debug/app-debug.apk"

            // 2) Lance le conteneur Ansible en montant le socket SSH
            docker.image('soumiael774/my-ansible:latest').inside(
              // Monte le socket host dans /ssh-agent du conteneur
              "-u root " +
              "-v ${env.SSH_AUTH_SOCK}:/ssh-agent " +
              // Indique à SSH où trouver l’agent
              "-e SSH_AUTH_SOCK=/ssh-agent"
            ) {
              // 3) Exécute le playbook Ansible
              sh """
                cd "${env.WORKSPACE}"
                export ANSIBLE_HOST_KEY_CHECKING=False

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
