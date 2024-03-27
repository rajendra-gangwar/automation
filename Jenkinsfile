#!groovy​
import groovy.json.JsonSlurper
@Library('BuildUtils') _

pipeline {
    agent any
    stages {
        stage('Job DSL') {
            steps {
                script {
                    jobDsl targets: ['jobs/*.groovy'].join('\n'),
                        removedJobAction: 'DELETE',
                        removedViewAction: 'DELETE',
                        additionalParameters: [workspace_dir: "${env.WORKSPACE}", jenkins_url: "${env.JENKINS_URL}"]
                }
            }
            post {
                    always {
                        script {
                             slackNotifier(currentBuild.result)
                         }
                    }
            }
        }
    }

}
