package cloud.playio.gradle.generator.docgen

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

open class DocgenExtension(objects: ObjectFactory) {
    companion object {

        const val NAME = DocgenPlugin.GROUP
    }

    val outDir: Property<String> = objects.property<String>().convention(DocgenPlugin.GROUP)
    val ascii: AsciidocExtension = AsciidocExtension(objects)

    fun ascii(configuration: Action<AsciidocExtension>) {
        configuration.execute(ascii)
    }
}
