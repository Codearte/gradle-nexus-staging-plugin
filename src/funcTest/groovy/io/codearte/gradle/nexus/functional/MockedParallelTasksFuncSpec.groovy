package io.codearte.gradle.nexus.functional

import com.github.tomakehurst.wiremock.junit.WireMockRule
import nebula.test.functional.ExecutionResult
import org.junit.Rule
import spock.lang.Issue

class MockedParallelTasksFuncSpec extends BaseNexusStagingFunctionalSpec implements FunctionalSpecHelperTrait {

    //TODO: Duplication
    private static final String stagingProfileId = "5027d084a01a3a"

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(MockedFunctionalSpec.WIREMOCK_RANDOM_PORT)

    @Issue("https://github.com/Codearte/gradle-nexus-staging-plugin/issues/70")
    def "should not run releaseRepository and closeRepository failure"() {
        given:
            buildFile << """
                ${getApplyPluginBlock()}
                ${getDefaultConfigurationClosure()}
    
                task upload {
                    doFirst {
                        println "Uploading..."
                    }
                }
                upload.finalizedBy closeAndReleaseRepository
            """.stripIndent()
        when:
            ExecutionResult result = runTasksWithFailure("upload")
//            ExecutionResult result = runTasksWithFailure("closeAndReleaseRepository")
        then:
            //TODO: Złosić buga - z finalizedBy - czy na pewno oba powinny się wykonać?
            //Przykłady - https://github.com/gradle/gradle/pull/5417/files
            println result.standardOutput
    }

    //TODO
    @Override
    String getDefaultConfigurationClosure() {
        return """
                nexusStaging {
                    stagingProfileId = "$stagingProfileId"
                    username = "codearte"
                    packageGroup = "io.codearte"
                    serverUrl = "http://localhost:${wireMockRule.port()}/"
                    //To do not wait too long in case of failure
                    delayBetweenRetriesInMillis = 50
                    numberOfRetries = 2
                }
        """
    }
}
