node {
    label "any"
    properties([pipelineTriggers([pollSCM('')])])
    checkout scm
    
    stage('Job DSL') {
        steps {
            jobDsl targets: ['jobs/*.groovy'].join('\n'),
                removedJobAction: 'DELETE',
                removedViewAction: 'DELETE',
                additionalParameters: [workspace_dir: "${env.WORKSPACE}", jenkins_url: "${env.JENKINS_URL}"]
        }
        post {
            always {
                script {
                    // Call the custom script slackNotify from the vars folder
                    slackNotify()
                }
            }
        }
    }
}
