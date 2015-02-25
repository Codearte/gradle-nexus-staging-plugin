package io.codearte.gradle.nexus.logic

import groovy.json.JsonSlurper
import groovyx.net.http.RESTClient
import io.codearte.gradle.nexus.PasswordUtil
import io.codearte.gradle.nexus.infra.SimplifiedHttpJsonRestClient
import spock.lang.Ignore
import spock.lang.Specification

class RepositoryCloserSpec extends Specification {

    private static final String STAGING_PROFILE_ID = "93c08fdebde1ff"
    private static final String MOCK_SERVER_HOST = "https://mock.server/"
    private static final String CLOSE_REPOSITORY_PATH = "service/local/staging/profiles/$STAGING_PROFILE_ID/finish"
    private static final String CLOSE_REPOSITORY_FULL_URL = MOCK_SERVER_HOST + CLOSE_REPOSITORY_PATH

    @Ignore
    def "should close repository e2e"() {
        given:
            def client = new SimplifiedHttpJsonRestClient(new RESTClient(), "codearte", PasswordUtil.tryToReadNexusPassword())
            def closer = new RepositoryCloser(client, "https://oss.sonatype.org/")
        when:
            closer.closeRepositoryWithIdAndStagingProfileId("iocodearte-1011", STAGING_PROFILE_ID)
        then:
            noExceptionThrown()
    }

    def "should get open repository id from server"() {
        given:
            def client = Mock(SimplifiedHttpJsonRestClient)
            def closer = new RepositoryCloser(client, MOCK_SERVER_HOST)
        when:
            closer.closeRepositoryWithIdAndStagingProfileId("iocodearte-1011", STAGING_PROFILE_ID)
        then:
            1 * client.post(CLOSE_REPOSITORY_FULL_URL, _) >> { uri, content ->
                assert content == new JsonSlurper().parse(this.getClass().getResource("closeRepositoryRequest.json"))
            }
    }
}
