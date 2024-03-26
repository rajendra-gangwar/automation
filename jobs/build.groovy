import groovy.json.JsonSlurper

String stashUrl = 'git@github.com:rajendra-gangwar'
//String stashUser = 'jenkins' //commenting to test fail case notification

// define a map for inclusive and exclusive based on jenkins URL
currentJenkinsUrl = "${jenkins_url}"
def jenkinsUrls = [
    '8085-port': 'dev',   
    'jenkins-69738551.eu-central-1.elb.amazonaws.com': 'dev'    
]

println "jenkins url ${jenkins_url}"

def prefix

jenkinsUrls.each { url, config_prefix ->
    if (currentJenkinsUrl.contains(url)) {
        prefix = config_prefix
        return
        }
    }

println "Projects file env: ${prefix}"

// Reading projects detail from file
def gitStuffJson = new File("${workspace_dir}/${prefix}-projects.json").text
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
/*
    def job = 
jenkins.model.Jenkins.instance.getItemByFullName("${entry.repo}-autobuild")
    if(job){
      println ("start job : ${entry.repo}-autobuild")
      job.scheduleBuild2(0)
    }
*/
}
