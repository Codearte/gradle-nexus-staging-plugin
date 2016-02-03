package io.codearte.gradle.nexus.functional

import io.codearte.gradle.nexus.PasswordUtil
import spock.lang.Ignore
import spock.lang.IgnoreIf

class BasicFunctionalSpec extends BaseNexusStagingFunctionalSpec {

    @Override
    void setup() {
        nexusPassword = PasswordUtil.tryToReadNexusPassword()
    }

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

    @Ignore
    def "should drop promoted repository"() {
        given:
            buildFile << """
                ${getApplyPluginBlock()}
                ${getDefaultConfigurationClosure()}
            """.stripIndent()
        when:
            def result = runTasksSuccessfully('dropRepository')
        then:
            result.wasExecuted(':dropRepository')
        and:
            result.standardOutput.contains("has been dropped")   //TODO: Match with regexp
    }
}