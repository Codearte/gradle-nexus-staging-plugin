package io.codearte.gradle.nexus.functional

import spock.lang.Ignore
import spock.lang.IgnoreIf

class BasicFunctionalSpec extends BaseNexusStagingFunctionalSpec {

    @IgnoreIf({ !env.containsKey("nexusPassword") })
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
                    nexusUrl = "https://oss.sonatype.org/service/local/"
                    username = "codearte"
                    password = '$nexusPassword'
                    packageGroup = "io.codearte"
                }
            """.stripIndent()
        when:
            def result = runTasksSuccessfully('getStagingProfileTask')
        then:
            result.wasExecuted(':getStagingProfileTask')
        and:
//            println result.standardOutput   //TODO: How to redirect stdout to show on console (works with 2.2.1)
            result.standardOutput.contains("Received staging profile id: 93c08fdebde1ff")
    }

    @IgnoreIf({ !env.containsKey("nexusPassword") })
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
                    nexusUrl = "https://oss.sonatype.org/service/local/"
                    username = "codearte"
                    password = '$nexusPassword'
                    packageGroup = "io.codearte"
                }
                task getValue << {
                    assert getStagingProfileTask.stagingProfileId == "93c08fdebde1ff"
                }
            """.stripIndent()
        expect:
            runTasksSuccessfully('getStagingProfileTask', 'getValue')
    }

    @Ignore
    def "should close open repository"() {
        given:
            buildFile << """
                apply plugin: 'io.codearte.nexus-staging'

                buildscript {
                    repositories {
                        mavenCentral()
                    }
                }
                nexusStaging {
                    nexusUrl = "https://oss.sonatype.org/service/local/"
                    username = "codearte"
                    password = '$nexusPassword'
                    packageGroup = "io.codearte"
                }
            """.stripIndent()
        when:
            def result = runTasksSuccessfully('closeRepository')
        then:
            result.wasExecuted(':closeRepository')
        and:
            result.standardOutput.contains("has been closed")   //TODO: Match with regexp
    }

    @Ignore
    def "should promote closed repository"() {
        given:
            buildFile << """
                apply plugin: 'io.codearte.nexus-staging'

                buildscript {
                    repositories {
                        mavenCentral()
                    }
                }
                nexusStaging {
                    nexusUrl = "https://oss.sonatype.org/service/local/"
                    username = "codearte"
                    password = '$nexusPassword'
                    packageGroup = "io.codearte"
                }
            """.stripIndent()
        when:
            def result = runTasksSuccessfully('promoteRepository')
        then:
            result.wasExecuted(':promoteRepository')
        and:
            result.standardOutput.contains("has been promotted")   //TODO: Match with regexp
    }
}