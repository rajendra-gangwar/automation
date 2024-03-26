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
                    failure {
                        slackSend (channel: "build-alert", failOnError: true, message: "Build failed \nJOB Name: ${env.JOB_NAME}\n BUILD NUMBER: ${env.BUILD_NUMBER}\n Build URL: <${env.BUILD_URL}|Open>")
                         }
                    success {
                        slackSend (channel: "build-alert",message: "Build successfully \nJOB Name: ${env.JOB_NAME} \n BUILD NUMBER: ${env.BUILD_NUMBER}\n Build URL: <${env.BUILD_URL}|Open>")
                    }
            }
        }
    }

}
