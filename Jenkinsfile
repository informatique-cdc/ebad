pipeline {
    agent any
    tools {
        maven 'MAVEN3'
        jdk 'OJDK8'
    }
    environment {
        scannerHome = tool 'SONARSCANNER3.3';
        git = tool 'GIT';
    }
    stages {
        stage('Build') {
            steps {
                sh "mvn clean package -B -Dmaven.test.skip=true"
                archiveArtifacts artifacts: 'target/*.war', fingerprint: true
            }
        }
        stage('Unit Tests') {
            steps {
                sh "mvn verify --B org.jacoco:jacoco-maven-plugin:prepare-agent -Dmaven.test.failure.ignore=true -DtrimStackTrace=false"
                junit testResults: '**/target/surefire-reports/*.xml'
            }
        }
        stage('Javadoc') {
            steps {
                sh "mvn -B javadoc:javadoc"
            }
        }
        stage('SonarQube') {
            when{
                branch 'develop'
            }
            steps {
                withSonarQubeEnv('SONARQUBE') {
                    sh "mvn initialize sonar:sonar -Dproject.settings=sonar-project.properties -B"
                }
            }
        }

        stage('Release') {
            when {
                branch "develop"
            }
            steps {
                input "RELEASE EBAD?"
                sshagent(credentials: ['jenkins_git_ssh']) {
                    sh "mvn -B gitflow:release-start gitflow:release-finish -DpostReleaseGoals=deploy"
                }
            }
        }
    }
    post {
        always {
            deleteDir()
        }
    }
}
