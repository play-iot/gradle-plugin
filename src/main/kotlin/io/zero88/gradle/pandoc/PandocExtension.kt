package io.zero88.gradle.pandoc

import org.gradle.api.Action
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

abstract class PandocExtension(objects: ObjectFactory) {
    companion object {

        const val NAME = "pandoc"
    }

    abstract val from: Property<FormatFrom>
    abstract val to: Property<FormatTo>
    abstract val inputFile: RegularFileProperty
    abstract val outputFileName: Property<String>
    abstract val outputFolder: DirectoryProperty
    val config: PandocConfig = PandocConfig(objects)

    fun config(configuration: Action<PandocConfig>) {
        configuration.execute(this.config)
    }
}
