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

    def "should publish artifacts to explicitly created stating repository"() {
        given:
            copyResources("sampleProjects//nexus-at-minimal-publish", "")
        when:
            ExecutionResult result = runTasksSuccessfully('clean', 'publishToNexus')
        then:
            result.wasExecuted("initializeNexusStagingRepository")
            result.wasExecuted("publishMavenJavaPublicationToNexusRepository")
            result.wasExecuted("publishToNexus")
        and:
            result.standardOutput.contains('to repository remote at https://oss.sonatype.org/service/local/staging/deployByRepositoryId/iogitlabnexus-at-')
    }

    def "should close and release repository"() {
        given:
            copyResources("sampleProjects//nexus-at-minimal-publish", "")
        when:
            ExecutionResult result = runTasksSuccessfully('closeAndReleaseRepository')
        then:
            result.wasExecuted("closeRepository")
            result.wasExecuted("releaseRepository")
            result.wasExecuted("closeAndReleaseRepository")
        and:
            result.standardOutput.contains('has been effectively released')

        and: "reuse provided staging profile in both close and release"
            result.standardOutput.contains("Reusing staging repository id: iogitlabnexus-at")
//            //Uncomment once bumped nexus-publish-plugin dependency to version implementing https://github.com/marcphilipp/nexus-publish-plugin/issues/11
//            !result.standardOutput.contains("DEPRECATION WARNING. The staging repository ID is not provided.")
    }
}
