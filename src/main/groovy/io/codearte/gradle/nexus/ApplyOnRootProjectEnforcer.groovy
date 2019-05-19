package io.codearte.gradle.nexus

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.util.logging.Slf4j
import org.gradle.api.GradleException
import org.gradle.api.Incubating
import org.gradle.api.Project

@CompileStatic
@Slf4j
@Incubating
class ApplyOnRootProjectEnforcer {

    @PackageScope   //for testing
    static final String DISABLE_APPLY_ON_ROOT_PROJECT_ENFORCEMENT_PROPERTY_NAME = 'gnsp.disableApplyOnRootProjectEnforcement'

    private final String propertyNameToDisable

    //TODO: Switch to @TuppleConstructor(defaults = false) while completely migrated to Gradle 5.x (with Groovy 2.5)
    ApplyOnRootProjectEnforcer() {
        this.propertyNameToDisable = DISABLE_APPLY_ON_ROOT_PROJECT_ENFORCEMENT_PROPERTY_NAME
    }

    void failBuildWithMeaningfulErrorIfAppliedNotOnRootProject(Project project) {
        if ((project != project.rootProject) && !isPartOfDeterminingPrecompiledScriptPluginAccessorsBuild(project)) {
            if (GradleUtil.isPropertyNotDefinedOrFalse(project, propertyNameToDisable)) {   //See https://github.com/Codearte/gradle-nexus-staging-plugin/issues/116
                throw new GradleException("Nexus staging plugin should ONLY be applied on the ROOT project in a build. " +
                    "See https://github.com/Codearte/gradle-nexus-staging-plugin/issues/47 for explanation. If you really know what you are doing" +
                    "it can be overridden with setting '${DISABLE_APPLY_ON_ROOT_PROJECT_ENFORCEMENT_PROPERTY_NAME}' property.")
            } else {
                log.info("Overriding protection against applying on non-root project. It may cause execution errors if used improperly.")
            }
        }
    }

    /**
     * For precompiled Kotlin script plugins the plugin is automatically applied to a non-root project to
     * determine which extensions and conventions it adds to a project. This needs to be allowed and
     * currently cannot controlled by some project property as those are not forwarded to this build.
     *
     * https://github.com/Codearte/gradle-nexus-staging-plugin/issues/47#issuecomment-491474045
     *
     * @param project the project to test
     * @return whether the given project is part of a build to determine the precompiled script plugin accessors
     */
    private isPartOfDeterminingPrecompiledScriptPluginAccessorsBuild(Project project) {
        project.projectDir.absolutePath =~ '([\\\\/])build\\1tmp\\1generatePrecompiledScriptPluginAccessors\\1'
    }
}
