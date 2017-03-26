package io.codearte.gradle.nexus.logic

trait FetcherResponseTrait {

    Map createResponseMapWithGivenRepos(List<Map> repositories) {
        return [data: repositories]
    }

    //TODO: Rewrite tests to use full sample
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

    Map aRepoInStateAndIdFull(String state, String id, boolean isTransitioning = false) {
        return [
            created: "2017-03-25T15:44:11.248Z",
            createdDate: "Sat Mar 25 15:44:11 UTC 2017",
            createdTimestamp: 1490456651248,
            description: "Implicitly created (auto staging).",
            ipAddress: "127.0.0.2",
            notifications: 1,
            policy: "release",
            profileId: "5027d084a01a3a",
            profileName: "io.gitlab.nexus-at",
            profileType: "repository",
            provider: "maven2",
            releaseRepositoryId: "no-sync-releases",
            releaseRepositoryName: "No-Sync-Releases",
            repositoryId: id,
            repositoryURI: "https://oss.sonatype.org/content/repositories/iogitlabnexus-at-1018",
            transitioning: isTransitioning,
            type: state,
            updated: "2017-03-25T15:48:07.342Z",
            updatedDate: "Sat Mar 25 15:48:07 UTC 2017",
            updatedTimestamp: 1490456887342,
            userAgent: "Aether",
            userId: "nexus-at"
        ]
    }
}
