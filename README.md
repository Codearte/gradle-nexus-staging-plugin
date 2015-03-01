# Gradle Nexus Staging plugin [![Build Status](https://travis-ci.org/Codearte/gradle-nexus-staging-plugin.svg?branch=master)](https://travis-ci.org/Codearte/gradle-nexus-staging-plugin) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.codearte.gradle.nexus/gradle-nexus-staging-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.codearte.gradle.nexus/gradle-nexus-staging-plugin)

A gradle plugin providing tasks to close and promote/release staged repositories. It allows to do a full artifacts release to Maven Central through
[Sonatype OSSRH](http://central.sonatype.org/pages/ossrh-guide.html) (OSS Repository Hosting) without the need to use Nexus GUI (to close and promote
artifacts/repository).

## Quick start

Add gradle-nexus-staging-plugin to the buildscript dependencies in your build.gradle file for root project:

    buildscript {
        repositories {
            mavenCentral()
            //Needed only for SNAPSHOT versions
            //maven { url "http://oss.sonatype.org/content/repositories/snapshots/" }
        }
        dependencies {
            classpath "io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.5.0"
        }
    }

Apply plugin:

    apply plugin: 'io.codearte.nexus-staging'

Configure plugin:

    nexusStaging {
        username = yourNexusUsername
        password = yourPasswordReadFromProperties
        packageGroup = "org.mycompany.myproject"
        stagingProfileId = "yourStagingProfileId" //when not defined will be got from server using "packageGroup"
    }

After successful upload archives (with `maven`, `maven-publish` or `nexus` plugin) to Sonatype OSSRH call:

    ./gradlew closeRepository promoteRepository

to close staging repository to promote/release it and its artifacts. If a synchronisation with Maven Central was enabled the artifacts should
automatically appear into Maven Central within several minutes.

## Tasks

The plugin provides three task:

 - `closeRepository` - closes open repository with uploaded artifacts. There should be just one open repository available in the staging profile
(possible old/broken repositories can be dropped with Nexus GUI)
 - `promoteRepository` - promotes/releases closed repository (required to put artifacts to Maven Central)
 - `getStagingProfileTask` - gets and displays staging profile id for given package group. This is a diagnostic task to get the value and put it
into the configuration closure as `stagingProfileId`. To see the result it is required to call gradle with `--info` switch. 

Calling Nexus REST API ends immediately, but the closing operation takes a moment, so to make it possible to call `closeRepository promoteRepository`
together there is a build-in retry mechanism. 

## Configuration

The plugin defines the following configuration properties in the `nexusStaging` closure:

 - `serverUrl` (optional) - stable release repository - by default Sonatype OSSRH - `https://oss.sonatype.org/service/local/` 
 - `username` - username to the server 
 - `password` - password
 - `packageGroup` - package group as registered in Nexus staging profile
 - `stagingProfileId` (optional) - staging profile used to release given project - can be get with `getStagingProfile` task - when not set
one additional request is set to Nexus server to determine the value using `packageGroup`
 - `numberOfRetries` (optional) - number of retries when waiting for a repository to change a state - by default `5`
 - `delayBetweenRetriesInMillis` (optional) - delay between retries - by default `1000` milliseconds

For sensible configuration example see the plugin's own staging configuration in [build.gradle](build.gradle).

## Additional information 

**The released version is available as a technology preview and it definitely will be evolving breaking backward compatibility. Please take it into
account before using it in production.**

The plugin is licensed under the terms of [the Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt).
