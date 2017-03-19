package io.codearte.gradle.nexus.functional

import io.codearte.gradle.nexus.FunctionalTestHelperTrait
import spock.lang.Ignore
import spock.lang.IgnoreIf

class BasicFunctionalSpec extends BaseNexusStagingFunctionalSpec implements FunctionalTestHelperTrait {

    @Override
    void setup() {
        nexusPassword = tryToReadNexusPasswordAT()
    }

    @IgnoreIf({ !env.containsKey("nexusPasswordAT") })
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
            result.standardOutput.contains("Received staging profile id: $E2E_STAGING_PROFILE_ID")
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
            result.standardOutput.contains("has been promoted")   //TODO: Match with regexp
    }
}
