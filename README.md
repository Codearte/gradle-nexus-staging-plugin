# Gradle Nexus Staging plugin
[![Build Status](https://travis-ci.org/Codearte/gradle-nexus-staging-plugin.svg?branch=master)](https://travis-ci.org/Codearte/gradle-nexus-staging-plugin)
[![Windows Build Status](https://ci.appveyor.com/api/projects/status/github/Codearte/gradle-nexus-staging-plugin?branch=master&svg=true)](https://ci.appveyor.com/project/szpak/gradle-nexus-staging-plugin/)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.codearte.gradle.nexus/gradle-nexus-staging-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.codearte.gradle.nexus/gradle-nexus-staging-plugin)

A gradle plugin providing tasks to close and promote/release staged repositories. It allows to do a full artifacts release to Maven Central through
[Sonatype OSSRH](http://central.sonatype.org/pages/ossrh-guide.html) (OSS Repository Hosting) without the need to use Nexus GUI (to close and promote
artifacts/repository).

## Quick start

Add gradle-nexus-staging-plugin to the `buildscript` dependencies in your build.gradle file for root project:

    buildscript {
        repositories {
            mavenCentral()
            //Needed only for SNAPSHOT versions
            //maven { url "http://oss.sonatype.org/content/repositories/snapshots/" }
        }
        dependencies {
            classpath "io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.8.0"
        }
    }

Apply the plugin:

    apply plugin: 'io.codearte.nexus-staging'

Configure it:

    nexusStaging {
        packageGroup = "org.mycompany.myproject" //optional if packageGroup == project.getGroup()
        stagingProfileId = "yourStagingProfileId" //when not defined will be got from server using "packageGroup"
    }

After successful archives upload (with [`maven`](https://gradle.org/docs/current/userguide/maven_plugin.html), 
[`maven-publish`](https://gradle.org/docs/current/userguide/publishing_maven.html) or 
[`nexus`](https://github.com/bmuschko/gradle-nexus-plugin/) plugin) to Sonatype OSSRH call:

    ./gradlew closeAndReleaseRepository

to close staging repository and promote/release it and its artifacts. If a synchronization with Maven Central was enabled the artifacts should
automatically appear into Maven Central within several minutes.

### New plugin syntax

In addition to Maven Central the plugin is available also from the [Plugin Portal](https://plugins.gradle.org/plugin/io.codearte.nexus-staging) and (in most cases) can be applied in a simplified way:

    plugins {
        id "io.codearte.nexus-staging" version "0.8.0"
    }

Buildscript and `apply plugin` sections can be ommited in that case.

### Multi-project build

The plugin itself does not upload any artifacts. It only closes/promotes a repository with all already uploaded using the `maven` or `maven-publish` plugin artifacts (in the same or previous Gradle execution). Therefore it is enough to apply `io.codearte.nexus-staging` only on the root project in a multi-project build.

## Tasks

The plugin provides three main tasks:

 - `closeRepository` - closes an open repository with the uploaded artifacts. There should be just one open repository available in the staging
 profile (possible old/broken repositories can be dropped with Nexus GUI)
 - `releaseRepository` - releases a closed repository (required to put artifacts to Maven Central aka The Central Repository)
 - `closeAndReleaseRepository` - closes and releases a repository (an equivalent to `closeRepository releaseRepository`)
 
And one additional:

 - `getStagingProfile` - gets and displays a staging profile id for a given package group. This is a diagnostic task to get the value and put it
into the configuration closure as `stagingProfileId`.

It has to be mentioned that calling Nexus REST API ends immediately, but closing/release operations takes a moment. Therefore, to make it possible
to call `closeRepository releaseRepository` together (or use `closeAndReleaseRepository`) there is a built-in retry mechanism.

**Deprecation note**. Starting with version 0.8.0 `promoteRepository` and `closeAndPromoteRepository` are marked as deprecated and will be removed
in the one of the future versions. `releaseRepository` and `closeAndReleaseRepository` can be used as drop-in replacements. The reasons behind that
change can be found in the corresponding [issue](https://github.com/Codearte/gradle-nexus-staging-plugin/issues/50).

## Configuration

The plugin defines the following configuration properties in the `nexusStaging` closure:

 - `serverUrl` (optional) - the stable release repository URL - by default Sonatype OSSRH - `https://oss.sonatype.org/service/local/`
 - `username` (optional) - the username to the server
 - `password` (optional) - the password to the server (an auth token [can be used](https://solidsoft.wordpress.com/2015/09/08/deploy-to-maven-central-using-api-key-aka-auth-token/) instead)
 - `packageGroup` (optional) - the package group as registered in Nexus staging profile - by default set to a project group (has to be overridden
if packageGroup in Nexus was requested for a few packages in the same domain)
 - `stagingProfileId` (optional) - the staging profile used to release given project - can be get with the `getStagingProfile` task - when not set
one additional request is send to the Nexus server to determine the value using `packageGroup`
 - `numberOfRetries` (optional) - the number of retries when waiting for a repository state transition to finish - by default `20`
 - `delayBetweenRetriesInMillis` (optional) - the delay between retries - by default `2000` milliseconds

For the sensible configuration example see the plugin's own release configuration in [build.gradle](build.gradle).

## Server credentials

Production Nexus instances usually require an user to authenticate before perform staging operations. In the nexus-staging plugin there are few
ways to provide credentials:
 - manually set an username and a password in the `nexusStaging` configuration closure (probably reading them from Gradle or system properties)
 - provide the authentication section in `MavenDeloyer` (from the Gradle `maven` plugin) - it will be reused by the nexus-staging plugin
 - set the Gradle properties `nexusUsername` abd `nexusPassword` (via a command line or `~/.gradle/gradle.properties`) - properties with these
names are also used by [gradle-nexus-plugin](https://github.com/bmuschko/gradle-nexus-plugin/).

The first matching strategy win. If you need to set an empty password use `''` (an empty string) instead of null.

## FAQ

### 1. Why do I get `Wrong number of received repositories in state 'open'. Expected 1, received 2`?

There may be a few reasons to get this.

1. Ensure using the [Nexus UI](https://oss.sonatype.org/) that there are no old open staging repositories from the previous executions. If yes, just
drop them suing the UI and try again. This is quite common during the initial experiments with the plugin.

2. It takes some time to close and/or promote a staging repository in Nexus, especially with multiple artifacts. The plugin has a built-in retry
mechanism, however, the default value can be too [low](https://github.com/Codearte/gradle-nexus-staging-plugin/issues/12), especially for
the multiproject build. To confirm that enable logging at the info level in Gradle (using the `--info` or `-i` build parameter). You should see log
messages similar to `Attempt 8/8 failed.`. If yes, increase the timeout using the `numberOfRetries` or `delayBetweenRetriesInMillis` configuration 
parameters. 

3. An another reason to get the aforementioned error is releasing more than one project using the same Nexus staging repository simultaneously
(usually automatically from a Continuous Delivery pipeline from a Continuous Integration server). Unfortunately Gradle does not provide a mechanism
to track/manage staging repository where the artifacts are being uploaded. Therefore, it is hard to distinguish on closing the own/current repository
from the one created by our another project. There is an [idea](https://github.com/Codearte/gradle-nexus-staging-plugin/issues/29) how it could be
handled using the Nexus API. Please comment in that [issue](https://github.com/Codearte/gradle-nexus-staging-plugin/issues/29) if you are in that
situation. 

## Notable users

The plugin is used by [hundreds of projects](https://github.com/search?q=io.codearte.nexus-staging&type=Code&utf8=%E2%9C%93) around the web.

Just to mention a few FOSS projects which leverage the plugin to automatize releasing and Continuous Delivery:
[Frege](https://github.com/Frege/frege-interpreter), 
[Geb](https://github.com/geb/geb), 
[Grails](https://github.com/grails/grails-core), 
[Javers](https://github.com/javers/javers), 
[JSON Assert](https://github.com/marcingrzejszczak/jsonassert), 
[logback-android](https://github.com/tony19/logback-android), 
[mini2Dx](https://github.com/mini2Dx/minibus), 
[Nextflow](https://github.com/nextflow-io/nextflow) and 
[TestNG](https://github.com/cbeust/testng).

The plugin is also used by the tools and the libraries created by various more or less known companies including:
[Allegro](https://github.com/allegro/hermes), 
[Braintree](https://github.com/braintree/braintree_android), 
[Google](https://github.com/google/FreeBuilder), 
[IBM](https://github.com/IBM-UrbanCode/groovy-plugin-utils), 
[PayPal](https://github.com/paypal/PayPal-Java-SDK), 
[Schibsted Spain](https://github.com/scm-spain/karyon-rest-router), 
[TouK](https://github.com/TouK/bubble) and 
[Zalando](https://github.com/zalando-incubator/straw).

## Additional information 

[gradle-nexus-staging-plugin](https://github.com/Codearte/gradle-nexus-staging-plugin) was written by Marcin Zajączkowski
with the help of the [contributors](https://github.com/Codearte/gradle-nexus-staging-plugin/graphs/contributors).
The author can be contacted directly via email: `mszpak ATT wp DOTT pl`.
There is also Marcin's blog available: [Solid Soft](http://blog.solidsoft.info) - working code is not enough.

The PoC leading to the initial version of the plugin was brought to life during one of the hackathons held at [Codearte](http://codearte.io/). 

The first version of the project has been released in 2015 and the plugin seems to be quite stable. Nevertheless, documentation for the Nexus
staging REST API and in addition Gradle support for uploading artifacts to selected Nexus staging repositories leaves much to be desired.
Therefore, the current plugin version is still before 1.0.0.

The project [changelog](https://github.com/Codearte/gradle-nexus-staging-plugin/releases).

The plugin is licensed under the terms of [the Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt).

![Stat Counter stats](https://c.statcounter.com/10347937/0/98ac55b0/0/)
