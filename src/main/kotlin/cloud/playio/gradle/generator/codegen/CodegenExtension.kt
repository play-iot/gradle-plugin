package cloud.playio.gradle.generator.codegen

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

open class CodegenExtension(objects: ObjectFactory) {
    companion object {

        const val NAME = CodegenPlugin.GROUP
    }

    val outDir: Property<String> = objects.property<String>().convention(CodegenPlugin.GROUP)
    val vertx: VertxCodegenExtension = VertxCodegenExtension(objects)

    fun vertx(configuration: Action<VertxCodegenExtension>) {
        configuration.execute(vertx)
    }
}
