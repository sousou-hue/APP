pipeline {
    agent {
        docker {
            image 'openjdk:17'
        }
    }
    stages {
        stage('Checkout Code') {
            steps {
                git 'https://github.com/TON_COMPTE/TON_REPO.git'
            }
        }
        stage('Build APK') {
            steps {
                sh 'chmod +x gradlew'       // Autoriser gradlew à s'exécuter
                sh './gradlew assembleDebug' // Construire l'APK
            }
        }
        stage('Archive APK') {
            steps {
                archiveArtifacts artifacts: '**/*.apk', fingerprint: true
            }
        }
    }
}
