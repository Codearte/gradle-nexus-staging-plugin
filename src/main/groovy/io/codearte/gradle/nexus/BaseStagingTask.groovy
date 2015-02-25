package io.codearte.gradle.nexus

import groovy.transform.PackageScope
import groovyx.net.http.RESTClient
import io.codearte.gradle.nexus.infra.SimplifiedHttpJsonRestClient
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

abstract class BaseStagingTask extends DefaultTask {

    @Input
    String nexusUrl

    @Input
    @Optional
    String username

    @Input
    @Optional
    String password

    @PackageScope
    SimplifiedHttpJsonRestClient createClient() {
        new SimplifiedHttpJsonRestClient(new RESTClient(), getUsername(), getPassword())
    }
}
