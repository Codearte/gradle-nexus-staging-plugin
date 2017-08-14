package io.codearte.gradle.nexus

import io.codearte.gradle.nexus.logic.OperationRetrier
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.maven.MavenDeployer
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Upload

import java.lang.invoke.MethodHandles

class NexusStagingPlugin implements Plugin<Project> {

    private final static Logger log =  Logging.getLogger(MethodHandles.lookup().lookupClass())

    private static final String GET_STAGING_PROFILE_TASK_NAME = "getStagingProfile"
    private static final String CLOSE_REPOSITORY_TASK_NAME = "closeRepository"
    private static final String RELEASE_REPOSITORY_TASK_NAME = "releaseRepository"
    private static final String CLOSE_AND_RELEASE_REPOSITORY_TASK_NAME = "closeAndReleaseRepository"

    private static final Set<Class> STAGING_TASK_CLASSES = [GetStagingProfileTask, CloseRepositoryTask, ReleaseRepositoryTask]

    private static final String NEXUS_USERNAME_PROPERTY = 'nexusUsername'
    private static final String NEXUS_PASSWORD_PROPERTY = 'nexusPassword'

    private Project project
    private NexusStagingExtension extension

    @Override
    void apply(Project project) {
        this.project = project
        this.extension = createAndConfigureExtension(project)
        emitWarningIfAppliedNotToRootProject(project)
        createAndConfigureGetStagingProfileTask(project)
        def closeRepositoryTask = createAndConfigureCloseRepositoryTask(project)
        def releaseRepositoryTask = createAndConfigureReleaseRepositoryTask(project)
        releaseRepositoryTask.mustRunAfter(closeRepositoryTask)
        def closeAndReleaseRepositoryTask = createAndConfigureCloseAndReleaseRepositoryTask(project)
        closeAndReleaseRepositoryTask.dependsOn(closeRepositoryTask, releaseRepositoryTask)
        tryToDetermineCredentials(project, extension)
        //just during the transition period - see https://github.com/Codearte/gradle-nexus-staging-plugin/issues/50
        new LegacyTasksCreator().createAndConfigureLegacyTasks(project)
    }

    private void emitWarningIfAppliedNotToRootProject(Project project) {
        if (project != project.rootProject) {
            project.logger.warn("WARNING. Nexus staging plugin should only be applied to the root project in build.")
        }
    }

    private NexusStagingExtension createAndConfigureExtension(Project project) {
        def extension = project.extensions.create("nexusStaging", NexusStagingExtension)
        extension.with {
            serverUrl = "https://oss.sonatype.org/service/local/"
            numberOfRetries = OperationRetrier.DEFAULT_NUMBER_OF_RETRIES
            delayBetweenRetriesInMillis = OperationRetrier.DEFAULT_DELAY_BETWEEN_RETRIES_IN_MILLIS
        }
        return extension
    }

    private void createAndConfigureGetStagingProfileTask(Project project) {
        GetStagingProfileTask task = project.tasks.create(GET_STAGING_PROFILE_TASK_NAME, GetStagingProfileTask)
        setTaskDescriptionAndGroup(task, "Gets a staging profile id in Nexus - a diagnostic task")
        setTaskDefaultsAndDescription(task)
    }

    private CloseRepositoryTask createAndConfigureCloseRepositoryTask(Project project) {
        CloseRepositoryTask task = project.tasks.create(CLOSE_REPOSITORY_TASK_NAME, CloseRepositoryTask)
        setTaskDescriptionAndGroup(task, "Closes an open artifacts repository in Nexus")
        setTaskDefaultsAndDescription(task)
        return task
    }

    private ReleaseRepositoryTask createAndConfigureReleaseRepositoryTask(Project project) {
        ReleaseRepositoryTask task = project.tasks.create(RELEASE_REPOSITORY_TASK_NAME, ReleaseRepositoryTask)
        setTaskDescriptionAndGroup(task, "Releases a closed artifacts repository in Nexus")
        setTaskDefaultsAndDescription(task)
        return task
    }

    private Task createAndConfigureCloseAndReleaseRepositoryTask(Project project) {
        Task task = project.tasks.create(CLOSE_AND_RELEASE_REPOSITORY_TASK_NAME, DefaultTask)   //TODO: DefaultTask is redundant?
        setTaskDescriptionAndGroup(task, "Closes and releases an artifacts repository in Nexus")
        return task
    }

    private void setTaskDescriptionAndGroup(Task task, String taskDescription) {
        task.with {
            description = taskDescription
            group = "release"
        }
    }

