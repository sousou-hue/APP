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
        archiveArtifacts artifacts: '**/app/build/outputs/apk/debug/*.apk', fingerprint: true
      }
    }

    stage('Deploy with Ansible') {
      // Ce stage tourne dans un conteneur Docker Ansible, pas besoin de Docker sur android-build
      agent {
        docker {
          image 'soumiael774/my-ansible-agent:latest'
          args  '-v $WORKSPACE:/workspace'
        }
      }
      steps {
        withCredentials([sshUserPrivateKey(
          credentialsId: 'ansible-ssh-key',
          keyFileVariable: 'ANSIBLE_KEY'
        )]) {
          sh '''
            # Préparer la clé SSH pour Ansible
            mkdir -p /root/.ssh
            cp ${ANSIBLE_KEY} /root/.ssh/id_rsa
            chmod 600 /root/.ssh/id_rsa

            # Exécuter le playbook
            cd /workspace
            ansible-playbook \
              -i inventory/k8s_hosts.ini \
              playbooks/deploy_apk.yml \
              --extra-vars "apk_src=${WORKSPACE}/app/build/outputs/apk/debug/app-debug.apk"
          '''
        }
      }
    }
  }
}
