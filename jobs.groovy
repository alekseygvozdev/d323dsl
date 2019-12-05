def git_info = ("git ls-remote -h https://github.com/alekseygvozdev/d323dsl").execute()
def branches = git_info.text.readLines().collect { it.split()[1].replaceAll('refs/heads/', '')}.unique()

job('MNTLAB-agvozdev-main-build-job') {
	parameters {
		choiceParam('BRANCH_NAME', ['agvozdev', 'master'], 'Branch name')
		activeChoiceParam('BUILD_TRIGGER') {
			description('Choose jobs')
			choiceType('CHECKBOX')
			groovyScript {
              script('list = []; for(i in 1..4){ list.add("MNTLAB-agvozdev-child${i}-build-job")}; return list\n')
				}
			}
		}
		steps {
			downstreamParameterized {
			trigger('$BUILD_TRIGGER') {
				block {
					buildStepFailure("FAILURE")
					unstable("UNSTABLE")
					failure("FAILURE")
				}
                parameters {
                predefinedProp('BRANCH_NAME', '${BRANCH_NAME}')
              	currentBuild()	
                }
            }
        }

    }
}
for (i in 1..4) {
  job("MNTLAB-agvozdev-child${i}-build-job") {
    	parameters {      
			choiceParam('BRANCH_NAME', branches, 'Choose a branch')
    }
          
		scm {
			git {
				remote {
					github("alekseygvozdev/d323dsl", "https")
				}
              branch('$BRANCH_NAME')
				}
			}
      	logRotator {
        numToKeep(5)
        artifactNumToKeep(1)
    }
      	wrappers {
        	preBuildCleanup {
            deleteDirectories(false)
            cleanupParameter()
        	}
        }
		steps {
            shell('chmod +x script.sh; ./script.sh > output.txt; tar -czvf ${BRANCH_NAME}_dsl_script.tar.gz jobs.groovy')
        }
     	publishers {
			archiveArtifacts {
              pattern('${BRANCH_NAME}_dsl_script.tar.gz')
            }
        }     
    }
}