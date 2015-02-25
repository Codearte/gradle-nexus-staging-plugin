package io.codearte.gradle.nexus.functional

import io.codearte.gradle.nexus.PasswordUtil
import nebula.test.IntegrationSpec

class BaseNexusStagingFunctionalSpec extends IntegrationSpec {

    protected String nexusPassword

    void setup() {
        fork = true //to prevent ClassCastException: org.apache.xerces.parsers.XIncludeAwareParserConfiguration cannot be cast to org.apache.xerces.xni.parser.XMLParserConfiguration
        nexusPassword = PasswordUtil.tryToReadNexusPassword()
    }
}
