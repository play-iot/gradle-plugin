package io.zero88.gradle.qwe.app

import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
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
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Tar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.*
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.language.jvm.tasks.ProcessResources

/**
 * This plugin adds Generator/Bundle capabilities to QWE Application
 */
@Suppress("UnstableApiUsage")
class QWEAppPlugin : QWEDecoratorPlugin<QWEAppExtension> {

    override fun applyExternalPlugins(project: Project) {
        project.plugins.apply(io.zero88.gradle.OSSProjectPlugin::class.java)
        project.plugins.apply(ShadowPlugin::class.java)
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
        configureSourceSet(project, decoratorExt.layout)
        project.tasks {
            val pConfig = register<ConfigGeneratorTask>(ConfigGeneratorTask.NAME) {
                outputDirs.add(decoratorExt.layout.find(GeneratedLayoutExtension.CONF)!!.directory)
            }
            val pManifest = register<ManifestGeneratorTask>(ManifestGeneratorTask.NAME) {
                outputs.upToDateWhen { false }
                launcher.set(decoratorExt.launcher)
                appVerticle.set(decoratorExt.verticle)
            }
            val pLogConfig = register<LoggingGeneratorTask>(LoggingGeneratorTask.NAME) {
                projectName.set(ossExt.baseName)
                ext.set(decoratorExt.logging)
                fatJar.set(decoratorExt.fatJar)
                outputDirs.add(decoratorExt.layout.find(GeneratedLayoutExtension.CONF)!!.directory)
                if (decoratorExt.fatJar.get()) {
                    outputDirs.add(decoratorExt.layout.find(GeneratedLayoutExtension.RESOURCES)!!.directory)
                }
            }
            val pSystemd = register<QWESystemdGeneratorTask>(QWESystemdGeneratorTask.NAME) {
                val systemd = (qweExt as ExtensionAware).extensions.getByType<QWESystemdExtension>()
                onlyIf { systemd.enabled.get() }
                baseName.set(ossExt.baseName)
                projectDes.set(ossExt.description.convention(ossExt.title))
                configFile.set(decoratorExt.configFile)
                workingDir.set(decoratorExt.workingDir)
                dataDir.set(decoratorExt.dataDir)
                systemdProp.set(systemd)
                outputDirs.add(decoratorExt.layout.find(GeneratedLayoutExtension.SERVICE)!!.directory)
            }
            withType<ProcessResources>().configureEach { dependsOn(pConfig, pManifest, pLogConfig, pSystemd) }
            withType<ShadowJar>().configureEach {
                onlyIf {
                    decoratorExt.fatJar.get()
                }
                mustRunAfter(named<Jar>(JavaPlugin.JAR_TASK_NAME))
                archiveBaseName.set(ossExt.baseName)
                archiveClassifier.set("all")
                from(decoratorExt.layout.find(GeneratedLayoutExtension.RESOURCES)!!.directory)
            }
            register<Zip>("distFatZip") { distFat(ossExt, decoratorExt) }
            register<Tar>("distFatTar") { distFat(ossExt, decoratorExt) }
            named(LifecycleBasePlugin.ASSEMBLE_TASK_NAME).configure {
                if (decoratorExt.fatJar.get()) {
                    dependsOn(withType<ShadowJar>(), "distZipFat", "distTarFat")
                }
            }
            named<AbstractArchiveTask>("distZip") { bundleArchive(ossExt, decoratorExt) }
            named<AbstractArchiveTask>("distTar") { bundleArchive(ossExt, decoratorExt) }
        }
    }

    private fun AbstractArchiveTask.distFat(ossExt: OSSExtension, appExt: QWEAppExtension) {
        group = "distribution"
        onlyIf {
            appExt.fatJar.get()
        }
        dependsOn(project.tasks.withType<ShadowJar>())
        archiveBaseName.set(ossExt.baseName)
        archiveClassifier.set("all")
        from(project.tasks.withType<ShadowJar>().map { it.outputs })
    }

    private fun AbstractArchiveTask.bundleArchive(ossExt: OSSExtension, appExt: QWEAppExtension) {
        onlyIf {
            appExt.verticle.get().trim() != ""
        }
        into("${ossExt.baseName.get()}-${project.version}/conf") {
            from(appExt.layout.find(GeneratedLayoutExtension.CONF)!!.directory)
        }
        into("${ossExt.baseName.get()}-${project.version}/service") {
            from(appExt.layout.find(GeneratedLayoutExtension.SERVICE)!!.directory)
        }
    }

    private fun configureSourceSet(project: Project, layout: GeneratedLayoutExtension) {
        val sourceSets = project.convention.getPlugin<JavaPluginConvention>().sourceSets
        layout.generatedLayout.get().values.forEach {
            if (it.mode == GeneratedLayoutExtension.LayoutMode.SOURCE) {
                sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).java.srcDirs.add(it.directory.get().asFile)
            }
            if (it.mode == GeneratedLayoutExtension.LayoutMode.RESOURCES) {
                sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).resources.srcDirs.add(it.directory.get().asFile)
            }
            if (it.mode == GeneratedLayoutExtension.LayoutMode.TEST_SOURCE) {
                sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME).java.srcDirs.add(it.directory.get().asFile)
            }
            if (it.mode == GeneratedLayoutExtension.LayoutMode.TEST_RESOURCES) {
                sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME).resources.srcDirs.add(it.directory.get().asFile)
            }
        }
    }

}
