package io.zero88.gradle.qwe.app

import io.zero88.gradle.OSSExtension
import io.zero88.gradle.qwe.QWEDecoratorPlugin
import io.zero88.gradle.qwe.QWEExtension
import io.zero88.gradle.qwe.app.task.ConfigGeneratorTask
import io.zero88.gradle.qwe.app.task.LoggingGeneratorTask
import io.zero88.gradle.qwe.app.task.ManifestGeneratorTask
import io.zero88.gradle.qwe.app.task.SystemdServiceGeneratorTask
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.kotlin.dsl.*
import org.gradle.language.jvm.tasks.ProcessResources

/**
 * This plugin adds Generator/Bundle capabilities to QWE Application
 */
@Suppress("UnstableApiUsage")
class QWEAppPlugin : QWEDecoratorPlugin<QWEAppExtension> {

    override fun applyExternalPlugins(project: Project) {
        project.plugins.apply(io.zero88.gradle.OSSProjectPlugin::class.java)
    }

    override fun configureExtension(project: Project, ossExt: OSSExtension, qweExt: QWEExtension): QWEAppExtension {
        return (qweExt as ExtensionAware).extensions.create(QWEAppExtension.NAME)
    }

    override fun registerAndConfigureTask(
        project: Project,
        ossExt: OSSExtension,
        qweExt: QWEExtension,
        decoratorExt: QWEAppExtension
    ) {
        val manifestProvider = project.tasks.register<ManifestGeneratorTask>("generateManifest") {
            onlyIf { qweExt.application.get() }
        }
        val configProvider = project.tasks.register<ConfigGeneratorTask>("generateConfig") {
            outputDir.set(decoratorExt.layout.generatedConfigDir)
        }
        val loggingProvider = project.tasks.register<LoggingGeneratorTask>("generateLogging") {
            onlyIf { qweExt.application.get() }
            projectName.set(ossExt.baseName)
            ext.set(decoratorExt.logging)
            outputDir.set(decoratorExt.layout.generatedConfigDir)
        }
        val systemdProvider = project.tasks.register<SystemdServiceGeneratorTask>("generateSystemdService") {
            onlyIf { qweExt.application.get() && decoratorExt.systemd.enabled.get() }
            baseName.set(ossExt.baseName)
            projectDes.set(ossExt.description.convention(ossExt.title))
            systemdProp.set(decoratorExt.systemd)
            outputDir.set(decoratorExt.layout.generatedServiceDir)
        }
        project.tasks {
            withType<ProcessResources>()
                .configureEach { dependsOn(manifestProvider, configProvider, loggingProvider, systemdProvider) }

            named<AbstractArchiveTask>("distZip") {
                bundleArchive(ossExt, qweExt, decoratorExt)
            }
            named<AbstractArchiveTask>("distTar") {
                bundleArchive(ossExt, qweExt, decoratorExt)
            }
        }
        configureSourceSet(project, decoratorExt.layout)
    }

    private fun AbstractArchiveTask.bundleArchive(
        ossExt: OSSExtension,
        qweExt: QWEExtension,
        decoratorExt: QWEAppExtension
    ) {
        onlyIf { qweExt.application.get() }
        into("${ossExt.baseName.get()}-${project.version}/conf") {
            from(decoratorExt.layout.generatedConfigDir.get())
        }
        into("${ossExt.baseName.get()}-${project.version}/service") {
            from(decoratorExt.layout.generatedServiceDir.get())
        }
    }

    private fun configureSourceSet(project: Project, layout: GeneratedLayoutExtension) {
        val sourceSets = project.convention.getPlugin<JavaPluginConvention>().sourceSets
        sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).java.srcDirs.add(layout.generatedJavaSrcDir.get().asFile)
        sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).resources.srcDirs.add(layout.generatedResourceDir.get().asFile)

        sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME).java.srcDirs.add(layout.generatedJavaSrcTestDir.get().asFile)
        sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME).resources.srcDirs.add(layout.generatedResourceTestDir.get().asFile)
    }

    companion object {

        const val MAIN_CLASS = "io.zero88.qwe.QWELauncher"
    }

}
