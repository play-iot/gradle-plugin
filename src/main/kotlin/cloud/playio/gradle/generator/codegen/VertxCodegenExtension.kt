package cloud.playio.gradle.generator.codegen

import cloud.playio.gradle.generator.GeneratorTool
import org.gradle.api.DomainObjectSet
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.domainObjectSet
import org.gradle.kotlin.dsl.property

class VertxCodegenExtension(objects: ObjectFactory) : GeneratorTool<SourceSetName> {
    companion object {

        const val LIB = "io.vertx:vertx-codegen"
        const val LIB_VERSION = "4.4.0"
        const val PROCESSOR = "io.vertx.codegen.CodeGenProcessor"
    }

    override val name: Property<String> = objects.property<String>().value("vertx")
    override val lib: Property<String> = objects.property<String>().convention(LIB)
    override val version: Property<String> = objects.property<String>().convention(LIB_VERSION)
    override val processor: Property<String> = objects.property<String>().convention(PROCESSOR)
    override val sources: DomainObjectSet<SourceSetName> = objects.domainObjectSet(SourceSetName::class)

    init {
        name.disallowChanges()
    }
}
