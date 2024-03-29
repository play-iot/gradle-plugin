package cloud.playio.gradle.pandoc

import cloud.playio.gradle.pandoc.tasks.PandocTask
import cloud.playio.gradle.shared.PluginConstraint
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.register

class PandocPlugin : Plugin<Project>, PluginConstraint {

    companion object {

        const val PLUGIN_ID = "cloud.playio.gradle.pandoc"
        const val GROUP = "pandoc"
    }

    @Override
    override fun apply(project: Project) {
        project.logger.info("Applying plugin '$PLUGIN_ID'")
        checkPlugin(PLUGIN_ID)
        val extension = PandocExtension.create(project)
        project.tasks {
            register<PandocTask>(PandocTask.NAME) {
                group = GROUP
                description = "For converting from one markup format to another"
                image.convention(extension.config.image)
                workingDir.convention(extension.config.workingDir)
                arguments.convention(extension.config.arguments)
                from.convention(extension.from)
                to.convention(extension.to)
                inputFile.convention(extension.inputFile)
                outFile.convention(extension.outFile)
                outputFolder.value(project.layout.buildDirectory.dir(extension.outDir))
            }
        }
    }
}
