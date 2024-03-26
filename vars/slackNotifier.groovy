#!/usr/bin/env groovy
import groovy.json.JsonSlurper

def call( String buildStatus = 'STARTED',
          String destChannel = 'build-alerts' ,
          String destTeamDomain = 'innovation-dvy1427') {
  // build status of null means successful
  buildStatus =  buildStatus ?: 'SUCCESS'
  if ( buildStatus != "ABORTED") {
    def myFavGiphy      = 'https://media1.giphy.com/media/ch4OTkSncCVsA/giphy.gif?cid=6104955e1997a7650275c7e5d28215097fe4e28367f95c82&rid=giphy.gif';
    def jobName         = getJobName() 
    def allJob          = env.JOB_NAME.tokenize('/') as String[];
    def projectName     = allJob[0];
    def branchName      = getBranchName().trim()
    def color           = colorStatus(buildStatus)
    def commitHash      = getLastCommitInfo('%h').trim()
    def commitAuthor    = getLastCommitInfo('%aN').trim()
    def commitAuthorId  = getLastCommitInfo('%aE').split('@')[0]
    def slackBotOfUser  = '@' + commitAuthorId
    def authorNotFound  = false
    def commitMessage   = getLastCommitInfo('%B').trim()

    def msg = """ ${buildStatus} - <${env.JENKINS_URL}/blue/organizations/jenkins/${projectName}/detail/${env.BRANCH_NAME}/${env.BUILD_NUMBER}/pipeline | ${jobName}> [build ${env.BUILD_NUMBER}]
                | Commit ${commitHash} by ${commitAuthor}
                | ${commitMessage}""".stripMargin()

    try {
      slackSend(channel: slackBotOfUser,
              color: color,
              message: msg,
              teamDomain: destTeamDomain,
              tokenCredentialId:'slack'
        )
    } catch (exc) {
      authorNotFound = true
      print ('Could not find ' + slackBotOfUser + '.. sending the notification to ' + destChannel);
    }

    if('dev' == branchName 
       ||  branchName ==~ '(.)*RELEASE(.)*'
       || 'qa' == branchName 
       || 'qa-k8s' == branchName 
       || 'dev-k8s' == branchName 
       || authorNotFound) {
      slackSend(channel: destChannel,
              color: color,
              message: msg,
              teamDomain: destTeamDomain,
              tokenCredentialId:'slack_token'
        )
    } else {
      print(branchName + ' is not develop or qa or  master or Tagged RELEASE.. skip ' + destChannel + ' notification')
    }
  }
}

def colorStatus(status) {
  def result
  switch (status) {
    case 'SUCCESS':
    case 'STABLE':
      result = 'good'
      break
    case 'FAILURE':
      result = 'danger'
      break
    default:
      result = 'warning'
      break
  }
  result
}

// Jenkins does / -> %2F encoding, which hurts our eyes when reading the slack channel
def getJobName() {
  env.JOB_NAME.replace("%2F", "/")
}


@NonCPS
def getBranchName() {
   return sh(returnStdout: true, script: 'git rev-parse --abbrev-ref HEAD').trim()
}

@NonCPS
def getLastCommitInfo(format) {
   return sh(returnStdout: true, script: 'git log -1 --pretty="' + format + '"').trim()
}