pipeline {
  agent none                // plus d’agent par défaut

  stages {
    stage('Prepare local.properties') {
      agent { label 'android-build' }
      steps {
        sh '''
          cat > local.properties <<EOF
sdk.dir=/opt/android-sdk
EOF
        '''
      }
    }

    stage('Build APK') {
      agent { label 'android-build' }
      steps {
        sh 'chmod +x gradlew'
        sh './gradlew assembleDebug'
      }
    }

    stage('Archive APK') {
      agent { label 'android-build' }
      steps {
        archiveArtifacts artifacts: '**/app/build/outputs/apk/debug/*.apk', fingerprint: true
      }
    }

    stage('Deploy with Ansible') {
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
            mkdir -p /root/.ssh
            cp ${ANSIBLE_KEY} /root/.ssh/id_rsa
            chmod 600 /root/.ssh/id_rsa

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
