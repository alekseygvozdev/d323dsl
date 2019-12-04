job('MNTLAB-agvozdev-main-build-job') {
	parameters {
		choiceParam('BRANCH_NAME', ['agvozdev', 'master'], 'Branch name')
		activeChoiceParam('BUILD_TRIGGER') {
			description('Choose jobs')
			choiceType('CHECKBOX')
			groovyScript {
				script('''return ["MNTLAB-agvozdev-child1-build-job", 
                       "MNTLAB-agvozdev-child2-build-job", 
                       "MNTLAB-agvozdev-child3-build-job", 
                       "MNTLAB-agvozdev-child4-build-job"]''')
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
                    predefinedProp('BRANCH_NAME', '$BRANCH_NAME')
                }
            }
        }
    }
}
for (i in 1..4) {
	job("MNTLAB-agvozdev-child${i}-build-job") {
		scm {
			git {
				remote {
					github("alekseygvozdev/d323dsl", "https")
				}
				branch('$BRANCH_NAME')
				}
			}
		steps {
            shell('''chmod +x script.sh
                ./script.sh > output.txt
				tar -czvf ${BRANCH_NAME}_dsl_script.tar.gz jobs.groovy''')
        }
     	publishers {
			archiveArtifacts {
              pattern('${BRANCH_NAME}_dsl_script.tar.gz')
            }
        }     
    }  
}