package io.zero88.gradle.docgen

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

open class AsciidocExtension(objects: ObjectFactory) {

    companion object {

        const val LIB = "io.vertx:vertx-docgen"
        const val LIB_VERSION = "0.9.4"
        const val PROCESSOR = "io.vertx.docgen.JavaDocGenProcessor"
    }

    val version: Property<String> = objects.property<String>().convention(LIB_VERSION)
    val processor: Property<String> = objects.property<String>().convention(PROCESSOR)
}
