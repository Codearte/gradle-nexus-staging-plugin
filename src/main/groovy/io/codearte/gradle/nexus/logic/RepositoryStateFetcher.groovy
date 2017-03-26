package io.codearte.gradle.nexus.logic

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j
import io.codearte.gradle.nexus.exception.RepositoryInTransitionException

@InheritConstructors
@CompileStatic
@Slf4j
class RepositoryStateFetcher extends BaseOperationExecutor {

    //TODO: Map 404 to State.NOT_FOUND (for drop and drop after release operations)
    RepositoryState getNonTransitioningRepositoryStateById(String repoId) {
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
}
