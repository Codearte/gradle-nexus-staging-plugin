# gradle-nexus-staging-plugin changelog

## 0.20.0 - Unreleased

 - Reuse explicitly created staging repository ID if provided by external plugin - TODO
 - Raise minimal required Gradle version to 4.8 due to internals modernisation

**Deprecation note**. Support for implicitly created staging repositories is deprecated. It has been always problematic, slow and error prone
to find a proper staging repository and the recent [changes](https://github.com/travis-ci/travis-ci/issues/9555) in Travis just emphasised that.
Thanks to the new [nexus-publish-plugin](https://github.com/marcphilipp/nexus-publish-plugin/) plugin by
[Marc Philipp](https://github.com/marcphilipp) which seamlessly integrates with the `gradle-nexus-staging` plugin it should straightforward to use
explicitly created staging repositories in Nexus. At least in a case of using `maven-publish` (`publish...` tasks). If you still use the old `maven`
plugin (the `uploadArchives` task) please refer to [that issue](https://github.com/marcphilipp/nexus-publish-plugin/issues/8).

The original code has not been removed and *should* still work for the time being (if you don't use Travis), but it is no longer officially
supported (e.g. the E2E test has been switched to the new approach).   

## 0.12.0 - 2018-09-29

 - Java 11 compatibility (basic path verified by e2e tests) - [#73](https://github.com/Codearte/gradle-nexus-staging-plugin/issues/73)
 - Fix crash on non-upload task named 'uploadArchives' - [#67](https://github.com/szpak/CDeliveryBoy/issues/#67)
 - Drop support for Java 7
 - Drop Gradle 2.x support (not supported by used plugins) 
 - Upgrade project dependencies
 - Upgrade Gradle to 4.10.2

## 0.11.0 - 2017-08-18

 - Fail when applied on non-root project - [#47](https://github.com/Codearte/gradle-nexus-staging-plugin/issues/47)
 - Less confusing log output without "info" logging enabled - [#60](https://github.com/Codearte/gradle-nexus-staging-plugin/issues/60) 
 - Upgrade project dependencies
 - Upgrade Gradle to 4.1 (compatibility with Gradle 2.0+ should be still maintained)
 
## 0.10.0 - 2017-08-18

 - Configurable repository description in close/release operation - [#63](https://github.com/Codearte/gradle-nexus-staging-plugin/pull/63) - contribution by [akomakom](https://github.com/akomakom)

## 0.9.0 - 2017-06-05

This release provides no new features or bugfixes. It is focused on acceptance E2E testing and Continuous Delivery
with [CDeliveryBoy](https://travis-ci.org/szpak/CDeliveryBoy). 

 - Acceptance tests at Gradle level run before release against real Nexus - [#40](https://github.com/Codearte/gradle-nexus-staging-plugin/issues/40)
 - Automatic `CHANGELOG.md` synchronization with GitHub releases - [#52](https://github.com/Codearte/gradle-nexus-staging-plugin/issues/52)
 - Switch releasing to Continuous Delivery with CDeliveryBoy - [#54](https://github.com/Codearte/gradle-nexus-staging-plugin/issues/54)

## 0.8.0 - 2017-04-08

 - Auto drop repository after release - [#37](https://github.com/Codearte/gradle-nexus-staging-plugin/issues/37)
 - Rename "promote" operation to "release" - [#50](https://github.com/Codearte/gradle-nexus-staging-plugin/issues/50)
 - Upgrade project dependencies to 2017 - [#43](https://github.com/Codearte/gradle-nexus-staging-plugin/issues/43)
 - Separate functional tests from unit tests - [#48](https://github.com/Codearte/gradle-nexus-staging-plugin/issues/48)
 - Make functional tests work also on Windows - [#39](https://github.com/Codearte/gradle-nexus-staging-plugin/issues/39)

**Deprecation note**. The ~~promoteRepository~~ and ~~closeAndPromoteRepository~~ tasks are marked as deprecated and will be removed
in one of the future versions. `releaseRepository` and `closeAndReleaseRepository` can be used as drop-in replacements. 

## 0.7.0 - 2017-03-27

 - Verify that repository has been really closed - [#21](https://github.com/Codearte/gradle-nexus-staging-plugin/issues/21)
 - Re-enable sharing stagingRepositoryId between close and promote - [#46](https://github.com/Codearte/gradle-nexus-staging-plugin/issues/46)
 - Basic functional tests with different Gradle versions - [#41](https://github.com/Codearte/gradle-nexus-staging-plugin/issues/41)
 - Suggest longer timeout if failed on time related operations even without `--info` enabled - [#34](https://github.com/Codearte/gradle-nexus-staging-plugin/issues/34) 
 - Longer default retry period  - [#12](https://github.com/Codearte/gradle-nexus-staging-plugin/issues/12)

## 0.6.1 - 2017-03-20

 - Reusing `stagingRepositoryId` from close task bypasses retry mechanism and fails - [#44](https://github.com/Codearte/gradle-nexus-staging-plugin/issues/44) - reusing `stagingRepositoryId` is temporary disabled

## 0.6.0 - 2017-03-19

 - Consider state trying to find just one repository in given state - [#36](https://github.com/Codearte/gradle-nexus-staging-plugin/issues/36) - contribution by [strelok1](https://github.com/strelok1)
 - Better error message in case of HTTP request failure - [#5](https://github.com/Codearte/gradle-nexus-staging-plugin/issues/5) - contribution by [deanhiller](https://github.com/deanhiller)
 - Add EditorConfig configuration to better deal with spaces vs tabs - [#33](https://github.com/Codearte/gradle-nexus-staging-plugin/issues/33)

## 0.5.3 - 2015-06-13

 - `packageGroup` should be taken from project.group by default - [#11](https://github.com/Codearte/gradle-nexus-staging-plugin/issues/11)

## 0.5.2 - 2015-06-09

 - Provide single task to close and promote repository - [#9](https://github.com/Codearte/gradle-nexus-staging-plugin/issues/9)
 - `getStagingProfile` task should display output without `--info` switch - [#8](https://github.com/Codearte/gradle-nexus-staging-plugin/issues/8)

## 0.5.1 - 2015-03-08

 - Credentials should be automatically fetched from configured deployer - [#7](https://github.com/Codearte/gradle-nexus-staging-plugin/issues/7)
 - Credentials should be automatically fetched from Gradle properties (when available) - [#6](https://github.com/Codearte/gradle-nexus-staging-plugin/issues/6)

## 0.5.0 - 2015-03-02

 - Wait given time period when repositories are not yet available - [#3](https://github.com/Codearte/gradle-nexus-staging-plugin/issues/3)
 - Use configured stagingProfileId when available - [#2](https://github.com/Codearte/gradle-nexus-staging-plugin/issues/2)
 - nexusUrl by default should use Sonatype OSSRH - [#1](https://github.com/Codearte/gradle-nexus-staging-plugin/issues/1)

## 0.4.0 - 2015-02-27

 - Initial release
