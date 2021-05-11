plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    jacoco
    signing
    kotlin(PluginLibs.jvm) version PluginLibs.Version.jvm
    id(PluginLibs.sonarQube) version PluginLibs.Version.sonarQube
    id(PluginLibs.nexusStaging) version PluginLibs.Version.nexusStaging
    id(PluginLibs.gradlePluginPublish) version PluginLibs.Version.gradlePluginPublish
}

repositories {
    mavenLocal()
    maven { url = uri("https://oss.sonatype.org/content/groups/public/") }
    gradlePluginPortal()
}

gradlePlugin {
    plugins {
        create("OSS project plugin") {
            id = "io.zero88.qwe.gradle.oss"
            displayName = "QWE OSS Project plugin"
            description = "This plugin adds some utilities in project for build/maven distribution"
            implementationClass = "io.zero88.qwe.gradle.QWEOSSProjectPlugin"
        }
        create("OSS Root project plugin") {
            id = "io.zero88.qwe.gradle.root"
            displayName = "QWE Root Project plugin"
            description = "This plugin adds some utilities in root project in a multi-project build"
            implementationClass = "io.zero88.qwe.gradle.QWERootProjectPlugin"
        }
        create("QWE Application plugin") {
            id = "io.zero88.qwe.gradle.app"
            displayName = "QWE Application plugin"
            description = "This plugin adds Generator/Bundle capabilities to QWE Application"
            implementationClass = "io.zero88.qwe.gradle.app.QWEAppPlugin"
        }
        create("QWE Docker plugin") {
            id = "io.zero88.qwe.gradle.docker"
            displayName = "QWE Docker plugin"
            description = "This plugin adds Docker capabilities to build/push Docker image for QWE application"
            implementationClass = "io.zero88.qwe.gradle.docker.QWEDockerPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/play-iot/gradle-plugin/blob/main/plugin/README.md"
    vcsUrl = "https://github.com/play-iot/gradle-plugin.git"
    tags = listOf("qwe-application", "qwe-docker", "java-oss")
}

dependencies {
    api(PluginLibs.Depends.docker)
    api(PluginLibs.Depends.sonarQube)

    testImplementation(TestLibs.junit5Api)
    testImplementation(TestLibs.junit5Engine)
}

group = "io.github.zero88"
version = "$version${prop(project, "semanticVersion")}"

sourceSets {
    main { java.srcDirs("src/main/java", "src/main/kotlin") }
    test { java.srcDirs("src/test/java", "src/test/kotlin") }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    withJavadocJar()
    withSourcesJar()
}

tasks {
    test {
        useJUnitPlatform()
        finalizedBy(jacocoTestReport)
    }
    jacocoTestReport {
        dependsOn(test)
        reports {
            xml.isEnabled = true
            csv.isEnabled = false
            html.destination = file("${buildDir}/jacocoHtml")
        }
    }
    named("sonarqube") {
        group = "analysis"
        dependsOn(assemble, jacocoTestReport)
    }
    withType<Sign>().configureEach {
        onlyIf { project.hasProperty("release") }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group as String?
            artifactId = project.name
            version = project.version as String?
            from(components["java"])

            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set(prop(project, "title"))
                description.set(prop(project, "description"))
                url.set("https://github.com/play-iot/gradle-plugin")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://github.com/play-iot/gradle-plugin/blob/master/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("zero88")
                        email.set("sontt246@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://git@github.com:play-iot/gradle-plugin.git")
                    url.set("https://github.com/play-iot/gradle-plugin")
                }
            }
        }
    }
    repositories {
        maven {
            val path = if (project.hasProperty("github")) {
                "${project.property("github.nexus.url")}/${project.property("nexus.username")}/${rootProject.name}"
            } else {
                val releasesRepoUrl = prop(project, "ossrh.release.url")
                val snapshotsRepoUrl = prop(project, "ossrh.snapshot.url")
                if (project.hasProperty("release")) releasesRepoUrl else snapshotsRepoUrl
            }
            url = uri(path)
            credentials {
                username = project.property("nexus.username") as String?
                password = project.property("nexus.password") as String?
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["maven"])
}

sonarqube {
    properties {
        property("jacocoHtml", "false")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.coverage.jacoco.xmlReportPaths", "${buildDir}/reports/jacoco/coverage.xml")
    }
}

nexusStaging {
    packageGroup = "io.github.zero88"
    username = project.property("nexus.username") as String?
    password = project.property("nexus.password") as String?
}
