package io.zero88.gradle.qwe.app

import io.zero88.gradle.qwe.app.task.LoggingExtension
import io.zero88.gradle.qwe.app.task.SystemdServiceExtension
import org.gradle.api.Action
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory

open class QWEAppExtension(objects: ObjectFactory, projectLayout: ProjectLayout) {

    companion object {

        const val NAME = "app"
    }

    /**
     * Generator layout
     */
    val layout = GeneratedLayoutExtension(objects, projectLayout)

    /**
     * Logging generator extension
     */
    val logging = LoggingExtension(objects)

    /**
     * Systemd *nix generator extension
     */
    val systemd = SystemdServiceExtension(objects)

    fun layout(configuration: Action<GeneratedLayoutExtension>) {
        configuration.execute(layout)
    }

    fun logging(configuration: Action<LoggingExtension>) {
        configuration.execute(logging)
    }

    fun systemd(configuration: Action<SystemdServiceExtension>) {
        configuration.execute(systemd)
    }
}