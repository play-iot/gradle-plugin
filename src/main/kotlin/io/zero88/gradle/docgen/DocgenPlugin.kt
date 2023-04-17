package io.zero88.gradle.docgen

import io.zero88.gradle.docgen.tasks.AsciidocGenTask
import io.zero88.gradle.helper.checkMinGradleVersion
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencyResolveDetails
import org.gradle.kotlin.dsl.TaskContainerScope
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.register

class DocgenPlugin : Plugin<Project> {

    companion object {

        const val PLUGIN_ID = "io.github.zero88.gradle.docgen"
        const val GROUP = "docgen"
        const val GENERATOR = "docGenerator"
    }

    override fun apply(project: Project) {
        project.logger.info("Applying plugin '${PLUGIN_ID}'")
        checkMinGradleVersion(PLUGIN_ID)
        val extension = project.extensions.create<DocgenExtension>(DocgenExtension.NAME)
        val runtimeClasspath = createDocGeneratorRuntimeConfiguration(project)
        project.tasks { registerTasks(extension, runtimeClasspath) }
        enforceLibVersion(project, extension)
    }

    private fun TaskContainerScope.registerTasks(extension: DocgenExtension, runtimeConfig: Configuration) {
        register<AsciidocGenTask>(AsciidocGenTask.NAME) {
            docgenOutDir.set(extension.outDir)
            processor.set(extension.ascii.processor)
            runtimeClasspath.setFrom(runtimeConfig)
            setup()
        }
    }

    /**
     * Adds the configuration that holds the classpath to use for invoking docgen.
     */
    private fun createDocGeneratorRuntimeConfiguration(project: Project): Configuration {
        val generatorRuntime: Configuration = project.configurations.create(GENERATOR)
        generatorRuntime.setDescription("The classpath used to invoke the docgen generator. Add any additional dependencies here.")
        project.dependencies.add(generatorRuntime.name, "${AsciidocExtension.LIB}:${AsciidocExtension.LIB_VERSION}")
        return generatorRuntime
    }

    private fun enforceLibVersion(project: Project, extension: DocgenExtension) {
        project.configurations.configureEach {
            Action<Configuration> {
                resolutionStrategy.eachDependency {
                    Action<DependencyResolveDetails> {
                        if (requested.group + ":" + requested.name == AsciidocExtension.LIB) {
                            useTarget("${AsciidocExtension.LIB}:${extension.ascii.version.get()}")
                        }
                    }
                }
            }
        }
    }
}
