package io.codearte.gradle.nexus.functional

import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.github.tomakehurst.wiremock.stubbing.Scenario
import groovy.json.JsonOutput
import io.codearte.gradle.nexus.logic.FetcherResponseTrait
import io.codearte.gradle.nexus.logic.RepositoryState
import nebula.test.functional.ExecutionResult
import org.gradle.api.logging.LogLevel
import org.junit.Rule

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.github.tomakehurst.wiremock.client.WireMock.containing
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo
import static com.github.tomakehurst.wiremock.client.WireMock.get
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import static com.github.tomakehurst.wiremock.client.WireMock.post
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import static com.github.tomakehurst.wiremock.client.WireMock.verify

//TODO: Split into two files: basic and transition related
class MockedFunctionalSpec extends BaseNexusStagingFunctionalSpec implements FetcherResponseTrait {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089)

    private static final String stagingProfileId = "5027d084a01a3a"
    private static final String REPO_ID_1 = "testRepo1"
    private static final String REPO_ID_2 = "testRepo2"

    def "should not do request for staging profile when provided in configuration on #testedTaskName task"() {
        given:
            stubGetOneRepositoryWithProfileIdAndContent(stagingProfileId,
                    createResponseMapWithGivenRepos([aRepoInStateAndId(REPO_ID_1, repoTypeToReturn)]))
        and:
            stubGetRepositoryStateByIdForConsecutiveStates(REPO_ID_1, repositoryStatesToGetById)
        and:
            stubSuccessfulCloseRepositoryWithProfileId()
        and:
            stubSuccessfulPromoteRepositoryWithProfileId()
        and:
            buildFile << """
                ${getApplyPluginBlock()}
                ${getDefaultConfigurationClosure()}
                nexusStaging {
                    stagingProfileId = "$stagingProfileId"
                }
            """.stripIndent()
        when:
            ExecutionResult result = runTasksSuccessfully(testedTaskName)
        then:
            result.wasExecuted(testedTaskName)
            result.standardOutput.contains("Using configured staging profile id: $stagingProfileId")
            !result.standardOutput.contains("Getting staging profile for package group")
        and:
            verify(0, getRequestedFor(urlEqualTo("/staging/profiles")))
        where:
            testedTaskName      | repoTypeToReturn       | repositoryStatesToGetById
            "closeRepository"   | RepositoryState.OPEN   | [RepositoryState.CLOSED]
            "promoteRepository" | RepositoryState.CLOSED | [RepositoryState.RELEASED]
    }

    def "should send request for staging profile when not provided in configuration on #testedTaskName task"() {
        given:
            stubGetStagingProfilesWithJson(this.getClass().getResource("/io/codearte/gradle/nexus/logic/2stagingProfilesShrunkResponse.json").text)
        and:
            stubGetOneRepositoryWithProfileIdAndContent(stagingProfileId,
                    createResponseMapWithGivenRepos([aRepoInStateAndId(REPO_ID_1, repoTypeToReturn)]))
        and:
            stubGetRepositoryStateByIdForConsecutiveStates(REPO_ID_1, repositoryStatesToGetById)
        and:
            stubSuccessfulCloseRepositoryWithProfileId()
        and:
            stubSuccessfulPromoteRepositoryWithProfileId()
        and:
            buildFile << """
                ${getApplyPluginBlock()}
                ${getDefaultConfigurationClosure()}
                nexusStaging {
                    stagingProfileId = null //by default set in tests
                }
            """.stripIndent()
        when:
            ExecutionResult result = runTasksSuccessfully(testedTaskName)
        then:
            result.wasExecuted(testedTaskName)
            !result.standardOutput.contains("Using configured staging profile id: $stagingProfileId")
            result.standardOutput.contains("Getting staging profile for package group")
        and:
            verify(1, getRequestedFor(urlEqualTo("/staging/profiles")))
        where:
            testedTaskName      | repoTypeToReturn       | repositoryStatesToGetById
            "closeRepository"   | RepositoryState.OPEN   | [RepositoryState.CLOSED]
            "promoteRepository" | RepositoryState.CLOSED | [RepositoryState.RELEASED]
    }

    def "should reuse stagingProfileId AND stagingRepositoryId from closeRepository in promoteRepository when called together"() {
        given:
            stubGetStagingProfilesWithJson(this.getClass().getResource("/io/codearte/gradle/nexus/logic/2stagingProfilesShrunkResponse.json").text)
        and:
            stubGetOneOpenRepositoryAndOneClosedInFirstCallAndTwoClosedInTheNext(stagingProfileId)
        and:
            stubGetRepositoryStateByIdForConsecutiveStates(REPO_ID_1, [RepositoryState.CLOSED, RepositoryState.RELEASED])
        and:
            stubSuccessfulCloseRepositoryWithProfileId()
        and:
            stubSuccessfulPromoteRepositoryWithProfileId()
        and:
            buildFile << """
                ${getApplyPluginBlock()}
                ${getDefaultConfigurationClosure()}
                nexusStaging {
                    stagingProfileId = null //by default set in tests
                }
            """.stripIndent()
        when:
            ExecutionResult result = runTasksSuccessfully("closeRepository", "promoteRepository")
        then:
            result.wasExecuted("closeRepository")
            result.wasExecuted("promoteRepository")
        and:
            verify(1, getRequestedFor(urlEqualTo("/staging/profile_repositories/$stagingProfileId")))
            verify(1, getRequestedFor(urlEqualTo("/staging/profiles")))
    }

    def "should pass parameter to other task"() {
        given:
            stubGetStagingProfilesWithJson(this.getClass().getResource("/io/codearte/gradle/nexus/logic/2stagingProfilesShrunkResponse.json").text)
        and:
            buildFile << """
                ${getApplyPluginBlock()}
                ${getDefaultConfigurationClosure()}
                task getValue << {
                    assert getStagingProfile.stagingProfileId == "$stagingProfileId"
                }
            """.stripIndent()
        expect:
            runTasksSuccessfully('getStagingProfile', 'getValue')
    }

    def "should retry promotion when repository has not been already closed"() {
        given:
            stubGetOneOpenRepositoryInFirstCallAndOneClosedInTheNext(stagingProfileId)
        and:
            stubGetRepositoryStateByIdForConsecutiveStates(REPO_ID_1, [RepositoryState.RELEASED])
        and:
            stubSuccessfulCloseRepositoryWithProfileId()
        and:
            stubSuccessfulPromoteRepositoryWithProfileId()
        and:
            buildFile << """
                ${getApplyPluginBlock()}
                ${getDefaultConfigurationClosure()}
            """.stripIndent()
        when:
            ExecutionResult result = runTasksSuccessfully("promoteRepository")
        then:
            result.wasExecuted("promoteRepository")
            result.standardOutput.contains("Attempt 1/3 failed.")
            !result.standardOutput.contains("Attempt 2/3 failed.")
    }

    def "should display staging profile without --info switch"() {
        given:
            stubGetStagingProfilesWithJson(this.getClass().getResource("/io/codearte/gradle/nexus/logic/2stagingProfilesShrunkResponse.json").text)
        and:
            buildFile << """
                ${getApplyPluginBlock()}
                ${getDefaultConfigurationClosure()}
            """.stripIndent()
        and:
            logLevel = LogLevel.LIFECYCLE
        when:
            ExecutionResult result = runTasksSuccessfully('getStagingProfile')
        then:
            result.standardOutput.contains("Received staging profile id: $stagingProfileId")
    }

    def "should call close and promote in closeAndPromoteRepository task"() {
        given:
            stubGetOneOpenRepositoryAndOneClosedInFirstCallAndTwoClosedInTheNext(stagingProfileId)
        and:
            stubGetRepositoryStateByIdForConsecutiveStates(REPO_ID_1, [RepositoryState.CLOSED, RepositoryState.RELEASED])
        and:
            stubSuccessfulCloseRepositoryWithProfileId()
        and:
            stubSuccessfulPromoteRepositoryWithProfileId()
        and:
            buildFile << """
                ${getApplyPluginBlock()}
                ${getDefaultConfigurationClosure()}
            """.stripIndent()
        when:
            ExecutionResult result = runTasksSuccessfully("closeAndPromoteRepository")
        then:
            result.wasExecuted("closeRepository")
            result.wasExecuted("promoteRepository")
            result.wasExecuted("closeAndPromoteRepository")
    }

    def "packageGroup should be set to project.group by default "() {
        given:
            stubGetStagingProfilesWithJson(this.getClass().getResource("/io/codearte/gradle/nexus/logic/2stagingProfilesShrunkResponse.json").text)
        and:
            buildFile << """
                ${getApplyPluginBlock()}
                ${getDefaultConfigurationClosure()}
                nexusStaging {
                    stagingProfileId = null
                }
                project.group = "io.codearte"
            """.stripIndent()
        when:
            ExecutionResult result = runTasksSuccessfully('getStagingProfile')
        then:
            result.standardOutput.contains("Received staging profile id: $stagingProfileId")
    }

    def "explicitly defined packageGroup should override default value"() {
        given:
            stubGetStagingProfilesWithJson(this.getClass().getResource("/io/codearte/gradle/nexus/logic/2stagingProfilesShrunkResponse.json").text)
        and:
            buildFile << """
                ${getApplyPluginBlock()}
                ${getDefaultConfigurationClosure()}
                project.group = "io.someother"
            """.stripIndent()
        when:
            ExecutionResult result = runTasksSuccessfully('getStagingProfile')
        then:
            result.standardOutput.contains("Received staging profile id: $stagingProfileId")
    }

    def "should wait on #operationName operation until transitioning is finished"() {
        given:
            stubGetOneRepositoryWithProfileIdAndContent(stagingProfileId,
                createResponseMapWithGivenRepos([aRepoInStateAndId(REPO_ID_1, repoStates[0])]))
        and:
            stubGetRepositoryStateByIdForConsecutiveStates(REPO_ID_1, repoStates, [true, false])
        and:
            stubbingOperation()
        and:
            buildFile << """
                ${getApplyPluginBlock()}
                ${getDefaultConfigurationClosure()}
            """.stripIndent()
        when:
            ExecutionResult result = runTasksSuccessfully("${operationName}Repository")
        then:
            result.wasExecuted("${operationName}Repository")
            result.standardOutput.contains("Attempt 1/3 failed.")
            !result.standardOutput.contains("Attempt 2/3 failed.")
        where:
            operationName | repoStates                                         | stubbingOperation
            "close"       | [RepositoryState.OPEN, RepositoryState.CLOSED]     | { stubSuccessfulCloseRepositoryWithProfileId() }
            "promote"     | [RepositoryState.CLOSED, RepositoryState.RELEASED] | { stubSuccessfulPromoteRepositoryWithProfileId() }
    }

    @Override
    protected String getDefaultConfigurationClosure() {
        return """
                nexusStaging {
                    stagingProfileId = "$stagingProfileId"
                    username = "codearte"
                    packageGroup = "io.codearte"
                    serverUrl = "http://localhost:8089/"
                    //To do not wait too long in case of failure
                    delayBetweenRetriesInMillis = 50
                    numberOfRetries = 2
                }
        """
    }

    private void stubGetStagingProfilesWithJson(String responseAsJson) {
        stubFor(get(urlEqualTo("/staging/profiles"))
                .withHeader("Content-Type", containing("application/json"))
                .withHeader("Accept", containing("application/json"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(responseAsJson)))
    }

    //TODO: Do not pass stagingProfileId as argument - use constant - only one is used everywhere
    private void stubGetOneRepositoryWithProfileIdAndContent(String stagingProfileId, Map response) {
        stubFor(get(urlEqualTo("/staging/profile_repositories/$stagingProfileId"))
                .withHeader("Content-Type", containing("application/json"))
                .withHeader("Accept", containing("application/json"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(JsonOutput.prettyPrint(JsonOutput.toJson(response)))))
    }

    private void stubGetRepositoryStateByIdForConsecutiveStates(String repoId, List<RepositoryState> repoStates,
                                                                List<Boolean> isTransitioningList = null) {
        if (isTransitioningList == null) {
            isTransitioningList = repoStates.collect { false }
        }
        repoStates.eachWithIndex { repoState, index ->
            stubFor(get(urlEqualTo("/staging/repository/$repoId"))
                .inScenario("StateById")
                .whenScenarioStateIs(index == 0 ? Scenario.STARTED : repoStates[index].name())
                .withHeader("Content-Type", containing("application/json"))
                .withHeader("Accept", containing("application/json"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(JsonOutput.prettyPrint(JsonOutput.toJson(aRepoInStateAndId(repoId, repoState, isTransitioningList[index])))))
                .willSetStateTo(repoStates[index < repoStates.size() - 1 ? index + 1 : index].name()))  //TODO: Simplify/extract...
        }
    }

    private void stubSuccessfulCloseRepositoryWithProfileId() {
        stubGivenSuccessfulTransitionOperationWithProfileId("close")
    }

    private void stubSuccessfulPromoteRepositoryWithProfileId() {
        stubGivenSuccessfulTransitionOperationWithProfileId("promote")
    }

    private void stubGivenSuccessfulTransitionOperationWithProfileId(String restCommandName) {
        stubFor(post(urlEqualTo("/staging/bulk/$restCommandName"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Accept", containing("application/json"))
                //TODO: Content matching
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")))
    }

    private void stubGetOneOpenRepositoryAndOneClosedInFirstCallAndTwoClosedInTheNext(String stagingProfileId) {
        stubGetGivenRepositoriesInFirstAndSecondCall(stagingProfileId,
                [aRepoInStateAndId(REPO_ID_1, RepositoryState.OPEN), aRepoInStateAndId(REPO_ID_2, RepositoryState.CLOSED)],
                [aRepoInStateAndId(REPO_ID_1, RepositoryState.CLOSED), aRepoInStateAndId(REPO_ID_2, RepositoryState.CLOSED)])
    }

    private void stubGetOneOpenRepositoryInFirstCallAndOneClosedInTheNext(String stagingProfileId) {
        stubGetGivenRepositoriesInFirstAndSecondCall(stagingProfileId,
                [aRepoInStateAndId(REPO_ID_1, RepositoryState.OPEN)],
                [aRepoInStateAndId(REPO_ID_1, RepositoryState.CLOSED)])
    }

    private void stubGetGivenRepositoriesInFirstAndSecondCall(String stagingProfileId, List<Map> repositoriesToReturnInFirstCall,
                                                              List<Map> repositoriesToReturnInSecondCall) {
        stubFor(get(urlEqualTo("/staging/profile_repositories/$stagingProfileId")).inScenario("State")
            .whenScenarioStateIs(Scenario.STARTED)
            .withHeader("Content-Type", containing("application/json"))
            .withHeader("Accept", containing("application/json"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(
                    JsonOutput.prettyPrint(JsonOutput.toJson(createResponseMapWithGivenRepos(repositoriesToReturnInFirstCall)))
                )
            )
            .willSetStateTo("CLOSED"))

        stubFor(get(urlEqualTo("/staging/profile_repositories/$stagingProfileId")).inScenario("State")
            .whenScenarioStateIs("CLOSED")
            .withHeader("Content-Type", containing("application/json"))
            .withHeader("Accept", containing("application/json"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(
                    JsonOutput.prettyPrint(JsonOutput.toJson(createResponseMapWithGivenRepos(repositoriesToReturnInSecondCall)))
                )
            )
        )
    }
}
