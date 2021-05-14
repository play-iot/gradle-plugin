package io.zero88.gradle.qwe.app

import io.zero88.gradle.qwe.app.task.LoggingExtension
import org.gradle.api.Action
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.property

@Suppress("UnstableApiUsage")
open class QWEAppExtension(objects: ObjectFactory, projectLayout: ProjectLayout) {

    companion object {

        const val NAME = "app"
    }

    /**
     * Launcher class
     */
    @Input
    val launcher = objects.property<String>().convention("io.zero88.qwe.QWELauncher")

    /**
     * Application verticle class
     */
    @Input
    val verticle = objects.property<String>().convention("")

    /**
     * Application config file
     */
    @Input
    val configFile = objects.property<String>().convention("config.json")

    /**
     * Application working dir
     */
    @Input
    val workingDir = objects.property<String>().convention("/app")

    /**
     * Application data dir
     */
    @Input
    val dataDir = objects.property<String>().convention("/data")

    /**
     * Generator layout
     */
    val layout = GeneratedLayoutExtension(objects, projectLayout)

    /**
     * Logging generator extension
     */
    val logging = LoggingExtension(objects)

    fun layout(configuration: Action<GeneratedLayoutExtension>) {
        configuration.execute(layout)
    }

    fun logging(configuration: Action<LoggingExtension>) {
        configuration.execute(logging)
    }
}
