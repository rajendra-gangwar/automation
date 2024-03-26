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
/*                    failure {
                        slackSend failOnError:true message:"Build failed  - ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
                         }. 
                         */
                    success {
                        slackSend (channel: "build-alert",message: "Build deployed successfully - ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>))"
                    }

                }
            }
        }
    }

}
