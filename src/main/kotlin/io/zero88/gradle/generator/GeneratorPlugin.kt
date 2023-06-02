package io.zero88.gradle.generator

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

interface GeneratorPlugin {

    /**
     * Adds the configuration that holds the classpath to use for invoking generator.
     */
    fun createGeneratorConfiguration(
        project: Project,
        configName: String,
        tool: GeneratorTool<*>
    ): Configuration {
        val generatorRuntime: Configuration = project.configurations.create(configName)
        generatorRuntime.setDescription("The classpath used to invoke the generator for ${configName}. Add any additional dependencies here.")
        project.dependencies.add(configName, tool.lib.get())
        return generatorRuntime
    }

    fun enforceLibVersion(
        project: Project,
        runtimeClasspath: Configuration,
        tool: GeneratorTool<*>,
        generatorSource: GeneratorSource
    ) {
        project.configurations.configureEach {
            if (name == generatorSource.classpathConfigName) {
                extendsFrom(runtimeClasspath)
            }
            resolutionStrategy.eachDependency {
                if (requested.group + ":" + requested.name == tool.lib.get()) {
                    useTarget("${tool.lib.get()}:${tool.version.get()}")
                }
            }
        }

    }
}
