package cloud.playio.gradle.antora.tasks

import cloud.playio.gradle.antora.AntoraPlugin
import cloud.playio.gradle.antora.AntoraType
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.withType

@CacheableTask
abstract class AntoraTask : DefaultTask() {

    init {
        group = AntoraPlugin.GROUP
        description = "Finalize Antora document component"
    }

    companion object {

        const val NAME = "antora"
    }

    @get:Input
    abstract val antoraType: Property<AntoraType>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun finalize() {
        if (antoraType.get().isComponent()) {
            project.copy {
                from(project.subprojects.flatMap { it.tasks.withType<AntoraTask>() })
                into(outputDir)
            }
        }
    }
}
