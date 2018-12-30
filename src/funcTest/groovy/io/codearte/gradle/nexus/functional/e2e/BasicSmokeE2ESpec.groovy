package io.codearte.gradle.nexus.functional.e2e

import io.codearte.gradle.nexus.functional.BaseNexusStagingFunctionalSpec
import nebula.test.functional.ExecutionResult
import spock.lang.Stepwise

@Stepwise
class BasicSmokeE2ESpec extends BaseNexusStagingFunctionalSpec implements E2ESpecHelperTrait {

    def "should get staging profile"() {
        given:
            copyResources("sampleProjects//nexus-at-minimal", "")
        when:
            ExecutionResult result = runTasksSuccessfully('getStagingProfile')
        then:
            result.wasExecuted(':getStagingProfile')
        and:
//            println result.standardOutput   //TODO: How to redirect stdout to show on console (works with 2.2.1)
            result.standardOutput.contains("Received staging profile id: $E2E_STAGING_PROFILE_ID")
    }

    def "should upload artifacts to server and reuse explicitly created staging repository id in both close and release "() {
        given:
            copyResources("sampleProjects//nexus-at-minimal", "")
        when:
            ExecutionResult result = runTasksSuccessfully('clean', 'uploadArchivesStaging', 'closeAndReleaseRepository')
        then:
            with(result) {
                verifyAll {
                    wasExecuted("createRepository")
                    wasExecuted("uploadArchives")
                    wasExecuted("uploadArchivesStaging")
                    //and
                    standardOutput.contains('Uploading: io/gitlab/nexus-at/minimal/nexus-at-minimal/')
                    //and
                    wasExecuted("closeRepository")
                    wasExecuted("releaseRepository")
                    wasExecuted("closeAndReleaseRepository")
                    //and
                    standardOutput.contains("Reusing staging repository id: iogitlabnexus-at")
                    !standardOutput.contains("DEPRECATION WARNING. The staging repository ID is not provided.")
                }
            }
    }
}
