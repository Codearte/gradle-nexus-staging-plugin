package io.codearte.gradle.nexus.functional

class BasicFunctionalSpec extends BaseNexusStagingFunctionalSpec {

    def "should run"() {
        given:
            buildFile << """
                apply plugin: 'io.codearte.nexus-staging'

                buildscript {
                    repositories {
                        mavenCentral()
                    }
                }
                nexusStaging {
                    nexusUrl = "https://oss.sonatype.org/"
                    username = "codearte"
                    password = '$nexusPassword'
                }
            """.stripIndent()
        when:
            def result = runTasksSuccessfully('getStagingProfileTask')
        then:
            result.wasExecuted(':getStagingProfileTask')
        and:
//            println result.standardOutput   //TODO: How to redirect stdout to show on console (works with 2.2.1)
            result.standardOutput.contains("autoStagingDisabled:false")
    }

    def "should pass parameter to other task"() {
        given:
            buildFile << """
                apply plugin: 'io.codearte.nexus-staging'

                buildscript {
                    repositories {
                        mavenCentral()
                    }
                }
                nexusStaging {
                    nexusUrl = "https://oss.sonatype.org/"
                    username = "codearte"
                    password = '$nexusPassword'
                }
                task getValue << {
                    assert getStagingProfileTask.stagingProfileId == "93c08fdebde1ff"
                }
            """.stripIndent()
        expect:
            runTasksSuccessfully('getStagingProfileTask', 'getValue')
    }
}