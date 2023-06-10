package cloud.playio.gradle.pandoc

import cloud.playio.gradle.shared.Documentation
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

open class PandocExtension(objects: ObjectFactory) : Documentation.DocumentationBranch(objects, PandocPlugin.GROUP) {
    companion object {

        fun create(project: Project): PandocExtension =
            Documentation.get(project).createBranch(PandocExtension::class.java, PandocPlugin.GROUP)
    }

    val inputFile: RegularFileProperty = objects.fileProperty()
    val from: Property<FormatFrom> = objects.property<FormatFrom>()
    val to: Property<FormatTo> = objects.property<FormatTo>()
    val outFile: Property<String> = objects.property<String>().convention(inputFile.map { it.asFile.name })
    val config: PandocConfig = PandocConfig(objects)

    fun config(configuration: Action<PandocConfig>) {
        configuration.execute(this.config)
    }
}
