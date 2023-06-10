package cloud.playio.gradle.qwe

import cloud.playio.gradle.shared.PluginConstraint
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType

interface QWEDecoratorPlugin<T> : Plugin<Project>, PluginConstraint {

    override fun apply(project: Project) {
        project.logger.info("Applying plugin '${pluginId()}'")
        checkGradleVersion(pluginId())
        applyExternalPlugins(project)
        val ossExt = project.extensions.getByType<cloud.playio.gradle.OSSExtension>()
        val qweExt = project.extensions.findByType<QWEExtension>() ?: project.extensions.create(QWEExtension.NAME)
        registerAndConfigureTask(project, ossExt, qweExt, configureExtension(project, ossExt, qweExt))
    }

    fun pluginId(): String

    fun applyExternalPlugins(project: Project)

    fun configureExtension(project: Project, ossExt: cloud.playio.gradle.OSSExtension, qweExt: QWEExtension): T

    fun registerAndConfigureTask(project: Project, ossExt: cloud.playio.gradle.OSSExtension, qweExt: QWEExtension, decoratorExt: T)
}
