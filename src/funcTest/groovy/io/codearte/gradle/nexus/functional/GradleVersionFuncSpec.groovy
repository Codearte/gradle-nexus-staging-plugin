package io.codearte.gradle.nexus.functional

import com.google.common.base.Predicate
import com.google.common.base.Predicates
import nebula.test.functional.ExecutionResult
import nebula.test.functional.GradleRunner
import spock.util.Exceptions

import java.util.regex.Pattern

/**
 * Verifies that plugin doesn't fail during Gradle initialization (e.g. due to ClassCastException error) with different "supported" Gradle versions.
 */
class GradleVersionFuncSpec extends BaseNexusStagingFunctionalSpec implements FunctionalSpecHelperTrait {

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
            Exceptions.getRootCause(result.failure).with {
                getClass() == ConnectException
                message.contains("Connection refused")
            }
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
    private static final Pattern SPOCK_JAR_PATTERN = Pattern.compile(".*spock-core-1\\..*.jar")
    private static final Predicate<URL> FILTER_SPOCK_JAR = { URL url ->
        return !url.toExternalForm().matches(SPOCK_JAR_PATTERN)
    } as Predicate<URL>

    private List<String> resolveRequestedGradleVersions() {
        return ["2.0", "3.5.1", "4.1"]
    }
}
