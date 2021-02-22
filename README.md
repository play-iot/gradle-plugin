# QWE Gradle plugin

![build](https://github.com/topmo12/qwe-gradle-plugin/workflows/build-release/badge.svg?branch=main)
![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/topmo12/qwe-gradle-plugin?sort=semver)
![Sonatype Nexus (Releases)](https://img.shields.io/nexus/r/io.github.zero88.qwe/qwe-gradle-plugin?server=https%3A%2F%2Foss.sonatype.org%2F)
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/io.github.zero88.qwe/qwe-gradle-plugin?server=https%3A%2F%2Foss.sonatype.org%2F)

[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=topmo12_qwe-gradle-plugin&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=topmo12_qwe-gradle-plugin)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=topmo12_qwe-gradle-plugin&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=topmo12_qwe-gradle-plugin)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=topmo12_qwe-gradle-plugin&metric=security_rating)](https://sonarcloud.io/dashboard?id=topmo12_qwe-gradle-plugin)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=topmo12_qwe-gradle-plugin&metric=duplicated_lines_density)](https://sonarcloud.io/dashboard?id=topmo12_qwe-gradle-plugin)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=topmo12_qwe-gradle-plugin&metric=coverage)](https://sonarcloud.io/dashboard?id=topmo12_qwe-gradle-plugin)

- [x] `OSS` project
    - [x] Configure `jar`, `javadoc`, `test`, `jacoco` task
    - [x] Publish artifacts to `GitHub Package` and `Sonatype OSS repository` task
- [x] `Root project` plugin
    - [x] Distribute `artifacts` to `root project`
    - [x] Distribute `test report` to `root project`
- [x] `Application` plugin
    - [x] Generate json config file
    - [x] Generate logging file
    - [x] Generate `*nix systemd service` file
- [x] `Docker` plugin
    - [x] Create `Dockerfile`
    - [x] Build `Docker image`
    - [x] Push `Docker image` to multiple registries
