package io.zero88.gradle.pandoc

import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property

class PandocConfig(objects: ObjectFactory) {

    val image = objects.property<String>().convention("pandoc/core:latest")
    val workingDir = objects.property<String>().convention("/data")
    val arguments = objects.property<Array<String>>().convention(emptyArray())
}
