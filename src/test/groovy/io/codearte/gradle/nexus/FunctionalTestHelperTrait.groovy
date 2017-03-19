package io.codearte.gradle.nexus

import groovy.transform.CompileStatic

@CompileStatic
trait FunctionalTestHelperTrait implements FunctionalTestConstants {

    private static final String NEXUS_USERNAME_AT_ENVIRONMENT_VARIABLE_NAME = 'nexusUsernameAT'
    private static final String NEXUS_PASSWORD_AT_ENVIRONMENT_VARIABLE_NAME = 'nexusPasswordAT'

    String getNexusUsernameAT() {
        return System.getenv(NEXUS_USERNAME_AT_ENVIRONMENT_VARIABLE_NAME) ?: 'nexus-at'
    }

    //Temporary hack to read nexus password in e2e tests
    String tryToReadNexusPasswordAT() {
        return System.getenv(NEXUS_PASSWORD_AT_ENVIRONMENT_VARIABLE_NAME) ?: { throw new RuntimeException(
                "Nexus password for AT tests is not set in a system variable '$NEXUS_PASSWORD_AT_ENVIRONMENT_VARIABLE_NAME'") }()
    }
}
