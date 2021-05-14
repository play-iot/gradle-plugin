package io.zero88.gradle.qwe.app

import io.zero88.gradle.OSSExtension
import io.zero88.gradle.qwe.QWEDecoratorPlugin
import io.zero88.gradle.qwe.QWEExtension
import io.zero88.gradle.qwe.app.task.ConfigGeneratorTask
import io.zero88.gradle.qwe.app.task.LoggingGeneratorTask
import io.zero88.gradle.qwe.app.task.ManifestGeneratorTask
import io.zero88.gradle.qwe.systemd.QWESystemdExtension
import io.zero88.gradle.qwe.systemd.QWESystemdGeneratorTask
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
        val extensionAware = qweExt as ExtensionAware
        extensionAware.extensions.create<QWESystemdExtension>(QWESystemdExtension.NAME)
        return extensionAware.extensions.create(QWEAppExtension.NAME)
    }

    override fun registerAndConfigureTask(
        project: Project,
        ossExt: OSSExtension,
        qweExt: QWEExtension,
        decoratorExt: QWEAppExtension
    ) {
        val configProvider = project.tasks.register<ConfigGeneratorTask>("generateConfig") {
            outputDir.set(decoratorExt.layout.generatedConfigDir)
        }
        val manifestProvider = project.tasks.register<ManifestGeneratorTask>("generateManifest") {
            outputs.upToDateWhen { false }
            launcher.set(decoratorExt.launcher)
            appVerticle.set(decoratorExt.verticle)
            outputDir.set(temporaryDir)
        }
        val loggingProvider = project.tasks.register<LoggingGeneratorTask>("generateLogging") {
            projectName.set(ossExt.baseName)
            ext.set(decoratorExt.logging)
            outputDir.set(decoratorExt.layout.generatedConfigDir)
        }
        val systemdProvider = project.tasks.register<QWESystemdGeneratorTask>("generateSystemdService") {
            val systemd = (qweExt as ExtensionAware).extensions.getByType<QWESystemdExtension>()
            onlyIf { systemd.enabled.get() }
            baseName.set(ossExt.baseName)
            projectDes.set(ossExt.description.convention(ossExt.title))
            configFile.set(decoratorExt.configFile)
            workingDir.set(decoratorExt.workingDir)
            dataDir.set(decoratorExt.dataDir)
            systemdProp.set(systemd)
            outputDir.set(decoratorExt.layout.generatedServiceDir)
        }
        project.tasks {
            withType<ProcessResources>()
                .configureEach { dependsOn(configProvider, manifestProvider, loggingProvider, systemdProvider) }

            named<AbstractArchiveTask>("distZip") {
                bundleArchive(ossExt, decoratorExt)
            }
            named<AbstractArchiveTask>("distTar") {
                bundleArchive(ossExt, decoratorExt)
            }
        }
        configureSourceSet(project, decoratorExt.layout)
    }

    private fun AbstractArchiveTask.bundleArchive(
        ossExt: OSSExtension,
        decoratorExt: QWEAppExtension
    ) {
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

}
