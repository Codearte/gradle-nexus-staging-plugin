package io.codearte.gradle.nexus.logic

import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j
import io.codearte.gradle.nexus.infra.WrongNumberOfStagingProfiles

@InheritConstructors
@Slf4j
class StagingProfileFetcher extends BaseOperationExecutor {

    String getStagingProfileIdForPackageGroup(String packageGroup) {
        log.info("Getting staging profile for package group '$packageGroup'")
        Map responseAsMap = client.get(nexusUrl + "/staging/profiles")    //TODO: Constant
        return parseResponseAndGetStagingProfileIdForPackageGroup(responseAsMap, packageGroup)
    }

    private Object parseResponseAndGetStagingProfileIdForPackageGroup(Map responseAsMap, String packageGroup) {
        def profileIds = responseAsMap.data.findAll { profile ->
            profile.name == packageGroup
        }.collect { profile ->
            profile.id
        }
        if (profileIds.isEmpty() || profileIds.size() > 1) {
            throw new WrongNumberOfStagingProfiles(profileIds.size(), packageGroup)
        }
        log.debug("Received 1 staging profile with id: ${profileIds[0]}")
        return profileIds[0]
    }
}
