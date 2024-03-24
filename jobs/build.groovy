import groovy.json.JsonSlurper

String stashUrl = 'git@github.com:rajendra-gangwar'
String stashUser = 'jenkins'

/*
def gitStuff = [
    [
        project: '',
        url: stashUrl,
        user: stashUser,
        repo: 'cicd-infra'
    ]
]
*/

//def inclusions = "main"
//def exclusions = "dev"

// Get the jenkins URL
//def currentjenkinsUrl = Jenkins.instance.getRootUrl()

// define a map for inclusive and exclusive based on jenkins URL
currentJenkinsUrl = "${jenkins_url}"
def jenkinsUrls = [
    'localhost': 'poc',   
    'jenkins-69738551.eu-central-1.elb.amazonaws.com': 'dev'    
]

println "jenkins url ${jenkins_url}"

jenkinsUrls.each { url, env ->
    if (currentJenkinsUrl == url) {
        prefix = env
        }
    }

println "Projects file env: ${prefix}"

//def currentDirectory = System.getProperty("user.dir")

//println "$currentDirectory"

// Reading projects detail from file
def gitStuffJson = new File("${workspace_dir}/${env}-projects.json").text
def gitStuff = new JsonSlurper().parseText(gitStuffJson)


gitStuff.each{ entry ->
    multibranchPipelineJob("${entry.repo}-autobuild") {
        branchSources {
            branchSource {
                source {
                    git {
                        remote("${entry.url}${entry.repo}.git")
                        credentialsId(entry.user)
                        id("git")
                        traits {
                            authorInChangelogTrait()
                            wipeWorkspaceTrait()
                            pruneStaleBranchTrait()
                            localBranchTrait()
                            cloneOptionTrait {
                              extension{
                                noTags(false)
                                reference("")
                                shallow(false)
                                timeout(60)
                              }
                            }
                            headWildcardFilter {
                              includes("${entry.inclusion}")
                              excludes("${entry.exclusions}")
                            }
                            submoduleOptionTrait {
                                extension{
                                    disableSubmodules(false)
                                    recursiveSubmodules(true)
                                    trackingSubmodules(false)
                                    reference("")
                                    parentCredentials(true)
                                    timeout(60)
                                }
                            }
                    }
                }
            }
        }

        orphanedItemStrategy {
              discardOldItems {
                  daysToKeep(5)
                  numToKeep(5)
              }
        }

      }
      configure { node ->
          def data = node  / sources / data
          node  / sources / data / 'jenkins.branch.BranchSource' / 
source / traits / 'jenkins.plugins.git.traits.BranchDiscoveryTrait' {
                'switch'('off')
            }

      }
      configure {
        it / 'triggers' << 'com.cloudbees.hudson.plugins.folder.computed.PeriodicFolderTrigger'{
            spec '* * * * *'
            interval "60000"
        }
    }

    }
    def job = 
jenkins.model.Jenkins.instance.getItemByFullName("${entry.repo}-autobuild")
    if(job){
      println ("start job : ${entry.repo}-autobuild")
      job.scheduleBuild2(0)
    }
}
