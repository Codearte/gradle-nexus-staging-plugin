package io.codearte.gradle.nexus

import groovy.transform.CompileStatic

@CompileStatic
class PasswordUtil {

    //Temporary hack to read nexus password in e2e tests
    static String tryToReadNexusPassword() {
        String nexusPass = System.getenv("nexusPassword")
        if (nexusPass == null) {
            throw new RuntimeException("Nexus password is not set in a system variable")
        }
        return nexusPass
    }
}
