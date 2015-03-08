package io.codearte.gradle.nexus

import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.gradle.api.Incubating

@CompileStatic
@Slf4j
@ToString(includeFields = true, includeNames = true, includePackage = false)
class NexusStagingExtension {

    String serverUrl
    String username
    String password
    String packageGroup
    String stagingProfileId //since 0.4.1
    @Incubating Integer numberOfRetries
    @Incubating Integer delayBetweenRetriesInMillis

    //Deprecated since 0.4.1.
    void setNexusUrl(String nexusUrl) {
        log.warn("DEPRECATION WARNING. nexusUrl property has been deprecated and will be removed in the future versions.")
        serverUrl = nexusUrl
    }
}
