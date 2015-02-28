package io.codearte.gradle.nexus.functional

import nebula.test.IntegrationSpec

class BaseNexusStagingFunctionalSpec extends IntegrationSpec {

    protected String nexusPassword

    void setup() {
        fork = true //to prevent ClassCastException: org.apache.xerces.parsers.XIncludeAwareParserConfiguration cannot be cast to org.apache.xerces.xni.parser.XMLParserConfiguration
        nexusPassword = ''
    }

    protected String getApplyPluginBlock() {
        return """
                apply plugin: 'io.codearte.nexus-staging'

                buildscript {
                    repositories {
                        mavenCentral()
                    }
                }
        """
    }

    protected String getDefaultConfigurationClosure() {
        return """
                nexusStaging {
                    username = "codearte"
                    password = '$nexusPassword'
                    packageGroup = "io.codearte"
                }
        """
    }
}
