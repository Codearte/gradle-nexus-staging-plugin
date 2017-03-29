package io.codearte.gradle.nexus.logic

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors

@InheritConstructors
@CompileStatic
abstract class AbstractRepositoryTransitioner extends BaseOperationExecutor implements RepositoryTransition {

    protected Map<String, Map> prepareStagingPostContentWithGivenRepositoryIdAndStagingId(String repositoryId, String stagingProfileId) {
        return [data: [
                        stagedRepositoryId: repositoryId,
                        description: 'Automatically released/promoted with gradle-nexus-staging-plugin!',
                        targetRepositoryId: stagingProfileId
                ]] as Map<String, Map>  //coercion required to prevent: Incompatible generic argument types. Cannot assign java.util.LinkedHashMap
                                        // <java.lang.String, java.util.LinkedHashMap> to: java.util.Map <String, Map> in Groovy 2.4.10
    }
}
