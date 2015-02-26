package io.codearte.gradle.nexus.infra

import groovy.transform.CompileStatic

@CompileStatic
class WrongNumberOfStagingProfiles extends NexusStagingException {

    final int numberOfProfiles
    final String packageGroup

    WrongNumberOfStagingProfiles(int numberOfProfiles, String packageGroup) {
        super("Wrong number of received staging profiles for '$packageGroup'. Expected 1, received $numberOfProfiles")
        this.numberOfProfiles = numberOfProfiles
        this.packageGroup = packageGroup
    }
}
