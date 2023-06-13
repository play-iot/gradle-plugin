package cloud.playio.gradle.qwe

import cloud.playio.gradle.OSSExtension
import cloud.playio.gradle.shared.PluginConstraint
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

interface QWEDecoratorPlugin<T> : Plugin<Project>, PluginConstraint {

    override fun apply(project: Project) {
        project.logger.info("Applying plugin '${pluginId()}'")
        checkGradleVersion(pluginId())
        applyExternalPlugins(project)
        val ossExt = project.extensions.getByType<OSSExtension>()
        val qweExt = QWEExtension.get(project)
        registerAndConfigureTask(project, ossExt, qweExt, configureExtension(project, ossExt, qweExt))
    }

    fun pluginId(): String

    fun applyExternalPlugins(project: Project)

    fun configureExtension(project: Project, ossExt: OSSExtension, qweExt: QWEExtension): T

    fun registerAndConfigureTask(project: Project, ossExt: OSSExtension, qweExt: QWEExtension, decoratorExt: T)
}
