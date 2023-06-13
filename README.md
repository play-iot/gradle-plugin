# QWE Gradle plugin

![build](https://github.com/play-iot/gradle-plugin/workflows/build-release/badge.svg?branch=main)
[![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/play-iot/gradle-plugin?sort=semver)](https://github.com/play-iot/gradle-plugin/releases/latest)
[![Sonatype Nexus (Releases)](https://img.shields.io/nexus/r/cloud.playio/gradle-plugin?server=https%3A%2F%2Fs01.oss.sonatype.org%2F)](https://search.maven.org/artifact/cloud.playio/gradle-plugin)
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/cloud.playio/gradle-plugin?server=https%3A%2F%2Fs01.oss.sonatype.org%2F)

[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=play-iot_gradle-plugin&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=play-iot_gradle-plugin)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=play-iot_gradle-plugin&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=play-iot_gradle-plugin)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=play-iot_gradle-plugin&metric=security_rating)](https://sonarcloud.io/dashboard?id=play-iot_gradle-plugin)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=play-iot_gradle-plugin&metric=duplicated_lines_density)](https://sonarcloud.io/dashboard?id=play-iot_gradle-plugin)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=play-iot_gradle-plugin&metric=coverage)](https://sonarcloud.io/dashboard?id=play-iot_gradle-plugin)

- [x] `OSS` project [![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/cloud/playio/gradle-plugin/maven-metadata.xml.svg?colorB=007ec6&label=oss)](https://plugins.gradle.org/plugin/cloud.playio.gradle.oss)
    - [x] Configure `jar`, `fatJar`, `source`, `javadoc`, `test`, `jacoco` task
    - [x] Publish artifacts to `GitHub Package` and `Sonatype OSS repository` task
- [x] `Root project` plugin [![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/cloud/playio/gradle-plugin/maven-metadata.xml.svg?colorB=007ec6&label=root)](https://plugins.gradle.org/plugin/cloud.playio.gradle.root)
    - [x] Distribute `artifacts` to `root project`
    - [x] Distribute `test report` to `root project`
- [x] `Application` plugin [![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/cloud/playio/gradle-plugin/maven-metadata.xml.svg?colorB=007ec6&label=app)](https://plugins.gradle.org/plugin/cloud.playio.gradle.qwe.app)
    - [x] Generate json config file
    - [x] Generate logging file
    - [x] Generate `*nix systemd service` file
- [x] `Docker` plugin [![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/cloud/playio/gradle-plugin/maven-metadata.xml.svg?colorB=007ec6&label=docker)](https://plugins.gradle.org/plugin/cloud.playio.gradle.qwe.docker)
    - [x] Create `Dockerfile`
    - [x] Build `Docker image`
    - [x] Push `Docker image` to multiple registries
- [x] `Codegen` plugin [![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/cloud/playio/gradle-plugin/maven-metadata.xml.svg?colorB=007ec6&label=docker)](https://plugins.gradle.org/plugin/cloud.playio.gradle.codegen)
    - [x] Generate code based on vertx-codegen
- [x] `Docgen` plugin [![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/cloud/playio/gradle-plugin/maven-metadata.xml.svg?colorB=007ec6&label=docker)](https://plugins.gradle.org/plugin/cloud.playio.gradle.docgen)
  - [x] Generate `asciidoc` from source code, based on vertx-docgen
- [x] `Antora` plugin [![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/cloud/playio/gradle-plugin/maven-metadata.xml.svg?colorB=007ec6&label=docker)](https://plugins.gradle.org/plugin/cloud.playio.gradle.antora)
    - [x] Create `Antora` documentation component
- [x] `Pandoc` plugin [![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/cloud/playio/gradle-plugin/maven-metadata.xml.svg?colorB=007ec6&label=docker)](https://plugins.gradle.org/plugin/cloud.playio.gradle.pandoc)
    - [x] To convert from one markup format to another
