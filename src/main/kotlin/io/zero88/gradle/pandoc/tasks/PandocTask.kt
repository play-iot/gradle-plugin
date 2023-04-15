package io.zero88.gradle.pandoc.tasks

import io.zero88.gradle.pandoc.FormatFrom
import io.zero88.gradle.pandoc.FormatTo
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.work.Incremental
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy

@CacheableTask
abstract class PandocTask : DefaultTask() {

    companion object {

        const val NAME = "pandoc"
    }

    @get:Input abstract val image: Property<String>
    @get:Input abstract val workingDir: Property<String>
    @get:Input abstract val arguments: Property<Array<String>>
    @get:Input abstract val from: Property<FormatFrom>
    @get:Input abstract val to: Property<FormatTo>

    @get:InputFile @get:Incremental @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val inputFile: RegularFileProperty
    @get:Input abstract val outFile: Property<String>
    @get:OutputDirectory abstract val outputFolder: DirectoryProperty

    @get:Internal
    val fileParams: Provider<Array<String>>
        get() = inputFile.zip(this.outFile) { i, o -> arrayOf(i.asFile.name, "-o", "${workingDir.get()}/${o}") }

    @get:Internal
    val formatParams: Provider<Array<String>>
        get() = from.zip(to) { f, t -> arrayOf("--from=${f}", "--to=${t}") }

    @TaskAction fun convert() {
        val command = arguments.map { it.plus(formatParams.get()) }.map { it.plus(fileParams.get()) }
        logger.info("Pandoc command: \"${command.get().joinToString(" ")}\"")
        PandocImage(image.get())
            .withOutputFile(outputFolder.file(outFile).get().asFile)
            .withCommand(*command.get())
            .withWorkingDirectory(workingDir.get())
            .withStartupCheckStrategy(OneShotStartupCheckStrategy())
            .withFileSystemBind(
                inputFile.get().asFile.absolutePath,
                "${workingDir.get()}/${inputFile.get().asFile.name}",
                BindMode.READ_ONLY
            ).use {
                it.start()
                it.followOutput { s -> logger.info(s.utf8String.trimEnd()) }
            }
    }
}