    private void setTaskDefaultsAndDescription(BaseStagingTask task) {
        task.conventionMapping.with {
            serverUrl = { extension.serverUrl }
            username = { extension.username }
            password = { extension.password }
            packageGroup = {
                if (extension.packageGroup) {
                    return extension.packageGroup
                } else {
                    return getProjectGroupOrNull(project)
                }
            }
            stagingProfileId = { extension.stagingProfileId }
            numberOfRetries = { extension.numberOfRetries }
            delayBetweenRetriesInMillis = { extension.delayBetweenRetriesInMillis }
            repositoryDescription = { extension.repositoryDescription}
        }
    }

    private String getProjectGroupOrNull(Project project) {
        log.debug("project.group: '{}', class: {}", project.getGroup(), project.getGroup()?.class)
        return project.getGroup() ?: null
    }

    //TODO: Extract to separate class
    private void tryToDetermineCredentials(Project project, NexusStagingExtension extension) {
        project.afterEvaluate {
            project.gradle.taskGraph.whenReady { TaskExecutionGraph taskGraph ->
                if (isAnyOfStagingTasksInTaskGraph(taskGraph)) {
                    tryToGetCredentialsFromUploadArchivesTask(project, extension)
                    tryToGetCredentialsFromGradleProperties(project, extension)
                } else {
                    project.logger.debug("No staging task will be executed - skipping determination of Nexus credentials")
                }
            }
        }
    }

    private boolean isAnyOfStagingTasksInTaskGraph(TaskExecutionGraph taskGraph) {
        return taskGraph.allTasks.find { Task task ->
            STAGING_TASK_CLASSES.find { Class stagingTaskClass ->
                //GetStagingProfileTask_Decorated is not assignable from GetStagingProfileTask, but its superclass is GetStagingProfileTask...
                task.getClass().superclass.isAssignableFrom(stagingTaskClass)
            }
        }
    }

    private void tryToGetCredentialsFromUploadArchivesTask(Project project, NexusStagingExtension extension) {
        if (extension.username != null && extension.password != null) {
            return  //username and password already set
        }

        Upload uploadTask = (Upload) project.tasks.findByPath("uploadArchives")
        uploadTask?.repositories?.withType(MavenDeployer)?.each { MavenDeployer deployer ->
            project.logger.debug("Trying to read credentials from repository '${deployer.name}'")
            def authentication = deployer.repository?.authentication //Not to use class names as maven-ant-task is not on classpath when plugin is executed
            if (authentication?.userName != null) {
                extension.username = authentication.userName
                extension.password = authentication.password
                project.logger.info("Using username '${extension.username}' and password from repository '${deployer.name}'")
                return  //from each
            }
        }
    }

    private void tryToGetCredentialsFromGradleProperties(Project project, NexusStagingExtension extension) {
        if (extension.username == null && project.hasProperty(NEXUS_USERNAME_PROPERTY)) {
            extension.username = project.property(NEXUS_USERNAME_PROPERTY)
            project.logger.info("Using username '${extension.username}' from Gradle property '${NEXUS_USERNAME_PROPERTY}'")
        }
        if (extension.password == null && project.hasProperty(NEXUS_PASSWORD_PROPERTY)) {
            extension.password = project.property(NEXUS_PASSWORD_PROPERTY)
            project.logger.info("Using password '*****' from Gradle property '${NEXUS_PASSWORD_PROPERTY}'")
        }
    }

    /**
     * See https://github.com/Codearte/gradle-nexus-staging-plugin/issues/50 for more details.
     */
    @Deprecated
    private static class LegacyTasksCreator {

        private static final String RELEASE_REPOSITORY_OLD_TASK_NAME = "promoteRepository"
        private static final String CLOSE_AND_RELEASE_REPOSITORY_OLD_TASK_NAME = "closeAndPromoteRepository"

        void createAndConfigureLegacyTasks(Project project) {
            createDeprecatedTaskDependingOnNewOne(project, RELEASE_REPOSITORY_OLD_TASK_NAME, RELEASE_REPOSITORY_TASK_NAME)
            createDeprecatedTaskDependingOnNewOne(project, CLOSE_AND_RELEASE_REPOSITORY_OLD_TASK_NAME, CLOSE_AND_RELEASE_REPOSITORY_TASK_NAME)
        }

        private void createDeprecatedTaskDependingOnNewOne(Project project, String deprecatedTaskName, String newTaskName) {
            project.tasks.create(deprecatedTaskName).with { task ->
                description = "DEPRECATION WARNING. This task is DEPRECATED. Use '$newTaskName' instead."
                group = "release"
                dependsOn project.tasks.getByName(newTaskName)

                project.gradle.taskGraph.whenReady { TaskExecutionGraph taskGraph ->
                    if (taskGraph.hasTask(task)) {
                        log.warn("DEPRECATION WARNING. Task '$deprecatedTaskName' is deprecated. Switch to '$newTaskName'.")
                    }
                }
            }
        }
    }
}
