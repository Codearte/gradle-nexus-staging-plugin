# Gradle Nexus Staging plugin
[![Build Status](https://travis-ci.org/Codearte/gradle-nexus-staging-plugin.svg?branch=master)](https://travis-ci.org/Codearte/gradle-nexus-staging-plugin) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.codearte.gradle.nexus/gradle-nexus-staging-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.codearte.gradle.nexus/gradle-nexus-staging-plugin)

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
            classpath "io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.5.3"
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

    ./gradlew closeAndPromoteRepository

to close staging repository and promote/release it and its artifacts. If a synchronization with Maven Central was enabled the artifacts should
automatically appear into Maven Central within several minutes.

## Tasks

The plugin provides three main tasks:

 - `closeRepository` - closes open repository with uploaded artifacts. There should be just one open repository available in the staging profile
(possible old/broken repositories can be dropped with Nexus GUI)
 - `promoteRepository` - promotes/releases closed repository (required to put artifacts to Maven Central)
 - `dropRepository` - drops promoted repository
 - `closeAndPromoteRepository` - closes and promotes/releases repository (an equivalent to `closeRepository promoteRepository`)
 
And one additional:

 - `getStagingProfile` - gets and displays staging profile id for given package group. This is a diagnostic task to get the value and put it
into the configuration closure as `stagingProfileId`.

It has to be mentioned that calling Nexus REST API ends immediately, but the closing operation takes a moment, so to make it possible to call
`closeRepository promoteRepository` together (or `closeAndPromoteRepository`) there is a built-in retry mechanism.

## Configuration

The plugin defines the following configuration properties in the `nexusStaging` closure:

 - `serverUrl` (optional) - stable release repository - by default Sonatype OSSRH - `https://oss.sonatype.org/service/local/`
 - `username` (optional) - username to the server
 - `password` (optional) - password
 - `packageGroup` (optional) - package group as registered in Nexus staging profile - by default set to a project group (has to be overridden
if packageGroup in Nexus was requested for a few packages in the same domain)
 - `stagingProfileId` (optional) - staging profile used to release given project - can be get with `getStagingProfile` task - when not set
one additional request is set to Nexus server to determine the value using `packageGroup`
 - `numberOfRetries` (optional) - number of retries when waiting for a repository to change a state - by default `7`
 - `delayBetweenRetriesInMillis` (optional) - delay between retries - by default `1000` milliseconds

For sensible configuration example see the plugin's own release configuration in [build.gradle](build.gradle).

## Server credentials

Production Nexus instances usually requires user to authenticate before perform staging operations. In nexus-staging plugin there are a few ways to
provide credentials:
 - manually set username and password in a `nexusStaging` configuration closure (probably reading them from Gradle or system properties)
 - provide authentication section in MavenDeloyer (from Gradle `maven` plugin) - it will be reused by nexus-staging plugin
 - set Gradle properties `nexusUsername` abd `nexusPassword` (via command line or `~/.gradle/gradle.properties`) - properties with these names are
also used by [gradle-nexus-plugin](https://github.com/bmuschko/gradle-nexus-plugin/)

The first matching strategy win. If you need to set empty password use `''` (empty string) instead of null. 

## Additional information 

**The released version is available as a technology preview and it definitely will be evolving breaking backward compatibility. Please take it into
account before using it in production.**

Project [changelog](https://github.com/Codearte/gradle-nexus-staging-plugin/releases)

The plugin is licensed under the terms of [the Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt).

![Stat Counter stats](https://c.statcounter.com/10347937/0/98ac55b0/0/)
