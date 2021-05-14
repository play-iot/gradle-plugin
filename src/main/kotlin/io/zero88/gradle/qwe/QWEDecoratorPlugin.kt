package io.zero88.gradle.qwe

import io.zero88.gradle.OSSExtension
import io.zero88.gradle.helper.prop
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType

@Suppress("UnstableApiUsage")
interface QWEDecoratorPlugin<T> : Plugin<Project> {

    override fun apply(project: Project) {
        applyExternalPlugins(project)
        val ossExt = project.extensions.getByType<OSSExtension>()
        val qweExt = project.extensions.findByType<QWEExtension>() ?: project.extensions.create(QWEExtension.NAME)
        registerAndConfigureTask(project, ossExt, qweExt, configureExtension(project, ossExt, qweExt))
    }

    fun applyExternalPlugins(project: Project)

    fun configureExtension(project: Project, ossExt: OSSExtension, qweExt: QWEExtension): T

    fun registerAndConfigureTask(project: Project, ossExt: OSSExtension, qweExt: QWEExtension, decoratorExt: T)
}
