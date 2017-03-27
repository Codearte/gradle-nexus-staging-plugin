package io.codearte.gradle.nexus.functional

import com.google.common.base.Predicate
import com.google.common.base.Predicates
import nebula.test.functional.ExecutionResult
import nebula.test.functional.GradleRunner

/**
 * Verifies that plugin doesn't fail during Gradle initialization (e.g. due to ClassCastException error) with different "supported" Gradle versions.
 */
class GradleVersionFuncSpec extends BaseNexusStagingFunctionalSpec {

    def "should not fail on plugin logic not initialization issue with Gradle #requestedGradleVersion"() {
        given:
            gradleVersion = requestedGradleVersion
            classpathFilter = Predicates.and(GradleRunner.CLASSPATH_DEFAULT, FILTER_SPOCK_JAR)
            memorySafeMode = true   //shutdown Daemon after a few seconds of inactivity
        and:
            buildFile << """
                ${getApplyPluginBlock()}
                ${getPluginConfigurationWithNotExistingNexusServer()}                
            """.stripIndent()
        when:
            ExecutionResult result = runTasksWithFailure('getStagingProfile')
        then:
            result.wasExecuted(':getStagingProfile')
        and:
            result.failure.cause.message.contains("Execution failed for task ':getStagingProfile'")
            result.failure.cause.cause.message.contains("HttpHostConnectException: Connection to http://localhost:61942 refused")
        where:
            requestedGradleVersion << resolveRequestedGradleVersions()
    }

    private String getPluginConfigurationWithNotExistingNexusServer() {
        return """
                nexusStaging {
                    packageGroup = "fake.one"
                    serverUrl = "http://localhost:61942/"
                }
                """
    }

    //To prevent failure when Spock for Groovy 2.4 is run with Groovy 2.3 delivered with Gradle <2.8
    //Spock is not needed in this artificial project - just the test classpath leaks to Gradle instance started by Nebula
    private static final Predicate<URL> FILTER_SPOCK_JAR = { URL url ->
        return !url.toExternalForm().contains("spock-core-1.0-groovy-2.4.jar")
    } as Predicate<URL>

    private List<String> resolveRequestedGradleVersions() {
        return ["2.0", "3.4.1"]
    }
}
