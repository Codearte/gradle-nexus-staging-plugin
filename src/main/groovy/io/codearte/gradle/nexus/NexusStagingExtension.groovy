package io.codearte.gradle.nexus

import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.gradle.api.Incubating
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

@SuppressWarnings("UnstableApiUsage")
@CompileStatic
@Slf4j
@ToString(includeFields = true, includeNames = true, includePackage = false)
class NexusStagingExtension {

    String serverUrl
    String username
    String password
    String packageGroup
    String stagingProfileId
    @Incubating Integer numberOfRetries
    @Incubating Integer delayBetweenRetriesInMillis
    @Incubating String repositoryDescription    //since 0.10.0

    @Incubating
    final Property<String> stagingRepositoryId  //since 0.20.0

    NexusStagingExtension(Project project) {
        ObjectFactory objectFactory = project.getObjects()
        stagingRepositoryId = objectFactory.property(String)
    }
}
