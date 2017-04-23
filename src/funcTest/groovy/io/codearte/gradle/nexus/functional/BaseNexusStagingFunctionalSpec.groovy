package io.codearte.gradle.nexus.functional

import groovy.transform.CompileStatic
import nebula.test.IntegrationSpec

@CompileStatic
abstract class BaseNexusStagingFunctionalSpec extends IntegrationSpec {

    void setup() {
        fork = true //to prevent ClassCastException: org.apache.xerces.parsers.XIncludeAwareParserConfiguration cannot be cast to org.apache.xerces.xni.parser.XMLParserConfiguration
    }
}
