package io.zero88.gradle.pandoc

import io.zero88.gradle.OSSProjectPlugin
import io.zero88.gradle.helper.checkMinGradleVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.register
import io.zero88.gradle.pandoc.tasks.PandocTask

class PandocPlugin : Plugin<Project> {

    companion object {

        const val PLUGIN_ID = "io.github.zero88.gradle.pandoc"
        const val GROUP = "pandoc"
    }

    @Override
    override fun apply(project: Project) {
        project.logger.info("Applying plugin '$PLUGIN_ID'")
        checkMinGradleVersion(OSSProjectPlugin.PLUGIN_ID)
        val extension = project.extensions.create<PandocExtension>(PandocExtension.NAME)
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
