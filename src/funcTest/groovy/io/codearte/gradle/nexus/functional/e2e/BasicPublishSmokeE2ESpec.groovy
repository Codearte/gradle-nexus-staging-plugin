package io.codearte.gradle.nexus.functional.e2e

import io.codearte.gradle.nexus.functional.BaseNexusStagingFunctionalSpec
import nebula.test.functional.ExecutionResult
import spock.lang.Stepwise

//TODO: Remove duplication with tests for 'maven' - @Stepwise works with abstract tests in super class?
@Stepwise
class BasicPublishSmokeE2ESpec extends BaseNexusStagingFunctionalSpec implements E2ESpecHelperTrait {

    def "should get staging profile"() {
        given:
            copyResources("sampleProjects//nexus-at-minimal-publish", "")
        when:
            ExecutionResult result = runTasksSuccessfully('getStagingProfile')
        then:
            result.wasExecuted(':getStagingProfile')
        and:
//            println result.standardOutput   //TODO: How to redirect stdout to show on console (works with 2.2.1)
            result.standardOutput.contains("Received staging profile id: $E2E_STAGING_PROFILE_ID")
    }

    def "should publish artifacts to explicitly created stating repository and close and release that particular repository reusing set ID"() {
        given:
            copyResources("sampleProjects//nexus-at-minimal-publish", "")
        when:
            ExecutionResult result = runTasksSuccessfully('clean', 'publish', 'closeAndReleaseRepository')
        then:
            with(result) {
                verifyAll {
                    //TODO: How to verify task execution in order?
                    wasExecuted("initializeNexusStagingRepository")
                    wasExecuted("publishMavenJavaPublicationToNexusRepository")
                    //and
                    standardOutput.contains('to repository remote at https://oss.sonatype.org/service/local/staging/deployByRepositoryId/iogitlabnexus-at-')
                    //and
                    wasExecuted("closeRepository")
                    wasExecuted("releaseRepository")
                    wasExecuted("closeAndReleaseRepository")
                    //and
                    standardOutput.contains('has been effectively released')
                    //and reuse provided staging profile in both close and release
                    standardOutput.contains("Reusing staging repository id: iogitlabnexus-at")
                    !standardOutput.contains("DEPRECATION WARNING. The staging repository ID is not provided.")
                }
            }
    }
}
