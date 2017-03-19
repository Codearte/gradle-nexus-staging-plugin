package io.codearte.gradle.nexus

//Separate interface as there is problem with constants visibility in traits
interface FunctionalTestConstants {

    public static final String E2E_SERVER_BASE_PATH = "https://oss.sonatype.org/service/local/"
    public static final String E2E_PACKAGE_GROUP = 'io.gitlab.nexus-at'
    public static final String E2E_STAGING_PROFILE_ID = "5027d084a01a3a"
}
