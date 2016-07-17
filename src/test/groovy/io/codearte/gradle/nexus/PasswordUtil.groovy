package io.codearte.gradle.nexus

import groovy.transform.CompileStatic

@CompileStatic
class PasswordUtil {

    //Temporary hack to read nexus password in e2e tests
    static String tryToReadNexusPassword() {
        return System.getenv("nexusPassword") ?: { throw new RuntimeException("Nexus password is not set in a system variable") }()
    }
}
