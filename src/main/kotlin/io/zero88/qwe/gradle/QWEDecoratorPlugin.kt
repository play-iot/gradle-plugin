package io.zero88.qwe.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

interface QWEDecoratorPlugin<T> : Plugin<Project> {

    override fun apply(project: Project) {
        applyExternalPlugins(project)
        val qweExtension = project.extensions.getByType<io.zero88.qwe.gradle.QWEExtension>()
        registerAndConfigureTask(project, qweExtension, configureExtension(project, qweExtension))
    }

    fun applyExternalPlugins(project: Project)

    fun configureExtension(project: Project, qweExt: io.zero88.qwe.gradle.QWEExtension): T

    fun registerAndConfigureTask(project: Project, qweExt: io.zero88.qwe.gradle.QWEExtension, decoratorExt: T)
}
