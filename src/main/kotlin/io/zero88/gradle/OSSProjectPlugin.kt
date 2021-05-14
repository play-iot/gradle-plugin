package io.zero88.gradle

import io.github.gradlenexus.publishplugin.NexusPublishPlugin
import io.zero88.gradle.helper.computeBaseName
import io.zero88.gradle.helper.prop
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.distribution.DistributionContainer
import org.gradle.api.distribution.plugins.DistributionPlugin
import org.gradle.api.plugins.JavaLibraryDistributionPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.internal.jvm.Jvm
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.util.GradleVersion
import java.net.URI
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.jar.Attributes

@Suppress("UnstableApiUsage")
class OSSProjectPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        applyExternalPlugins(project)
        val oss = evaluateProject(project)
        project.tasks {
            configExternalTasks(project, oss)
        }
    }

    private fun applyExternalPlugins(project: Project) {
        project.pluginManager.apply(JavaLibraryDistributionPlugin::class.java)
        project.pluginManager.apply(JacocoPlugin::class.java)
        project.pluginManager.apply(MavenPublishPlugin::class.java)
        project.pluginManager.apply(SigningPlugin::class.java)
        project.pluginManager.apply(NexusPublishPlugin::class.java)
    }

    private fun evaluateProject(project: Project): OSSExtension {
        val ossExt = project.extensions.create<OSSExtension>(OSSExtension.NAME)
        ossExt.baseName.convention(computeBaseName(project))
        ossExt.title.convention(prop(project, "title", ossExt.baseName.get()))
        ossExt.description.convention(prop(project, "description"))
        ossExt.publishingInfo.projectName.convention(ossExt.baseName.get())
        project.extra.set("baseName", ossExt.baseName.get())
        project.version = "${project.version}${prop(project, "semanticVersion")}"
        project.afterEvaluate {
            println("- Project Name:     ${ossExt.baseName.get()}")
            println("- Project Title:    ${ossExt.title.get()}")
            println("- Project Group:    ${project.group}")
            println("- Project Version:  ${project.version}")
            println("- Gradle Version:   ${GradleVersion.current()}")
            println("- Java Version:     ${Jvm.current()}")
            println("- Build Hash:       ${prop(project, "buildHash")}")
            println("- Build By:         ${prop(project, "buildBy")}")
            if (ossExt.zero88.get()) {
                ossExt.publishingInfo.developer {
                    id.set(OSSExtension.DEV_ID)
                    email.set(OSSExtension.DEV_EMAIL)
                }
            }
            configureExtension(project, ossExt)
        }
        return ossExt
    }

    private fun configureExtension(project: Project, ossExt: OSSExtension) {
        project.extensions.configure<JavaPluginExtension> {
            withJavadocJar()
            withSourcesJar()
        }
        project.extensions.getByName<DistributionContainer>("distributions")
            .named(DistributionPlugin.MAIN_DISTRIBUTION_NAME)
            .configure { distributionBaseName.set(ossExt.baseName) }
        if (ossExt.publishingInfo.enabled.get()) {
            val publicationName = "maven"
            project.extensions.configure<PublishingExtension> {
                publications {
                    createMavenPublication(publicationName, project, ossExt)
                }
                repositories {
                    maven {
                        url = computeMavenRepositoryUrl(project, ossExt)
                        credentials {
                            username = prop(project, "nexus.username")
                            password = prop(project, "nexus.password")
                        }
                    }
                }
            }
            project.extensions.configure<SigningExtension> {
                useGpgCmd()
                sign(project.extensions.findByType<PublishingExtension>()?.publications?.get(publicationName))
            }
        }
    }

    private fun PublicationContainer.createMavenPublication(
        publicationName: String,
        project: Project,
        ossExt: OSSExtension
    ) {
        create<MavenPublication>(publicationName) {
            groupId = project.group as String?
            artifactId = ossExt.baseName.get()
            version = project.version as String?
            from(project.components["java"])

            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set(ossExt.title)
                description.set(ossExt.description)
                url.set(ossExt.publishingInfo.homepage)
                licenses {
                    license {
                        name.set(ossExt.publishingInfo.license.name)
                        url.set(ossExt.publishingInfo.license.url)
                        comments.set(ossExt.publishingInfo.license.comments)
                        distribution.set(ossExt.publishingInfo.license.distribution)
                    }
                }
                developers {
                    developer {
                        id.set(ossExt.publishingInfo.developer.id)
                        email.set(ossExt.publishingInfo.developer.email)
                        organization.set(ossExt.publishingInfo.developer.organization)
                    }
                }
                scm {
                    connection.set(ossExt.publishingInfo.scm.connection)
                    developerConnection.set(ossExt.publishingInfo.scm.developerConnection)
                    url.set(ossExt.publishingInfo.scm.url)
                    tag.set(ossExt.publishingInfo.scm.tag)
                }
            }
        }
    }

    private fun TaskContainerScope.configExternalTasks(project: Project, ossExt: OSSExtension) {
        withType<JavaCompile>().configureEach {
            options.encoding = StandardCharsets.UTF_8.name()
        }
        named<Jar>(JavaPlugin.JAR_TASK_NAME) {
            manifest {
                attributes(
                    mapOf(
                        Attributes.Name.MANIFEST_VERSION.toString() to "1.0",
                        Attributes.Name.IMPLEMENTATION_TITLE.toString() to ossExt.baseName.get(),
                        Attributes.Name.IMPLEMENTATION_VERSION.toString() to project.version,
                        "Created-By" to GradleVersion.current(),
                        "Build-Jdk" to Jvm.current(),
                        "Build-By" to prop(project, "buildBy"),
                        "Build-Hash" to prop(project, "buildHash"),
                        "Build-Date" to Instant.now()
                    ) + ossExt.manifest.get()
                )
            }
        }
        withType<Jar>().configureEach {
            archiveBaseName.set(ossExt.baseName)
        }
        withType<Sign>().configureEach {
            onlyIf { project.hasProperty("release") }
        }
        withType<Javadoc> {
            title = "${ossExt.title.get()} ${project.version} API"
            options {
                encoding = StandardCharsets.UTF_8.name()
                this as StandardJavadocDocletOptions
                this.addBooleanOption("Xdoclint:none", true)
                tags = mutableListOf(
                    "apiNote:a:API Note:", "implSpec:a:Implementation Requirements:",
                    "implNote:a:Implementation Note:"
                )
            }
        }
        withType<Test> {
            useJUnitPlatform()
            systemProperty("file.encoding", StandardCharsets.UTF_8.name())
        }
    }

    private fun computeMavenRepositoryUrl(project: Project, oss: OSSExtension): URI {
        val path = if (project.hasProperty("github")) {
            val ghRepoUrl = prop(project, "github.nexus.url")
            val ghOwner = prop(project, "nexus.username")
            "${ghRepoUrl}/${ghOwner}/${oss.publishingInfo.projectName.get()}"
        } else {
            val releasesRepoUrl = prop(project, "ossrh.release.url")
            val snapshotsRepoUrl = prop(project, "ossrh.snapshot.url")
            if (project.hasProperty("release")) releasesRepoUrl else snapshotsRepoUrl
        }
        return path?.let { URI(it) }!!
    }
}
