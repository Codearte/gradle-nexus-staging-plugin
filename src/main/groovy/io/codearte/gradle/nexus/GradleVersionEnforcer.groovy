package io.codearte.gradle.nexus

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.gradle.api.GradleException
import org.gradle.api.Incubating
import org.gradle.api.Project
import org.gradle.util.GradleVersion

@CompileStatic
@Slf4j
@Incubating
//TODO: Improve testability - current Gradle version cannot be easily mocked
class GradleVersionEnforcer {

    private static final String DISABLE_GRADLE_VERSION_ENFORCEMENT_PROPERTY_NAME = 'gnsp.disableGradleVersionEnforcement'

    private final GradleVersion minimalSupportedVersion
    private final String propertyNameToDisable

    //TODO: Switch to @TuppleConstructor(defaults = false) while completely migrated to Gradle 5.x (with Groovy 2.5)
    private GradleVersionEnforcer(GradleVersion minimalSupportedVersion, String propertyNameToDisable) {
        this.minimalSupportedVersion = minimalSupportedVersion
        this.propertyNameToDisable = propertyNameToDisable
    }

    static GradleVersionEnforcer defaultEnforcer(GradleVersion minimalSupportedVersion) {
        return new GradleVersionEnforcer(minimalSupportedVersion, DISABLE_GRADLE_VERSION_ENFORCEMENT_PROPERTY_NAME)
    }

    void failBuildWithMeaningfulErrorIfAppliedOnTooOldGradleVersion(Project project) {
        if (GradleVersion.current() < minimalSupportedVersion) {
            log.warn("WARNING. The 'io.codearte.nexus-staging' plugin requires ${minimalSupportedVersion.version} to run properly " +
                "(detected: ${GradleVersion.current()}). Please upgrade your Gradle or downgrade the plugin version.")

            if (GradleUtil.isPropertyNotDefinedOrFalse(project, propertyNameToDisable)) {
                log.warn("Aborting the build with the meaningful error message to prevent confusion. If you are sure it is an error, " +
                    "please report it in the plugin issue tracker and in the meantime use '-D${propertyNameToDisable}' to disable this check")

                throw new GradleException("'io.codearte.nexus-staging' requires Gradle ${minimalSupportedVersion.version}")
            }
        }
    }
}
