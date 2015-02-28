package io.codearte.gradle.nexus.logic

trait FetcherResponseTrait {

    Map createResponseMapWithGivenRepos(List<Map> repositories) {
        return [data: repositories]
    }

    Map aRepoInStateAndId(String type, String id) {
        return [
                policy               : "release",
                profileId            : "93c08fdebde1ff",
                profileName          : "io.codearte",
                profileType          : "repository",
                releaseRepositoryId  : "releases",
                releaseRepositoryName: "Releases",
                repositoryId         : id,
                type                 : type
        ]
    }
}