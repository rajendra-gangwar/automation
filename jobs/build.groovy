import groovy.json.JsonSlurper
import ../projects

String stashUrl = 'git@github.com:rajendra-gangwar'
String stashUser = 'jenkins'

inclusions = "main"
excludes = "dev"

gitStuff.each{ Map entry ->
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
                              includes(inclusions)
                              excludes(exclusions)
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
