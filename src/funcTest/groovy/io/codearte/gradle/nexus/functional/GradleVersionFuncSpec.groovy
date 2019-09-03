package io.codearte.gradle.nexus.functional

import com.google.common.base.Predicate
import com.google.common.base.Predicates
import io.codearte.gradle.nexus.NexusStagingPlugin
import nebula.test.functional.ExecutionResult
import nebula.test.functional.GradleRunner
import org.gradle.internal.jvm.Jvm
import org.gradle.util.GradleVersion
import spock.util.Exceptions

import java.util.regex.Pattern

/**
 * Verifies that plugin doesn't fail during Gradle initialization (e.g. due to ClassCastException error) with different "supported" Gradle versions.
 */
class GradleVersionFuncSpec extends BaseNexusStagingFunctionalSpec implements FunctionalSpecHelperTrait {

    //Officially 5.0, but 4.10.2 works fine with that plugin
    private static final GradleVersion MINIMAL_STABLE_JAVA11_COMPATIBLE_GRADLE_VERSION = GradleVersion.version("4.10.2")
    private static final GradleVersion LATEST_GRADLE_VERSION = GradleVersion.version("5.6.1")

    def "should not fail on #legacyModeMessage plugin logic initialization issue with Gradle #requestedGradleVersion"() {
        given:
            gradleVersion = requestedGradleVersion.version
            classpathFilter = Predicates.and(GradleRunner.CLASSPATH_DEFAULT, FILTER_SPOCK_JAR)
            memorySafeMode = true   //shutdown Daemon after a few seconds of inactivity
        and:
            buildFile << """
                ${getApplyPluginBlock()}
                ${getPluginConfigurationWithNotExistingNexusServer()}

                ${getLegacyModeConfigurationIfRequested(isInLegacyMode as boolean)} //"as" for Idea
            """.stripIndent()
        when:
            ExecutionResult result = runTasksWithFailure('getStagingProfile')
        then:
            result.wasExecuted(':getStagingProfile')
        and:
            result.failure.cause.message.contains("Execution failed for task ':getStagingProfile'")
            Exceptions.getRootCause(result.failure).with {
                getClass() == ConnectException
                message.contains("Connection refused")
            }
            println result.standardOutput
        where:
            [requestedGradleVersion, isInLegacyMode] << [applyJavaCompatibilityAdjustment(resolveRequestedGradleVersions()).unique(), [false, true]].combinations()
            legacyModeMessage = isInLegacyMode ? "(legacy)" : ""
    }

    private String getPluginConfigurationWithNotExistingNexusServer() {
        return """
                nexusStaging {
                    packageGroup = "fake.one"
                    serverUrl = "http://localhost:61942/"
                }
                """
    }

    private String getLegacyModeConfigurationIfRequested(boolean isInLegacyMode) {
        return """
                apply plugin: 'com.bmuschko.nexus'
                apply plugin: 'io.codearte.nexus-upload-staging'

                buildscript {
                    dependencies {
                        classpath 'com.bmuschko:gradle-nexus-plugin:2.3.1'
                    }
                }
        """
    }

    //To prevent failure when Spock for Groovy 2.4 is run with Groovy 2.3 delivered with Gradle <2.8
    //Spock is not needed in this artificial project - just the test classpath leaks to Gradle instance started by Nebula
    private static final Pattern SPOCK_JAR_PATTERN = Pattern.compile(".*spock-core-1\\..*.jar")
    private static final Predicate<URL> FILTER_SPOCK_JAR = { URL url ->
        return !url.toExternalForm().matches(SPOCK_JAR_PATTERN)
    } as Predicate<URL>

    private List<GradleVersion> resolveRequestedGradleVersions() {
        return [GradleVersion.version(NexusStagingPlugin.MINIMAL_SUPPORTED_GRADLE_VERSION), MINIMAL_STABLE_JAVA11_COMPATIBLE_GRADLE_VERSION,
                LATEST_GRADLE_VERSION].unique()
    }

    //Java 9 testing mechanism taken after pitest-gradle-plugin - https://github.com/szpak/gradle-pitest-plugin

    //Jvm class from Spock doesn't work with Java 9 stable releases - otherwise @IgnoreIf could be used - TODO: check with Spock 1.2
    private List<GradleVersion> applyJavaCompatibilityAdjustment(List<GradleVersion> requestedGradleVersions) {
        if (!Jvm.current().javaVersion.isJava9Compatible()) {
            //All supported versions should be Java 8 compatible
            return requestedGradleVersions
        }
        return leaveJava11CompatibleGradleVersionsOnly(requestedGradleVersions)
    }

    private List<GradleVersion> leaveJava11CompatibleGradleVersionsOnly(List<GradleVersion> requestedGradleVersions) {
        List<GradleVersion> java11CompatibleGradleVersions = requestedGradleVersions.findAll {
            it >= MINIMAL_STABLE_JAVA11_COMPATIBLE_GRADLE_VERSION
        }
        if (java11CompatibleGradleVersions.size() < 1) {
            java11CompatibleGradleVersions.add(MINIMAL_STABLE_JAVA11_COMPATIBLE_GRADLE_VERSION)
        }
        return java11CompatibleGradleVersions
    }
}
