package cloud.playio.gradle.generator.codegen

import cloud.playio.gradle.generator.GeneratorPlugin
import cloud.playio.gradle.generator.GeneratorSource
import cloud.playio.gradle.generator.GeneratorTool
import cloud.playio.gradle.shared.PluginConstraint
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.Property
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.*
import java.nio.file.Paths

class CodegenPlugin : Plugin<Project>, GeneratorPlugin, PluginConstraint {

    companion object {

        const val PLUGIN_ID = "cloud.playio.gradle.codegen"
        const val GROUP = "codegen"
        const val GENERATOR = "codeGenerator"
        const val PREFIX_TASK_NAME = "genVertxCode"
    }

    override fun apply(project: Project) {
        project.logger.info("Applying plugin '$PLUGIN_ID'")
        checkPlugin(PLUGIN_ID)
        val extension = project.extensions.create<CodegenExtension>(CodegenExtension.NAME)
        val configurations = SourceSetName.values().associateBy({ it }, {
            createGeneratorConfiguration(project, SourceSetName.createGenerator(it, GENERATOR), extension.vertx)
        })
        extension.vertx.sources.configureEach {
            val source = this
            val runtimeClasspath = configurations.getValue(source)
            project.tasks { registerTasks(project, runtimeClasspath, extension.vertx, extension.outDir, source) }
            enforceLibVersion(project, runtimeClasspath, extension.vertx, source)
        }
    }

    private fun TaskContainerScope.registerTasks(
        project: Project,
        runtimeConfig: Configuration,
        extension: GeneratorTool<SourceSetName>,
        outDirectory: Property<String>,
        source: GeneratorSource
    ) {
        val taskName = SourceSetName.createTaskName(source, PREFIX_TASK_NAME)
        register<VertxCodegenTask>(taskName) {
            group = GROUP
            outDir.set(outDirectory.zip(extension.name) { o, n -> Paths.get(o, n, source.sourceName).toString() })
            processor.set(extension.processor)
            runtimeClasspath.setFrom(runtimeConfig)
            sourceSet.set(source.sourceName)
            setup()
            SourceSetName.getSourceSet(source, project).java.srcDirs(destinationDirectory.get())
        }
        named<JavaCompile>(source.successorTaskName) {
            dependsOn(taskName)
        }
    }

}
