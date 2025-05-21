/* ------------------------------------------------------------------
   Jenkinsfile
   - Build APK (Android)
   - Archive artefact
   - Déployer via Ansible (clé privée UNIQUE – Secret file –, pas de ssh-agent)
   ------------------------------------------------------------------ */

pipeline {
  /* le conteneur agent Jenkins déjà lancé (android-agent) */
  agent { label 'android-build' }

  stages {

    /* ---------- compilation Android ---------- */
    stage('Build APK') {
      steps {
        sh 'chmod +x gradlew'
        sh './gradlew assembleDebug'
      }
    }

    /* ---------- archivage ---------- */
    stage('Archive APK') {
      steps {
        archiveArtifacts artifacts: '**/app-debug.apk', fingerprint: true
      }
    }

    /* ---------- déploiement Ansible ---------- */
    stage('Deploy with Ansible') {
      steps {

        /* 
         * 1) Jenkins écrit la clé privée (credential ID = ansible-private-key)
         *    dans un fichier temporaire dont le chemin est exposé
         *    dans la variable $KEY_FILE.
         */
        withCredentials([file(credentialsId: 'ansible-deploy-key',
                              variable: 'KEY_FILE')]) {

          script {
            /* chemin APK à transmettre au playbook */
            def apkPath = "${env.WORKSPACE}/app/build/outputs/apk/debug/app-debug.apk"

            /* 
             * 2) on lance le conteneur Ansible :
             *    - workspace monté en lecture seule
             *    - clé privée montée en /tmp/id_rsa (lecture seule)
             *    - pas de ssh-agent, pas de socket
             *    - l’utilisateur SSH sera lu dans l’inventory
             */
            sh """
              docker run --rm \\
                --network jenkins-net \\
                -v ${env.WORKSPACE}:${env.WORKSPACE}:ro \\
                -v \$KEY_FILE:/tmp/id_rsa:ro \\
                -e ANSIBLE_HOST_KEY_CHECKING=False \\
                soumiael774/my-ansible:latest \\
                ansible-playbook \\
                  -i ${env.WORKSPACE}/inventory/k8s_hosts.ini \\
                  ${env.WORKSPACE}/playbooks/deploy_apk.yml \\
                  --private-key /tmp/id_rsa \\
                  --extra-vars "apk_src=${apkPath}"
            """
          }
        }
      }
    }
  }
}
