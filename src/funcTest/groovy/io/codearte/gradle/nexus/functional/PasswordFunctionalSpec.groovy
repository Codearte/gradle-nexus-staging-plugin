package io.codearte.gradle.nexus.functional

import nebula.test.functional.ExecutionResult
import org.codehaus.groovy.runtime.StackTraceUtils
import org.gradle.api.logging.LogLevel
import spock.lang.Issue

//Cannot be tested with ProjectBuilder as it doesn't trigger taskGraph.whenReady callback
class PasswordFunctionalSpec extends BaseNexusStagingFunctionalSpec implements FunctionalSpecHelperTrait {

    def "should read password from repository configured in uploadArchives"() {
        given:
            copyResources("sampleProjects//uploadArchives", "")
            file("gradle.properties") << "\ntestSonatypeUsername=testUsername" << "\ntestSonatypePassword=testPassword"
        when:
            ExecutionResult result = runTasksWithFailure('getStagingProfile')
        then:
            assertFailureWithConnectionRefused(result.failure)
        and:
            result.standardOutput.contains("Using username 'testUsername' and password from repository 'Test repo'")
    }

    def "should read username and password from property when available"() {
        given:
            copyResources("sampleProjects//uploadArchives", "")
            file("gradle.properties") << "\nnexusUsername=nexusTestUsername" << "\nnexusPassword=nexusTestPassword"
        when:
            ExecutionResult result = runTasksWithFailure('getStagingProfile', '--build-file', 'build-property.gradle')
        then:
            assertFailureWithConnectionRefused(result.failure)
        and:
            //Cannot assert precisely as those values can be overridden by user's ~/.gradle/gradle.properties
            result.standardOutput.contains("Using password '*****' from Gradle property")
    }

    def "should not try read username and password when release tasks are not executed"() {
        given:
            copyResources("sampleProjects//uploadArchives", "")
        and:
            logLevel = LogLevel.DEBUG
        when:
            //'tasks' was initially chose unhappily as 'check' exposes issue with super class assignment with "DefaultTask" - issue #67
            ExecutionResult result = runTasksSuccessfully('check', '--build-file', 'build-property.gradle')
        then:
            result.standardOutput.contains("No staging task will be executed - skipping determination of Nexus credentials")
    }

    @Issue("https://github.com/Codearte/gradle-nexus-staging-plugin/issues/67")
    def "should try to determine credentials and not fail with custom uploadArchives task when staging task is requested"() {
        given:
            copyResources("sampleProjects//uploadArchives", "")
            file("gradle.properties") << "\nnexusUsername=nexusTestUsername" << "\nnexusPassword=nexusTestPassword"
        and:
            file("build-property.gradle") << """
                task uploadArchives {}
            """.stripIndent()
        when:
            ExecutionResult result = runTasksWithFailure('getStagingProfile', '--build-file', 'build-property.gradle')
        then:
            assertFailureWithConnectionRefused(result.failure)
        and:
            !result.standardOutput.contains("No staging task will be executed - skipping determination of Nexus credentials")
    }

    private static void assertFailureWithConnectionRefused(Throwable failure) {
        Throwable rootCause = StackTraceUtils.extractRootCause(failure)
        assert rootCause.getClass() == ConnectException
        assert rootCause.message.contains("Connection refused")
    }
}
