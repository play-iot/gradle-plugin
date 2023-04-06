package io.zero88.gradle.pandoc

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.register
import io.zero88.gradle.pandoc.tasks.PandocTask

class PandocPlugin : Plugin<Project> {

    companion object {

        const val PLUGIN_ID = "io.github.zero88.gradle.pandoc"
    }

    @Override
    override fun apply(project: Project) {
        val extension = project.extensions.create<PandocExtension>(PandocExtension.NAME)

        project.logger.info("Applying plugin '$PLUGIN_ID'")
        project.tasks {
            register<PandocTask>(PandocTask.NAME) {
                image.set(extension.config.image)
                workingDir.set(extension.config.workingDir)
                arguments.set(extension.config.arguments)
                from.set(extension.from)
                to.set(extension.to)
                inputFile.set(extension.inputFile)
                outputFileName.set(extension.outputFileName)
                outputFolder.set(extension.outputFolder)
            }
        }
    }
}
