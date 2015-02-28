package io.codearte.gradle.nexus

import org.gradle.api.Plugin
import org.gradle.api.Project

class NexusStagingPlugin implements Plugin<Project> {

    private Project project
    private NexusStagingExtension extension

    @Override
    void apply(Project project) {
        this.project = project
        this.extension = createAndConfigureExtension(project)
        emitWarningIfAppliedNotToRootProject(project)
        createAndConfigureGetStagingProfileTask2(project)
        def closeRepositoryTask = createAndConfigureCloseRepositoryTask(project)
        def promoteRepositoryTask = createAndConfigurePromoteRepositoryTask(project)
        promoteRepositoryTask.mustRunAfter(closeRepositoryTask)
    }

    void emitWarningIfAppliedNotToRootProject(Project project) {
        if (project != project.rootProject) {
            project.logger.warn("WARNING. Nexus staging plugin should only be applied to the root project in build.")
        }
    }

    NexusStagingExtension createAndConfigureExtension(Project project) {
        def extension = project.extensions.create("nexusStaging", NexusStagingExtension)
        extension.with {
            serverUrl = "https://oss.sonatype.org/service/local/"
        }
        return extension
    }

    void createAndConfigureGetStagingProfileTask2(Project project) {
        GetStagingProfileTask2 task = project.tasks.create("getStagingProfile", GetStagingProfileTask2)
        task.with {
            description = "Gets staging profile id in Nexus - diagnostic tasks"
            group = "release"
        }
        setTaskDefaults(task)
    }

    CloseRepositoryTask createAndConfigureCloseRepositoryTask(Project project) {
        CloseRepositoryTask task = project.tasks.create("closeRepository", CloseRepositoryTask)
        task.with {
            description = "Closes open artifacts repository in Nexus"
            group = "release"
        }
        setTaskDefaults(task)
        return task
    }

    PromoteRepositoryTask createAndConfigurePromoteRepositoryTask(Project project) {
        PromoteRepositoryTask task = project.tasks.create("promoteRepository", PromoteRepositoryTask)
        task.with {
            description = "Promotes/releases closed artifacts repository in Nexus"
            group = "release"
        }
        setTaskDefaults(task)
        return task
    }

    void setTaskDefaults(BaseStagingTask task) {
        task.conventionMapping.with {
            serverUrl = { extension.serverUrl }
            username = { extension.username }
            password = { extension.password }
            packageGroup = { extension.packageGroup }
            stagingProfileId = { extension.stagingProfileId }
        }
    }
}
