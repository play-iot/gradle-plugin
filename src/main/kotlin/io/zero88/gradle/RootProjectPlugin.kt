package io.zero88.gradle

import io.zero88.gradle.helper.prop
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestReport
import org.gradle.api.tasks.testing.TestResult
import org.gradle.kotlin.dsl.*
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.testing.base.plugins.TestingBasePlugin
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.sonarqube.gradle.SonarQubeExtension
import org.sonarqube.gradle.SonarQubePlugin
import java.io.File
import java.nio.charset.StandardCharsets

class RootProjectPlugin : Plugin<Project> {

    companion object {

        const val COPY_SUB_PROJECT_ARTIFACTS_TASK_NAME = "copySubProjectsArtifacts"
        const val COPY_SUB_PROJECT_TEST_RESULTS_TASK_NAME = "copySubProjectsTestResults"
        const val ROOT_TEST_REPORT_TASK_NAME = "testRootReport"
        const val ROOT_JACOCO_TASK_NAME = "jacocoRootReport"
    }

    override fun apply(project: Project) {
        if (project != project.rootProject) {
            return
        }
        applyExternalPlugins(project)
        configureExtension(project)
        if (isSingle(project)) {
            project.tasks {
                withType<JacocoReport> {
                    dependsOn(withType<Test>())
                }
                named(SonarQubeExtension.SONARQUBE_TASK_NAME) {
                    dependsOn(withType<JacocoReport>())
                }
            }
        } else {
            project.tasks {
                assembleTask(project)
                verificationTask(project)
                named(SonarQubeExtension.SONARQUBE_TASK_NAME) {
                    dependsOn(ROOT_JACOCO_TASK_NAME)
                }
            }
        }
    }

    private fun isSingle(project: Project): Boolean {
        return project == project.rootProject && project.subprojects.isEmpty()
    }

    private fun configureExtension(project: Project) {
        project.afterEvaluate {
            project.extensions.configure<SonarQubeExtension> {
                properties {
                    property("jacocoHtml", "false")
                    property("sonar.sourceEncoding", StandardCharsets.UTF_8)
                    property("sonar.coverage.jacoco.xmlReportPaths", "${buildDir}/reports/jacoco/coverage.xml")
                }
            }
        }

    }

    private fun applyExternalPlugins(project: Project) {
        project.pluginManager.apply(SonarQubePlugin::class.java)
    }

    private fun TaskContainerScope.assembleTask(project: Project) {
        withType<Jar>().configureEach {
            onlyIf { !isSingle(project) }
        }
        withType<AbstractArchiveTask>().configureEach {
            onlyIf { !isSingle(project) }
        }
        withType<GenerateModuleMetadata>().configureEach {
            onlyIf { !isSingle(project) }
        }
        register<Copy>(COPY_SUB_PROJECT_ARTIFACTS_TASK_NAME) {
            group = "distribution"
            description = "Gathers sub projects artifacts"
            onlyIf { !isSingle(project) }
            dependsOn(project.subprojects.mapNotNull { it.tasks.findByName(LifecycleBasePlugin.ASSEMBLE_TASK_NAME) })
            from(project.subprojects.fold(listOf<File>()) { r, p -> r.plus(p.buildDir.resolve("distributions")) })
            into(project.buildDir.resolve("distributions"))
        }
        named(LifecycleBasePlugin.ASSEMBLE_TASK_NAME) {
            finalizedBy(COPY_SUB_PROJECT_ARTIFACTS_TASK_NAME)
        }
    }

    private fun TaskContainerScope.verificationTask(project: Project) {
        val testFailures = mutableListOf<String>()
        register<Copy>(COPY_SUB_PROJECT_TEST_RESULTS_TASK_NAME) {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            description = "Gathers sub projects test result"
            dependsOn(project.subprojects.mapNotNull { it.tasks.withType<Test>() })
            from(project.subprojects.fold(listOf<File>()) { r, p -> r.plus(p.buildDir.resolve(TestingBasePlugin.TEST_RESULTS_DIR_NAME)) })
            into(project.buildDir.resolve(TestingBasePlugin.TEST_RESULTS_DIR_NAME))
        }
        register<TestReport>(ROOT_TEST_REPORT_TASK_NAME) {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            description = "Aggregates sub projects test result"
            destinationDir = project.buildDir.resolve(TestingBasePlugin.TESTS_DIR_NAME)
            dependsOn(COPY_SUB_PROJECT_TEST_RESULTS_TASK_NAME)
            reportOn(project.subprojects.map { it.tasks.withType<Test>() })
            doLast {
                if (testFailures.isNotEmpty()) {
                    val failures = testFailures.joinToString("\n") { it }
                    val error =
                        "There were failing tests. See the report at: ${
                            destinationDir.toPath().resolve("index.html").toUri()
                        }\n${"-".repeat(50)}\n${failures}"
                    throw TaskExecutionException(project.tasks.withType<Test>().first(), RuntimeException(error))
                }
            }
        }
        named<Test>("test") {
            finalizedBy(ROOT_TEST_REPORT_TASK_NAME)
            ignoreFailures = true
            val handler =
                KotlinClosure2<TestDescriptor, TestResult, Any>(
                    { descriptor, result ->
                        if (descriptor.parent != null && result.resultType == TestResult.ResultType.FAILURE) {
                            testFailures.add("${descriptor.parent?.name} > ${descriptor.name} ${result.resultType}\n\t${result.exception}")
                        }
                    })
            project.subprojects.map {
                it.tasks.withType<Test> {
                    ignoreFailures = true
                    afterTest(handler)
                }
            }
        }
        register<JacocoReport>(ROOT_JACOCO_TASK_NAME) {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            description = "Aggregates Jacoco test result"
            dependsOn(project.subprojects.map { it.tasks.withType<Test>() },
                      project.subprojects.map { it.tasks.withType<JacocoReport>() })
            additionalSourceDirs.setFrom(project.subprojects.map {
                it.convention.getPlugin<JavaPluginConvention>().sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).allSource.srcDirs
            })
            sourceDirectories.setFrom(project.subprojects.map {
                it.convention.getPlugin<JavaPluginConvention>().sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).allSource.srcDirs
            })
            classDirectories.setFrom(project.subprojects.map {
                it.convention.getPlugin<JavaPluginConvention>().sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).output
            })
            executionData.setFrom(project.fileTree(".") {
                include("**/build/jacoco/test.exec")
            })
            reports {
                csv.isEnabled = false
                xml.isEnabled = true
                xml.destination = project.layout.buildDirectory.file("reports/jacoco/coverage.xml").get().asFile
                html.isEnabled = prop(project, "jacocoHtml", "true").toBoolean()
                html.destination = project.layout.buildDirectory.file("reports/jacoco/html").get().asFile
            }
        }
    }

}
