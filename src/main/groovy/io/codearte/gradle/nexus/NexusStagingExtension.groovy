package io.codearte.gradle.nexus

import groovy.transform.CompileStatic

@CompileStatic
class NexusStagingExtension {

    String nexusUrl
    String username
    String password
    String packageGroup
}
