package cloud.playio.gradle

import cloud.playio.gradle.shared.*
import com.adarshr.gradle.testlogger.TestLoggerExtension
import com.adarshr.gradle.testlogger.TestLoggerExtensionProperties
import com.adarshr.gradle.testlogger.TestLoggerPlugin
import com.adarshr.gradle.testlogger.theme.ThemeType
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.distribution.DistributionContainer
import org.gradle.api.distribution.plugins.DistributionPlugin
import org.gradle.api.plugins.ExtensionContainer
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
import org.gradle.internal.jvm.Jvm
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.util.GradleVersion
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.jar.Attributes
import kotlin.reflect.full.declaredMemberProperties

class OSSProjectPlugin : Plugin<Project>, PluginConstraint, ProjectConstraint {

    companion object {

        const val PLUGIN_ID = "cloud.playio.gradle.oss"
    }

    override fun apply(project: Project) {
        project.logger.info("Applying plugin '${PLUGIN_ID}'")
        checkPlugin(PLUGIN_ID)
        applyExternalPlugins(project)
        verifyProject(project)
        val ossExt = createOSSExtension(project)
        project.extensions.apply { configureExtension(project, ossExt) }
        project.tasks {
            configExternalTasks(project, ossExt)
            register("publishToGitHub") {
                group = "publishing"
                onlyIf { ossExt.githubConfig.publishToRegistry.get() }
                dependsOn("publishMavenPublicationToGitHubPackagesRepository")
            }
        }
    }

    private fun applyExternalPlugins(project: Project) {
        project.pluginManager.apply(JavaLibraryDistributionPlugin::class.java)
        project.pluginManager.apply(MavenPublishPlugin::class.java)
        project.pluginManager.apply(SigningPlugin::class.java)
        project.pluginManager.apply(JacocoPlugin::class.java)
        project.pluginManager.apply(TestLoggerPlugin::class.java)
    }

    private fun createOSSExtension(project: Project): OSSExtension {
        val ossExt = project.extensions.create<OSSExtension>(OSSExtension.NAME)
        ossExt.baseName.convention(computeBaseName(project))
        ossExt.title.convention(prop(project, "title", ossExt.baseName.get()))
        ossExt.description.convention(prop(project, "description"))
        ossExt.githubConfig.registryUrl.convention(prop(project, GitHubConfig.REGISTRY_URL_KEY))
        ossExt.githubConfig.repo.convention(prop(project, GitHubConfig.GITHUB_REPO_KEY))
        ossExt.publishing.projectName.convention(ossExt.baseName)

        project.extra.set("baseName", ossExt.baseName.get())
        project.version = "${project.version}${prop(project, "semanticVersion")}"
        project.afterEvaluate {
            println("- Project Group:    ${project.group}")
            println("- Project Name:     ${ossExt.baseName.get()}")
            println("- Project Title:    ${ossExt.title.get()}")
            println("- Project Version:  ${project.version}")
            println("- Gradle Version:   ${GradleVersion.current()}")
            println("- Java Version:     ${Jvm.current()}")
            println("- Build Hash:       ${prop(project, "buildHash")}")
            println("- Build By:         ${prop(project, "buildBy")}")
            if (ossExt.zero88.get()) {
                ossExt.publishing.developer(DeveloperInfo.Individual)
            }
            if (ossExt.playio.get()) {
                ossExt.publishing.developer(DeveloperInfo.Organization)
            }
            if (ossExt.github.get()) {
                ossExt.publishing {
                    homepage.convention(ossExt.githubConfig.getProjectUrl())
                    scm {
                        url.convention(ossExt.githubConfig.getProjectUrl())
                        connection.convention(ossExt.githubConfig.getScmUrl())
                    }
                    license {
                        name.convention(prop(project, "projectLicense"))
                        url.convention(ossExt.githubConfig.getProjectUrl().map { "${it}/blob/main/LICENSE" })
                    }
                }
            }
        }
        return ossExt
    }

    private fun ExtensionContainer.configureExtension(project: Project, ossExt: OSSExtension) {
        configure<JavaPluginExtension> {
            withJavadocJar()
            withSourcesJar()
        }

        getByName<DistributionContainer>("distributions")
            .named(DistributionPlugin.MAIN_DISTRIBUTION_NAME)
            .configure { distributionBaseName.set(ossExt.baseName) }

        configure<PublishingExtension> {
            project.afterEvaluate {
                publications {
                    if (ossExt.publishing.enabled.get()) {
                        addMavenPublication(project, ossExt)
                        withType<MavenPublication> { addPomMetadata(ossExt) }
                    }
                }
                repositories { addGitHubRegistry(project, ossExt) }
            }
        }

        configure<SigningExtension> {
            useGpgCmd()
            sign(project.extensions.findByType<PublishingExtension>()?.publications)
        }

        configure<TestLoggerExtension> {
            val default = TestLoggerExtension(project)
            default.theme = ThemeType.STANDARD
            TestLoggerExtensionProperties::class.declaredMemberProperties.forEach {
                this.setProperty(it.name, ossExt.testLogger.getProperty(it.name) ?: default.getProperty(it.name))
            }
        }
    }

    private fun PublicationContainer.addMavenPublication(project: Project, ossExt: OSSExtension) {
        create<MavenPublication>(ossExt.publishing.mavenPublicationName.get()) {
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
        }
    }

    private fun RepositoryHandler.addGitHubRegistry(project: Project, ossExt: OSSExtension) {
        if (ossExt.githubConfig.publishToRegistry.get()) {
            maven {
                name = "GitHubPackages"
                url = project.uri(ossExt.githubConfig.getProjectRegistryUrl())
                credentials {
                    username = prop(project, NexusConfig.USER_KEY)
                    password = prop(project, NexusConfig.PASSPHRASE_KEY)
                }
            }
        }
    }

    private fun MavenPublication.addPomMetadata(ext: OSSExtension) {
        pom {
            name.set(ext.title)
            description.set(ext.description)
            url.set(ext.publishing.homepage)
            licenses {
                license {
                    name.set(ext.publishing.license.name)
                    url.set(ext.publishing.license.url)
                    comments.set(ext.publishing.license.comments)
                    distribution.set(ext.publishing.license.distribution)
                }
            }
            developers {
                developer {
                    id.set(ext.publishing.developer.id)
                    name.set(ext.publishing.developer.name)
                    email.set(ext.publishing.developer.email)
                    roles.set(ext.publishing.developer.roles)
                    timezone.set(ext.publishing.developer.timezone)
                    organization.set(ext.publishing.developer.organization)
                    organizationUrl.set(ext.publishing.developer.organizationUrl)
                }
            }
            scm {
                url.set(ext.publishing.scm.url)
                tag.set(ext.publishing.scm.tag)
                connection.set(ext.publishing.scm.connection)
                developerConnection.set(ext.publishing.scm.developerConnection)
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
                        "Build-Date" to Instant.now(),
                        "Build-By" to prop(project, "buildBy"),
                        "Build-Hash" to prop(project, "buildHash")
                    ) + ossExt.manifest.get()
                )
            }
        }
        withType<Jar>().configureEach {
            archiveBaseName.set(ossExt.baseName)
        }
        withType<Sign>().configureEach {
            group = "publishing"
            onlyIf { project.hasProperty("release") }
        }
        withType<Javadoc> {
            title = "${ossExt.title.get()} ${project.version} API"
            options { createOptions() }
        }
        withType<Test> {
            useJUnitPlatform()
            systemProperty("file.encoding", StandardCharsets.UTF_8.name())
        }
    }

}
