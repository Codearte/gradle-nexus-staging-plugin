package io.codearte.gradle.nexus.logic

import groovy.transform.InheritConstructors
import io.codearte.gradle.nexus.infra.WrongNumberOfStagingProfiles

@InheritConstructors
class StagingProfileFetcher extends BaseOperationExecuter {

    String getStagingProfileIdForPackageGroup(String packageGroup) {
        Map responseAsMap = client.get(nexusUrl + "/service/local/staging/profiles")    //TODO: Constant
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
        return profileIds[0]
    }
}
