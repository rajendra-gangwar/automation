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
                            def buildStatus = currentBuild.currentResult ?: 'UNKNOWN'
                            slackSend(channel: "build-alert", message: "${buildStatus} - <${env.JENKINS_URL}/blue/organizations/jenkins/${projectName}/detail/${env.BRANCH_NAME}/${env.BUILD_NUMBER}/pipeline | ${jobName}> [build ${env.BUILD_NUMBER}]")
                         }
                    }
            }
        }
    }

}
