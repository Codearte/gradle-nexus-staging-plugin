package io.codearte.gradle.nexus

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@CompileStatic
@Slf4j
class NexusStagingExtension {

    String serverUrl
    String username
    String password
    String packageGroup
    String stagingProfileId //since 0.4.1

    //Deprecated since 0.4.1.
    void setNexusUrl(String nexusUrl) {
        log.warn("DEPRECATION WARNING. nexusUrl property has been deprecated and will be removed in the future")
        serverUrl = nexusUrl
    }
}
