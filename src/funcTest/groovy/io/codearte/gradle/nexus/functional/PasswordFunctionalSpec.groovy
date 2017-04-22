package io.codearte.gradle.nexus.functional

import io.codearte.gradle.nexus.FunctionalSpecHelperTrait
import org.codehaus.groovy.runtime.StackTraceUtils

class PasswordFunctionalSpec extends BaseNexusStagingFunctionalSpec implements FunctionalSpecHelperTrait {

    def "should read password from repository configured in uploadArchives"() {
        given:
            copyResources("sampleProjects//uploadArchives", "")
            file("gradle.properties") << "\ntestSonatypeUsername=testUsername" << "\ntestSonatypePassword=testPassword"
        when:
            def result = runTasksWithFailure('getStagingProfile')
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
            def result = runTasksWithFailure('getStagingProfile', '--build-file', 'build-property.gradle')
        then:
            assertFailureWithConnectionRefused(result.failure)
        and:
            //Cannot assert precisely as those values can be overridden by user's ~/.gradle/gradle.properties
            result.standardOutput.contains("Using password '*****' from Gradle property")  //TODO: matching with regexp
    }

    def "should not try read username and password when release tasks are not executed"() {
        given:
            copyResources("sampleProjects//uploadArchives", "")
            file("gradle.properties") << "\nnexusUsername=nexusTestUsername" << "\nnexusPassword=nexusTestPassword"
        when:
            def result = runTasksSuccessfully('tasks', '--build-file', 'build-property.gradle')
        then:
            !result.standardOutput.contains("Using password '*****' from Gradle property")  //TODO: matching with regexp
    }

    private static void assertFailureWithConnectionRefused(Throwable failure) {
        Throwable rootCause = StackTraceUtils.extractRootCause(failure)
        rootCause.getClass() == ConnectException
        rootCause.message == "Connection refused"
    }
}
