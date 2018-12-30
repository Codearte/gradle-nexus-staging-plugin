package io.codearte.gradle.nexus.legacy

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import io.codearte.gradle.nexus.NexusStagingExtension
import org.gradle.api.DefaultTask
import org.gradle.api.Incubating
import org.gradle.api.Project
import org.gradle.api.artifacts.maven.MavenDeployer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Upload

import javax.inject.Inject

/**
 * Task for internal use only with "legacy" UploadArchives mechanism. It's most likely not something that you are looking for.
 */
@CompileStatic
@Incubating
@SuppressWarnings("UnstableApiUsage")
class PointUploadArchivesToExplicitRepositoryTask extends DefaultTask {

    @Input
    @Optional
    @SkipWhenEmpty
    @Incubating
    final Property<String> stagingRepositoryId

    @Input
    @Incubating
    final Property<String> serverUrl

    @Inject
    PointUploadArchivesToExplicitRepositoryTask(Project project, NexusStagingExtension extension) {
        ObjectFactory objectFactory = project.getObjects();
        stagingRepositoryId = objectFactory.property(String)
        stagingRepositoryId.set(extension.stagingRepositoryId)
        serverUrl = objectFactory.property(String)
        serverUrl.set(project.provider({ extension.serverUrl }))
    }

    @TaskAction
    @CompileDynamic
    void updateDeploymentUrlInUploadTasks() {
        assert stagingRepositoryId.isPresent(), "For not provided stagingRepositoryId task should be skipped"

        project.tasks.withType(Upload) { task ->
            task.repositories?.withType(MavenDeployer)?.each { MavenDeployer deployer ->

                logger.debug("Processing MavenDeployer with repository: ${deployer.repository}")
                if (!deployer.repository) {
                    logger.warn("Skipping MavenDeployer (${deployer}) with no repository. It can result in errors in other tasks.")
                    return  //from closure for given MavenDeployer
                }

                logger.info("Processing MavenDeployer repository with repository url: ${deployer.repository.url}")
                if (deployer.repository.url.startsWith(getServerUrl().get())) {
                    deployer.repository.url = "${removeTrailingSlashIfAvailable(getServerUrl().get())}/staging/deployByRepositoryId/${stagingRepositoryId.get()}"
                } else {
                    logger.info("Skipping adjusting Upload task with non matching URL: ${deployer.repository.url}")
                }
            }
        }
    }

    //TODO: Duplication with BaseOperationExecutor, consider extract to some Util class
    @PackageScope
    //Cannot be private due to execution in Closure
    String removeTrailingSlashIfAvailable(String nexusUrl) {
        return nexusUrl.endsWith("/") ? nexusUrl[0..-2] : nexusUrl
    }
}
