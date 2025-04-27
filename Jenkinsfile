pipeline {
    agent {
        docker {
            image 'openjdk:17'
        }
    }
    stages {
        stage('Checkout Code') {
            steps {
                git 'https://github.com/sousou-hue/APP'
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
