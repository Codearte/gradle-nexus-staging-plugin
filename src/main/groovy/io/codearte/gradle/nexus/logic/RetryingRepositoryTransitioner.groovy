package io.codearte.gradle.nexus.logic

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.codearte.gradle.nexus.exception.UnexpectedRepositoryState

@CompileStatic
@Slf4j
class RetryingRepositoryTransitioner {

    private final AbstractRepositoryTransitioner repositoryCloser
    private final RepositoryStateFetcher byIdRepositoryFetcher
    private final OperationRetrier<String> retrier

    RetryingRepositoryTransitioner(AbstractRepositoryTransitioner repositoryCloser, RepositoryStateFetcher byIdRepositoryFetcher, OperationRetrier<String> retrier) {
        this.repositoryCloser = repositoryCloser
        this.byIdRepositoryFetcher = byIdRepositoryFetcher
        this.retrier = retrier
    }

    void performWithRepositoryIdAndStagingProfileId(String repoId, String stagingProfileId) {
        repositoryCloser.performWithRepositoryIdAndStagingProfileId(repoId, stagingProfileId)
        String state = retrier.doWithRetry { byIdRepositoryFetcher.getNonTransitioningRepositoryStateById(repoId) }
        if (state != repositoryCloser.desiredAfterTransitionRepositoryState().value()) {    //TODO: How to detect missing ".value()"?
            throw new UnexpectedRepositoryState(repoId, state, repositoryCloser.desiredAfterTransitionRepositoryState())
        }
    }
}
