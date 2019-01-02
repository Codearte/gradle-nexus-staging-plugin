package io.codearte.gradle.nexus.functional.e2e

import io.codearte.gradle.nexus.functional.BaseNexusStagingFunctionalSpec
import nebula.test.functional.ExecutionResult
import spock.lang.Ignore
import spock.lang.Stepwise

@Stepwise
@Ignore("Temporary to verify staging repository uplaod from travis in exploratory e2e tests")
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

    def "should upload artifacts to server"() {
        given:
            copyResources("sampleProjects//nexus-at-minimal", "")
        when:
            ExecutionResult result = runTasksSuccessfully('clean', 'uploadArchives')
        then:
            result.standardOutput.contains('Uploading: io/gitlab/nexus-at/minimal/nexus-at-minimal/')
    }

    def "should close and release repository"() {
        given:
            copyResources("sampleProjects//nexus-at-minimal", "")
        when:
            ExecutionResult result = runTasksSuccessfully('closeAndReleaseRepository')
        then:
            result.wasExecuted("closeRepository")
            result.wasExecuted("releaseRepository")
            result.wasExecuted("closeAndReleaseRepository")
        and:
            result.standardOutput.contains('has been effectively released')
        and:
            result.standardOutput.contains("Reusing staging repository id: iogitlabnexus-at")   //at least in release
    }
}
