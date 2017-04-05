package io.codearte.gradle.nexus.logic

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j
import io.codearte.gradle.nexus.exception.RepositoryInTransitionException
import io.codearte.gradle.nexus.infra.NexusHttpResponseException

@InheritConstructors
@CompileStatic
@Slf4j
class RepositoryStateFetcher extends BaseOperationExecutor {

    private static final int NOT_FOUND_RESPONSE_CODE = 404

    RepositoryState getNonTransitioningRepositoryStateById(String repoId) {
        try {
            return getRepoAssertingAndReturningState(repoId)
        } catch (NexusHttpResponseException e) {
            return handleGivenRepositoryNotFoundOrRethrowException(repoId, e)
        }
    }

    private RepositoryState getRepoAssertingAndReturningState(String repoId) {
        Map<String, Object> repoResponseAsMap = getRepositoryWithId(repoId)
        parseResponseAndThrowExceptionIfInTransition(repoResponseAsMap, repoId)
        return parseRepoStateFromRepsponse(repoResponseAsMap)
    }

    private Map<String, Object> getRepositoryWithId(String repoId) {
        log.info("Getting repository '$repoId'")
        Map<String, Object> repoResponseAsMap = client.get(nexusUrl + "/staging/repository/$repoId")
        return repoResponseAsMap
    }

    private void parseResponseAndThrowExceptionIfInTransition(Map<String, Object> repoResponseAsMap, String repoId) {
        if (repoResponseAsMap.transitioning == false) {
            return
        }
        throw new RepositoryInTransitionException(repoId, (String)repoResponseAsMap.type)
    }

    private RepositoryState parseRepoStateFromRepsponse(Map<String, Object> repoResponseAsMap) {
        return RepositoryState.parseString((String)repoResponseAsMap.type)
    }

    private RepositoryState handleGivenRepositoryNotFoundOrRethrowException(String repoId, NexusHttpResponseException responseException) {
        if (responseException.statusCode == NOT_FOUND_RESPONSE_CODE && isErrorMessageForGivenNotFoundRepository(repoId, responseException.message)) {
            return RepositoryState.NOT_FOUND
        } else {
            throw responseException;
        }
    }

    private boolean isErrorMessageForGivenNotFoundRepository(String repoId, String message) {
        return message.contains("No such repository") && message.contains(repoId)
    }
}
