plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    jacoco
    signing
    kotlin(PluginLibs.jvm) version PluginLibs.Version.jvm
    id(PluginLibs.sonarQube) version PluginLibs.Version.sonarQube
    id(PluginLibs.nexusPublish) version PluginLibs.Version.nexusPublish
    id(PluginLibs.gradlePluginPublish) version PluginLibs.Version.gradlePluginPublish
}

repositories {
    mavenLocal()
    maven { url = uri("https://oss.sonatype.org/content/groups/public/") }
    gradlePluginPortal()
}

group = "cloud.playio"
version = "$version${prop(project, "semanticVersion")}"

gradlePlugin {
    plugins {
        create("oss") {
            id = "cloud.playio.gradle.oss"
            displayName = "OSS Project plugin"
            description = "This plugin adds some utilities in project for build/maven distribution"
            implementationClass = "cloud.playio.gradle.OSSProjectPlugin"
        }
        create("root") {
            id = "cloud.playio.gradle.root"
            displayName = "Root Project plugin"
            description = "This plugin adds some utilities in root project in a multi-project build"
            implementationClass = "cloud.playio.gradle.RootProjectPlugin"
        }
        create("app") {
            id = "cloud.playio.gradle.qwe.app"
            displayName = "QWE Application plugin"
            description = "This plugin adds Generator/Bundle capabilities to QWE Application"
            implementationClass = "cloud.playio.gradle.qwe.app.QWEAppPlugin"
        }
        create("docker") {
            id = "cloud.playio.gradle.qwe.docker"
            displayName = "QWE Docker plugin"
            description = "This plugin adds Docker capabilities to build/push Docker image for QWE application"
            implementationClass = "cloud.playio.gradle.qwe.docker.QWEDockerPlugin"
        }
        create("antora") {
            id = "cloud.playio.gradle.antora"
            displayName = "Antora plugin"
            description = "This plugin adds Antora capabilities to generate Asciidoc and construct Antora documentation component"
            implementationClass = "cloud.playio.gradle.antora.AntoraPlugin"
        }
        create("pandoc") {
            id = "cloud.playio.gradle.pandoc"
            displayName = "Pandoc plugin"
            description = "This plugin adds Pandoc capabilities to convert from one markup format to another"
            implementationClass = "cloud.playio.gradle.pandoc.PandocPlugin"
        }
        create("docgen") {
            id = "cloud.playio.gradle.docgen"
            displayName = "Docgen plugin"
            description = "This plugin adds document gen capabilities"
            implementationClass = "cloud.playio.gradle.generator.docgen.DocgenPlugin"
        }
        create("codegen") {
            id = "cloud.playio.gradle.codegen"
            displayName = "Codegen plugin"
            description = "This plugin adds code gen capabilities"
            implementationClass = "cloud.playio.gradle.generator.codegen.CodegenPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/play-iot/gradle-plugin"
    vcsUrl = "https://github.com/play-iot/gradle-plugin.git"
    tags = listOf("qwe-application", "qwe-docker", "java-oss", "pandoc", "antora", "codegen", "docgen")

    mavenCoordinates {
        groupId = "cloud.playio"
        artifactId = "gradle-plugin"
        version = "${rootProject.version}"
    }
}

dependencies {
    api(PluginLibs.Depends.jacocoLogger)
    api(PluginLibs.Depends.sonarQube)
    api(PluginLibs.Depends.shadow)
    api(PluginLibs.Depends.testLogger)
    api(PluginLibs.Depends.yaml)
    api(PluginLibs.Depends.testcontainers)
    api(PluginLibs.Depends.docker)

    testImplementation(TestLibs.junit5Api)
    testImplementation(TestLibs.junit5Engine)
}

sourceSets {
    main { java.srcDirs("src/main/java", "src/main/kotlin") }
    test { java.srcDirs("src/test/java", "src/test/kotlin") }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
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
            csv.required.set(false)
            xml.required.set(true)
            html.outputLocation.set(file("${buildDir}/jacocoHtml"))
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
                        url.set("https://github.com/play-iot/gradle-plugin/blob/main/LICENSE")
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

nexusPublishing {
    packageGroup.set("cloud.playio")
    repositories {
        sonatype {
            username.set(project.property("nexus.username") as String?)
            password.set(project.property("nexus.password") as String?)
        }
    }
}
