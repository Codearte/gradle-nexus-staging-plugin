package io.codearte.gradle.nexus

import org.gradle.api.Project
import org.gradle.api.internal.plugins.PluginApplicationException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Issue
import spock.lang.Specification
import spock.util.Exceptions

@Issue("https://github.com/Codearte/gradle-nexus-staging-plugin/issues/116")
class ApplyOnSubprojectSpec extends Specification { //ProjectSpec from nebula could be used if moved to funcTest

    @Rule
    public TemporaryFolder tmpProjectDir = new TemporaryFolder()

    private Project project

    def setup() {
        project = ProjectBuilder.builder().withProjectDir(tmpProjectDir.root).build()
    }

    def "successfully apply on root project in single module project"() {
        when:
            project.apply(plugin: NexusStagingPlugin)
        then:
            noExceptionThrown()
    }

    def "successfully apply on root project in multi module project"() {
        given:
            createAddAndReturnSubproject(project)
        when:
            project.apply(plugin: NexusStagingPlugin)
        then:
            noExceptionThrown()
    }

    def "fail if applied on subproject in default configuration"() {
        given:
            Project subproject = createAddAndReturnSubproject(project)
        when:
            subproject.apply(plugin: NexusStagingPlugin)
        then:
            PluginApplicationException e = thrown()
            String rootCauseMessage = Exceptions.getRootCause(e).message
            rootCauseMessage.contains("Nexus staging plugin should ONLY be applied on the ROOT project in a build.")
        and:
            rootCauseMessage.contains(ApplyOnRootProjectEnforcer.DISABLE_APPLY_ON_ROOT_PROJECT_ENFORCEMENT_PROPERTY_NAME)
    }

    def "not fail if applied on subproject project with override switch turned on (#propertyValue)"() {
        given:
            Project subproject = createAddAndReturnSubproject(project)
        and:
            project.extensions.extraProperties.set(ApplyOnRootProjectEnforcer.DISABLE_APPLY_ON_ROOT_PROJECT_ENFORCEMENT_PROPERTY_NAME, propertyValue)
        when:
            subproject.apply(plugin: NexusStagingPlugin)
        then:
            noExceptionThrown()
        where:
            propertyValue << [true, "true", 1, "anything"]
    }

    //TODO: It should be rather test for more generic GradleUtil.isPropertyNotDefinedOrFalse() mechanism
    def "fail if applied on subproject project with override switch explicitly disabled (#propertyValue)"() {
        given:
            Project subproject = createAddAndReturnSubproject(project)
        and:
            project.extensions.extraProperties.set(ApplyOnRootProjectEnforcer.DISABLE_APPLY_ON_ROOT_PROJECT_ENFORCEMENT_PROPERTY_NAME, propertyValue)
        when:
            subproject.apply(plugin: NexusStagingPlugin)
        then:
            PluginApplicationException e = thrown()
            Exceptions.getRootCause(e).message.contains("Nexus staging plugin should ONLY be applied on the ROOT project in a build.")
        where:
            propertyValue << ["false", false]
    }

    private Project createAddAndReturnSubproject(Project parentProject, String name = "subproject") {
        Project subproject = ProjectBuilder.builder().withName(name).withProjectDir(new File(tmpProjectDir.root, name)).withParent(parentProject).build()
        parentProject.subprojects.add(subproject)
        return subproject
    }
}
