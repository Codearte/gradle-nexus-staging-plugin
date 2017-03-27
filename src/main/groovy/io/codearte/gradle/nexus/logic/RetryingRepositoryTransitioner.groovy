package io.codearte.gradle.nexus.logic

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.codearte.gradle.nexus.exception.UnexpectedRepositoryState

@CompileStatic
@Slf4j
class RetryingRepositoryTransitioner {

    private final AbstractRepositoryTransitioner repositoryTransitioner
    private final RepositoryStateFetcher repositoryStateFetcher
    private final OperationRetrier<RepositoryState> retrier

    RetryingRepositoryTransitioner(AbstractRepositoryTransitioner repositoryTransitioner, RepositoryStateFetcher repositoryStateFetcher,
                                   OperationRetrier<RepositoryState> retrier) {
        this.repositoryTransitioner = repositoryTransitioner
        this.repositoryStateFetcher = repositoryStateFetcher
        this.retrier = retrier
    }

    void performWithRepositoryIdAndStagingProfileId(String repoId, String stagingProfileId) {
        repositoryTransitioner.performWithRepositoryIdAndStagingProfileId(repoId, stagingProfileId)
        RepositoryState state = retrier.doWithRetry { repositoryStateFetcher.getNonTransitioningRepositoryStateById(repoId) }
        if (state != repositoryTransitioner.desiredAfterTransitionRepositoryState()) {
            throw new UnexpectedRepositoryState(repoId, state, repositoryTransitioner.desiredAfterTransitionRepositoryState())
        }
    }
}
