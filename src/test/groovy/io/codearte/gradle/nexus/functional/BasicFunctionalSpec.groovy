package io.codearte.gradle.nexus.functional

import spock.lang.Ignore
import spock.lang.IgnoreIf
import spock.lang.Unroll

class BasicFunctionalSpec extends BaseNexusStagingFunctionalSpec {

    @IgnoreIf({ !env.containsKey("nexusPassword") })
    def "should run"() {
        given:
            buildFile << """
                ${getApplyPluginBlock()}
                ${getDefaultConfigurationClosure()}
            """.stripIndent()
        when:
            def result = runTasksSuccessfully('getStagingProfile')
        then:
            result.wasExecuted(':getStagingProfile')
        and:
//            println result.standardOutput   //TODO: How to redirect stdout to show on console (works with 2.2.1)
            result.standardOutput.contains("Received staging profile id: 93c08fdebde1ff")
    }

    @IgnoreIf({ !env.containsKey("nexusPassword") })
    def "should pass parameter to other task"() {
        given:
            buildFile << """
                ${getApplyPluginBlock()}
                ${getDefaultConfigurationClosure()}
                task getValue << {
                    assert getStagingProfile.stagingProfileId == "93c08fdebde1ff"
                }
            """.stripIndent()
        expect:
            runTasksSuccessfully('getStagingProfile', 'getValue')
    }

    @Ignore
    def "should close open repository"() {
        given:
            buildFile << """
                ${getApplyPluginBlock()}
                ${getDefaultConfigurationClosure()}
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
                ${getApplyPluginBlock()}
                ${getDefaultConfigurationClosure()}
            """.stripIndent()
        when:
            def result = runTasksSuccessfully('promoteRepository')
        then:
            result.wasExecuted(':promoteRepository')
        and:
            result.standardOutput.contains("has been promotted")   //TODO: Match with regexp
    }

    //TODO: Could be switched to Wiremock server to do not have errors in logs
    @Unroll
    def "should not do request for staging profile when provided in configuration on #testedTaskName task"() {
        given:
            def incorrectProfileId = "incorrectProfileId"
            buildFile << """
                ${getApplyPluginBlock()}
                ${getDefaultConfigurationClosure()}
                nexusStaging {
                    serverUrl = "http://localhost/invalid/"
                    stagingProfileId = "incorrectProfileId"
                }
            """.stripIndent()
        when:
            def result = runTasksWithFailure(testedTaskName)
        then:
            result.wasExecuted(testedTaskName)
            result.standardOutput.contains("Using configured staging profile id: $incorrectProfileId")
            !result.standardOutput.contains("Getting staging profile for package group")
        where:
            testedTaskName << ["closeRepository", "promoteRepository"]
    }

    private String getApplyPluginBlock() {
        return """
                apply plugin: 'io.codearte.nexus-staging'

                buildscript {
                    repositories {
                        mavenCentral()
                    }
                }
        """
    }

    private String getDefaultConfigurationClosure() {
        return """
                nexusStaging {
                    username = "codearte"
                    password = '$nexusPassword'
                    packageGroup = "io.codearte"
                }
        """
    }
}