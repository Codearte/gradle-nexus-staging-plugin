package io.codearte.gradle.nexus.functional

class PasswordFunctionalSpec extends BaseNexusStagingFunctionalSpec {

    def "should read password from repository configured in uploadArchives"() {
        given:
            copyResources("sampleProjects//uploadArchives", "")
        when:
            def result = runTasksSuccessfully('tasks')
        then:
            result.standardOutput.contains("Using username 'testUsername' and password from repository 'Test repo'")
    }
}
