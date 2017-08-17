package io.codearte.gradle.nexus.logic

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import io.codearte.gradle.nexus.infra.SimplifiedHttpJsonRestClient

@InheritConstructors
@CompileStatic
abstract class AbstractRepositoryTransitioner extends BaseOperationExecutor implements RepositoryTransition {

    protected final String repositoryDescription

    protected AbstractRepositoryTransitioner(SimplifiedHttpJsonRestClient client, String nexusUrl, String repositoryDescription) {
        super(client, nexusUrl)
        this.repositoryDescription = repositoryDescription
    }

    protected Map<String, Map> prepareStagingPostContentWithGivenRepositoryIdAndStagingId(String repositoryId, String stagingProfileId) {
        return [data: [
                        stagedRepositoryIds: [repositoryId],
                        description: repositoryDescription,
                        autoDropAfterRelease: true
//                        stagingProfileGroup: stagingProfileId,    //Cannot be set as it triggers "promote" command not "release"

                ]] as Map<String, Map>  //coercion required to prevent: Incompatible generic argument types. Cannot assign java.util.LinkedHashMap
                                        // <java.lang.String, java.util.LinkedHashMap> to: java.util.Map <String, Map> in Groovy 2.4.10
    }

    protected String pathForGivenBulkOperation(String operationName) {
        return "${nexusUrl}/staging/bulk/$operationName"
    }
}
