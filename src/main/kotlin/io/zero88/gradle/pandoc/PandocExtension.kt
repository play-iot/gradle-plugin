package io.zero88.gradle.pandoc

import org.gradle.api.Action
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

open class PandocExtension(objects: ObjectFactory) {
    companion object {

        const val NAME = "pandoc"
    }

    val inputFile: RegularFileProperty = objects.fileProperty()
    val from: Property<FormatFrom> = objects.property<FormatFrom>()
    val to: Property<FormatTo> = objects.property<FormatTo>()
    val outDir: Property<String> = objects.property<String>().convention(PandocPlugin.GROUP)
    val outFile: Property<String> = objects.property<String>().convention(inputFile.map { it.asFile.name })
    val config: PandocConfig = PandocConfig(objects)

    fun config(configuration: Action<PandocConfig>) {
        configuration.execute(this.config)
    }
}
