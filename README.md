# Gradle Nexus Staging plugin

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
            classpath 'io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.4.0'
        }
    }

Apply plugin:

    apply plugin: 'io.codearte.nexus-staging'

Configure plugin:

    nexusStaging {
        nexusUrl = "https://oss.sonatype.org/service/local/"
        username = yourNexusUsername
        password = yourPasswordReadFromProperties
        packageGroup = "io.codearte"
    }

After successful upload archives (with `maven`, `maven-publish` or `nexus` plugin) to Sonatype OSSRH call:

    ./gradlew closeRepository

to close staging repository and later:

    ./gradlew promoteRepository

to promote/release the repository and its artifacts. If a synchronisation with Maven Central was enabled the artifacts should automatically appear
into Maven Central within several minutes.

## Additional information 

**The released version is available as a technology preview and it definitely will be evolving breaking backward compatibility. Please take it into
account before using it in production.**

The plugin is licensed under the terms of [the Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt).
