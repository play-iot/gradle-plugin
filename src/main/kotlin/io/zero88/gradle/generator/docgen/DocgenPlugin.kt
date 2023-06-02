package io.zero88.gradle.generator.docgen

import io.zero88.gradle.generator.GeneratorPlugin
import io.zero88.gradle.generator.GeneratorSource
import io.zero88.gradle.generator.GeneratorTool
import io.zero88.gradle.helper.PluginConstraint
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.TaskContainerScope
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.register
import java.nio.file.Paths

class DocgenPlugin : Plugin<Project>, GeneratorPlugin, PluginConstraint {

    companion object {

        const val PLUGIN_ID = "io.github.zero88.gradle.docgen"
        const val GROUP = "docgen"
        const val GENERATOR = "docGenerator"
    }

    override fun apply(project: Project) {
        project.logger.info("Applying plugin '$PLUGIN_ID'")
        checkGradleVersion(PLUGIN_ID)
        val extension = project.extensions.create<DocgenExtension>(DocgenExtension.NAME)
        val runtimeClasspath = createGeneratorConfiguration(project, GENERATOR, extension.ascii)
        extension.ascii.sources.configureEach {
            val source = this
            project.tasks { registerTasks(project, runtimeClasspath, extension.ascii, extension.outDir, source) }
            enforceLibVersion(project, runtimeClasspath, extension.ascii, source)
        }
    }

    private fun TaskContainerScope.registerTasks(
        project: Project,
        runtimeConfig: Configuration,
        extension: GeneratorTool<SourceDocName>,
        outDirectory: Property<String>,
        generatorSource: GeneratorSource
    ) {
        register<AsciidocGenTask>(AsciidocGenTask.NAME) {
            group = GROUP
            outDir.set(outDirectory.zip(extension.name) { o, n -> Paths.get(o, n).toString() })
            processor.set(extension.processor)
            runtimeClasspath.setFrom(runtimeConfig)
            sourceSet.set(generatorSource.sourceName)
            setup()
        }
    }

}
