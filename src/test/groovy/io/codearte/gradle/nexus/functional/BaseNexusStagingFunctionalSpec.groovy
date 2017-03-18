package io.codearte.gradle.nexus.functional

import nebula.test.IntegrationSpec

class BaseNexusStagingFunctionalSpec extends IntegrationSpec {

    protected String nexusPassword
    protected String nexusUsername
    protected String packageGroup

    void setup() {
        fork = true //to prevent ClassCastException: org.apache.xerces.parsers.XIncludeAwareParserConfiguration cannot be cast to org.apache.xerces.xni.parser.XMLParserConfiguration
        nexusUsername = 'nexus-at'
        nexusPassword = ''
        packageGroup = 'io.gitlab.nexus-at'
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
                    username = '$nexusUsername'
                    password = '$nexusPassword'
                    packageGroup = '$packageGroup'
                }
        """
    }
}
