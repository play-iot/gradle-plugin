package io.zero88.gradle.qwe

import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property

@Suppress("UnstableApiUsage")
open class QWEExtension(objects: ObjectFactory) {

    companion object {

        const val NAME = "qwe"
    }

    /**
     * Defines this project is executable or not
     */
    val application = objects.property<Boolean>()

}
