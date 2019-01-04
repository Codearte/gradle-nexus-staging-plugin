package io.codearte.gradle.nexus

import io.codearte.gradle.nexus.logic.OperationRetrier
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.maven.MavenDeployer
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Upload
import org.gradle.util.GradleVersion

import java.lang.invoke.MethodHandles

@SuppressWarnings("UnstableApiUsage")
class NexusStagingPlugin implements Plugin<Project> {

    public static final String MINIMAL_SUPPORTED_GRADLE_VERSION = "4.8" //public as used also in regression tests

    private final static Logger log =  Logging.getLogger(MethodHandles.lookup().lookupClass())

    private static final String GET_STAGING_PROFILE_TASK_NAME = "getStagingProfile" //TODO: Move to classes with task?
    private static final String CREATE_REPOSITORY_TASK_NAME = "createRepository"
    private static final String CLOSE_REPOSITORY_TASK_NAME = "closeRepository"
    private static final String RELEASE_REPOSITORY_TASK_NAME = "releaseRepository"
    private static final String CLOSE_AND_RELEASE_REPOSITORY_TASK_NAME = "closeAndReleaseRepository"

    private static final Set<Class> STAGING_TASK_CLASSES = [GetStagingProfileTask, CreateRepositoryTask, CloseRepositoryTask,
                                                            ReleaseRepositoryTask]

    private static final String NEXUS_USERNAME_PROPERTY = 'nexusUsername'
    private static final String NEXUS_PASSWORD_PROPERTY = 'nexusPassword'

    private static final String DEFAULT_BASE_NEXUS_SERVER_URL = 'https://oss.sonatype.org/service/local/'
    private static final String DEFAULT_REPOSITORY_DESCRIPTION = 'Automatically released/promoted with gradle-nexus-staging-plugin!'

    private final GradleVersionEnforcer gradleVersionEnforcer

    private Project project
    private NexusStagingExtension extension

    NexusStagingPlugin() {
        this.gradleVersionEnforcer = GradleVersionEnforcer.defaultEnforcer(GradleVersion.version(MINIMAL_SUPPORTED_GRADLE_VERSION))
    }

    @Override
    void apply(Project project) {
        this.project = project
        this.extension = createAndConfigureExtension(project)
        gradleVersionEnforcer.failBuildWithMeaningfulErrorIfAppliedOnTooOldGradleVersion(project)
        failBuildWithMeaningfulErrorIfAppliedNotOnRootProject(project)
        createAndConfigureGetStagingProfileTask(project)
        createAndConfigureCreateRepositoryTask(project)
        def closeRepositoryTask = createAndConfigureCloseRepositoryTask(project)
        def releaseRepositoryTask = createAndConfigureReleaseRepositoryTask(project)
        releaseRepositoryTask.mustRunAfter(closeRepositoryTask)
        def closeAndReleaseRepositoryTask = createAndConfigureCloseAndReleaseRepositoryTask(project)
        closeAndReleaseRepositoryTask.dependsOn(closeRepositoryTask, releaseRepositoryTask)
        tryToDetermineCredentials(project, extension)
        //just during the transition period - see https://github.com/Codearte/gradle-nexus-staging-plugin/issues/50
        new LegacyTasksCreator().createAndConfigureLegacyTasks(project)
    }

    private void failBuildWithMeaningfulErrorIfAppliedNotOnRootProject(Project project) {
        if (project != project.rootProject) {
            throw new GradleException("Nexus staging plugin should ONLY be applied on the ROOT project in a build. " +
                "See https://github.com/Codearte/gradle-nexus-staging-plugin/issues/47 for explanation. Feel free to comment there if you really" +
                "need to have it applied on subproject.")
        }
    }

    private NexusStagingExtension createAndConfigureExtension(Project project) {
        NexusStagingExtension extension = project.extensions.create("nexusStaging", NexusStagingExtension, project)
        extension.with {
            serverUrl = DEFAULT_BASE_NEXUS_SERVER_URL
            numberOfRetries = OperationRetrier.DEFAULT_NUMBER_OF_RETRIES
            delayBetweenRetriesInMillis = OperationRetrier.DEFAULT_DELAY_BETWEEN_RETRIES_IN_MILLIS
            repositoryDescription = DEFAULT_REPOSITORY_DESCRIPTION
        }
        return extension
    }

    private void createAndConfigureGetStagingProfileTask(Project project) {
        GetStagingProfileTask task = project.tasks.create(GET_STAGING_PROFILE_TASK_NAME, GetStagingProfileTask, project, extension)
        setTaskDescriptionAndGroup(task, "Gets a staging profile id in Nexus - a diagnostic task")
        setTaskDefaultsAndDescription(task)
    }

    private void createAndConfigureCreateRepositoryTask(Project project) {
        CreateRepositoryTask task = project.tasks.create(CREATE_REPOSITORY_TASK_NAME, CreateRepositoryTask, project, extension)
        setTaskDescriptionAndGroup(task, "Internal task. Should not be used directly. Explicitly creates a staging repository in Nexus")
        setTaskDefaultsAndDescription(task)
    }

    private CloseRepositoryTask createAndConfigureCloseRepositoryTask(Project project) {
        CloseRepositoryTask task = project.tasks.create(CLOSE_REPOSITORY_TASK_NAME, CloseRepositoryTask, project, extension)
        setTaskDescriptionAndGroup(task, "Closes an open artifacts repository in Nexus")
        setTaskDefaultsAndDescription(task)
        return task
    }

    private ReleaseRepositoryTask createAndConfigureReleaseRepositoryTask(Project project) {
        ReleaseRepositoryTask task = project.tasks.create(RELEASE_REPOSITORY_TASK_NAME, ReleaseRepositoryTask, project, extension)
        setTaskDescriptionAndGroup(task, "Releases a closed artifacts repository in Nexus")
        setTaskDefaultsAndDescription(task)
        return task
    }

    private Task createAndConfigureCloseAndReleaseRepositoryTask(Project project) {
        Task task = project.tasks.create(CLOSE_AND_RELEASE_REPOSITORY_TASK_NAME)
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
            repositoryDescription = { extension.repositoryDescription }
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
            return STAGING_TASK_CLASSES.find { Class stagingTaskClass ->
                //GetStagingProfileTask_Decorated is not assignable from GetStagingProfileTask, but its superclass is GetStagingProfileTask...
                return stagingTaskClass.isAssignableFrom(task.getClass().superclass)
            }
        }
    }

    @SuppressWarnings("GroovyUnnecessaryReturn")
    private void tryToGetCredentialsFromUploadArchivesTask(Project project, NexusStagingExtension extension) {
        if (extension.username != null && extension.password != null) {
            return  //username and password already set
        }

        Task uploadTask = project.tasks.findByPath("uploadArchives")
        if (uploadTask instanceof Upload) {

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
