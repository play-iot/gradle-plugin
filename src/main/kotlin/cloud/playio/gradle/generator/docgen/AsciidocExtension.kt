package cloud.playio.gradle.generator.docgen

import cloud.playio.gradle.generator.GeneratorTool
import org.gradle.api.DomainObjectSet
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

class AsciidocExtension(objects: ObjectFactory) : GeneratorTool<SourceDocName> {

    companion object {

        const val LIB = "io.vertx:vertx-docgen"
        const val LIB_VERSION = "0.9.4"
        const val PROCESSOR = "io.vertx.docgen.JavaDocGenProcessor"
    }

    override val name: Property<String> = objects.property<String>().value("asciidoc")
    override val lib: Property<String> = objects.property<String>().convention(LIB)
    override val version: Property<String> = objects.property<String>().convention(LIB_VERSION)
    override val processor: Property<String> = objects.property<String>().convention(PROCESSOR)
    override val sources: DomainObjectSet<SourceDocName> =
        objects.domainObjectSet(SourceDocName::class.java).apply { add(SourceDocName.ASCIIDOC) }

    init {
        name.disallowChanges()
    }
}
