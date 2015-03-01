package io.codearte.gradle.nexus.logic

import com.google.common.annotations.VisibleForTesting
import groovy.transform.PackageScope
import groovy.util.logging.Slf4j
import io.codearte.gradle.nexus.infra.WrongNumberOfRepositories

@Slf4j
class OperationRetrier<T> {

    public static final int DEFAULT_NUMBER_OF_RETRIES = 5
    public static final int DEFAULT_DELAY_BETWEEN_RETRIES_IN_MILLIS = 1000

    private final int numberOfRetries
    private final int delayBetweenRetries

    OperationRetrier(int numberOfRetries = DEFAULT_NUMBER_OF_RETRIES, int delayBetweenRetries = DEFAULT_DELAY_BETWEEN_RETRIES_IN_MILLIS) {
        this.numberOfRetries = numberOfRetries
        this.delayBetweenRetries = delayBetweenRetries
    }

    public T doWithRetry(Closure<T> operation) {
        int counter = 0
        while (true) {
            try {
                counter++
                log.debug("Attempt $counter/$numberOfRetries...")
                return operation()
            } catch (WrongNumberOfRepositories | IllegalArgumentException e) { //Exceptions to catch could be configurable if needed
                String message = "Attempt $counter/$numberOfRetries failed. ${e.getClass().getSimpleName()} was thrown with message '${e.message}'"
                if (counter > numberOfRetries) {
                    log.info("$message. Giving up.")
                    throw e
                } else {
                    log.info("$message. Waiting $delayBetweenRetries ms before next retry.")
                    waitBeforeNextAttempt()
                }
            }
        }
    }

    @VisibleForTesting
    @PackageScope
    void waitBeforeNextAttempt() {
        //sleep() hangs the thread, but in that case it doesn't matter - switch to https://github.com/nurkiewicz/async-retry/ if really needed
        sleep(delayBetweenRetries)
    }
}
