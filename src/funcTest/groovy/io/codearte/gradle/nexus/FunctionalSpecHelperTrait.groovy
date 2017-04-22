package io.codearte.gradle.nexus

trait FunctionalSpecHelperTrait {

    String nexusPassword
    String nexusUsername
    String packageGroup

    void setup() {
        nexusUsername = 'nexus-at'
        nexusPassword = ''
        packageGroup = 'io.gitlab.nexus-at'
    }

    String getApplyPluginBlock() {
        return """
                apply plugin: 'io.codearte.nexus-staging'

                buildscript {
                    repositories {
                        mavenCentral()
                    }
                }
        """
    }

    String getDefaultConfigurationClosure() {
        return """
                nexusStaging {
                    username = '$nexusUsername'
                    password = '$nexusPassword'
                    packageGroup = '$packageGroup'
                }
        """
    }
}
