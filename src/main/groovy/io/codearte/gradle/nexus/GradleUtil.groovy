package io.codearte.gradle.nexus

import groovy.transform.CompileStatic
import org.gradle.api.Project

@CompileStatic
class GradleUtil {

    static boolean isPropertyNotDefinedOrFalse(Project project, String propertyName) {
        return !project.hasProperty(propertyName) || project.findProperty(propertyName) == "false"
    }
}
