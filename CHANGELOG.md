# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## API
The API consists of all public Kotlin types from `com.atlassian.performance.tools.ssh.api` and its subpackages:

  * [source compatibility]
  * [binary compatibility]
  * [behavioral compatibility] with behavioral contracts expressed via Javadoc

[source compatibility]: http://cr.openjdk.java.net/~darcy/OpenJdkDevGuide/OpenJdkDevelopersGuide.v0.777.html#source_compatibility
[binary compatibility]: http://cr.openjdk.java.net/~darcy/OpenJdkDevGuide/OpenJdkDevelopersGuide.v0.777.html#binary_compatibility
[behavioral compatibility]: http://cr.openjdk.java.net/~darcy/OpenJdkDevGuide/OpenJdkDevelopersGuide.v0.777.html#behavioral_compatibility

## [Unreleased]
[Unreleased]: https://github.com/atlassian/ssh/compare/release-2.3.1...master

### Added
- Add `Ssh.runInBackground`, which yields `SshResult`s unlike the old `SshConnection.startProcess`. Resolve [JPERF-716].
- Tolerate lack of interrupt if `BackgroundProcess` is already finished.

[JPERF-716]: https://ecosystem.atlassian.net/browse/JPERF-716

## [2.3.1] - 2020-04-06
[2.3.1]: https://github.com/atlassian/ssh/compare/release-2.3.0...release-2.3.1

### Fixed
- Random number generator is now reused between SSH sessions [JPERF-617].

[JPERF-617]: https://ecosystem.atlassian.net/browse/JPERF-617

## [2.3.0] - 2019-05-07
[2.3.0]: https://github.com/atlassian/ssh/compare/release-2.2.0...release-2.3.0

### Added
- Expose the SSH host via SSH connection. Unblock [JPERF-478].

[JPERF-478]: https://ecosystem.atlassian.net/browse/JPERF-478

## [2.2.0] - 2019-02-27
[2.2.0]: https://github.com/atlassian/ssh/compare/release-2.1.0...release-2.2.0

### Added
- Support local port forwarding.
- Support remote port forwarding.

## [2.1.0] - 2018-10-26
[2.1.0]: https://github.com/atlassian/ssh/compare/release-2.0.0...release-2.1.0

### Added
- Support password authentication which resolves JPERF-237

[JPERF-237]: https://ecosystem.atlassian.net/browse/JPERF-237

## [2.0.0] - 2018-10-26
[2.0.0]: https://github.com/atlassian/ssh/compare/release-1.2.0...release-2.0.0

### Removed
- Remove Kotlin default args from the API for:
  - `SshConnection.execute`
  - `SshConnection.safeExecute`
  Changing `SshConnection` into an interface broke binary compatibility by moving the synthetic `$default` bridge
  methods from `SshConnection` to `SshConnection.DefaultImpls`, which was caused by the exposed default args.
  As a consequence, break Kotlin source compatibility due to infeasibility of providing individual overloads for
  just `stdout` or just `stderr`.
- Remove default args from the `Ssh` constructor.

### Added
- Enable abstraction of `SshConnection` and all of its public methods, enabling mocking. Resolve [JPERF-218].

[JPERF-218]: https://ecosystem.atlassian.net/browse/JPERF-218

## [1.2.0] - 2018-10-24
[1.2.0]: https://github.com/atlassian/ssh/compare/release-1.1.0...release-1.2.0

### Added
- Support custom ssh ports which resolves [JPERF-233].

[JPERF-233]: https://ecosystem.atlassian.net/browse/JPERF-233

## [1.1.0] - 2018-09-21
[1.1.0]: https://github.com/atlassian/ssh/compare/release-1.0.0...release-1.1.0

### Added
- Support uploading via SSH.

## [1.0.0] - 2018-08-30
[1.0.0]: https://github.com/atlassian/ssh/compare/release-0.1.0...release-1.0.0

### Changed
- Define the public API.

### Added
- License.

## [0.1.0] - 2018-08-02
[0.1.0]: https://github.com/atlassian/ssh/compare/initial-commit...release-0.1.0

### Added
- Migrate SSH from [JPT submodule].
- Add [README.md](README.md).
- Configure Bitbucket Pipelines.

[JPT submodule]: https://stash.atlassian.com/projects/JIRASERVER/repos/jira-performance-tests/browse/ssh?at=cb909508d9c504d7126d68af9c72087f5822ff2b
