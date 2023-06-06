package cloud.playio.gradle.qwe.app

import cloud.playio.gradle.OSSProjectPlugin
import cloud.playio.gradle.helper.JavaProject
import cloud.playio.gradle.qwe.QWEDecoratorPlugin
import cloud.playio.gradle.qwe.QWEExtension
import cloud.playio.gradle.qwe.app.task.ConfigGeneratorTask
import cloud.playio.gradle.qwe.app.task.LoggingGeneratorTask
import cloud.playio.gradle.qwe.app.task.ManifestGeneratorTask
import cloud.playio.gradle.qwe.systemd.QWESystemdExtension
import cloud.playio.gradle.qwe.systemd.QWESystemdGeneratorTask
import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.JavaPlugin
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

    companion object {

        const val FAT_JAR_CLASSIFIER = "fat"
        const val DIST_ZIP_FAT_TASK_NAME = "distZipFat"
        const val DIST_TAR_FAT_TASK_NAME = "distTarFat"
        const val PLUGIN_ID = "io.github.zero88.gradle.qwe.app"
    }

    override fun applyExternalPlugins(project: Project) {
        project.plugins.apply(OSSProjectPlugin::class.java)
    }

    override fun pluginId(): String {
        return PLUGIN_ID
    }

    override fun configureExtension(project: Project, ossExt: cloud.playio.gradle.OSSExtension, qweExt: QWEExtension): QWEAppExtension {
        val extensionAware = qweExt as ExtensionAware
        extensionAware.extensions.create<QWESystemdExtension>(QWESystemdExtension.NAME)
        val appExt = extensionAware.extensions.create<QWEAppExtension>(QWEAppExtension.NAME)
        appExt.appName.convention(ossExt.baseName)
        return appExt
    }

    override fun registerAndConfigureTask(
        project: Project,
        ossExt: cloud.playio.gradle.OSSExtension,
        qweExt: QWEExtension,
        decoratorExt: QWEAppExtension
    ) {
        configureSourceSet(project, decoratorExt.layout)
        project.tasks {
            val pConfig = register<ConfigGeneratorTask>(ConfigGeneratorTask.NAME) {
                outputDirs.add(decoratorExt.layout.find(GeneratedLayoutExtension.CONF)!!.directory)
            }
            val pManifest = register<ManifestGeneratorTask>(ManifestGeneratorTask.NAME) {
                appName.set(decoratorExt.appName)
                appVerticle.set(decoratorExt.appVerticle)
                appLauncher.set(decoratorExt.appLauncher)
                outputs.upToDateWhen { false }
            }
            val pLogConfig = register<LoggingGeneratorTask>(LoggingGeneratorTask.NAME) {
                appName.set(decoratorExt.appName)
                logExt.set(decoratorExt.logging)
                fatJar.set(decoratorExt.fatJar)
                outputDirs.add(decoratorExt.layout.find(GeneratedLayoutExtension.CONF)!!.directory)
                if (decoratorExt.fatJar.get()) {
                    outputDirs.add(decoratorExt.layout.find(GeneratedLayoutExtension.RESOURCES)!!.directory)
                }
            }
            val pSystemd = register<QWESystemdGeneratorTask>(QWESystemdGeneratorTask.NAME) {
                val systemd = (qweExt as ExtensionAware).extensions.getByType<QWESystemdExtension>()
                onlyIf { systemd.enabled.get() }
                appName.set(decoratorExt.appName)
                projectDes.set(ossExt.description.orElse(ossExt.title))
                configFile.set(decoratorExt.configFile)
                workingDir.set(decoratorExt.workingDir)
                dataDir.set(decoratorExt.dataDir)
                systemdProp.set(systemd)
                outputDirs.add(decoratorExt.layout.find(GeneratedLayoutExtension.SERVICE)!!.directory)
            }
            withType<ProcessResources>().configureEach { dependsOn(pConfig, pManifest, pLogConfig, pSystemd) }
            named<AbstractArchiveTask>("distZip") { bundleArchive(ossExt, decoratorExt) }
            named<AbstractArchiveTask>("distTar") { bundleArchive(ossExt, decoratorExt) }
            project.afterEvaluate {
                if (decoratorExt.fatJar.get()) {
                    project.plugins.apply(ShadowPlugin::class.java)
                    withType<ShadowJar> {
                        group = "build"
                        archiveBaseName.set(ossExt.baseName)
                        archiveClassifier.set(FAT_JAR_CLASSIFIER)
                        onlyIf {
                            decoratorExt.fatJar.get()
                        }
                        mustRunAfter(named<Jar>(JavaPlugin.JAR_TASK_NAME))
                        from(decoratorExt.layout.find(GeneratedLayoutExtension.RESOURCES)!!.directory)
                    }
                    register<Zip>(DIST_ZIP_FAT_TASK_NAME) { distFat(ossExt, decoratorExt) }
                    register<Tar>(DIST_TAR_FAT_TASK_NAME) { distFat(ossExt, decoratorExt) }
                    named(LifecycleBasePlugin.ASSEMBLE_TASK_NAME).configure {
                        dependsOn(withType<ShadowJar>(), named(DIST_ZIP_FAT_TASK_NAME), named(DIST_TAR_FAT_TASK_NAME))
                    }
                }
            }
        }
    }

    private fun AbstractArchiveTask.distFat(ossExt: cloud.playio.gradle.OSSExtension, appExt: QWEAppExtension) {
        group = "distribution"
        archiveBaseName.set(ossExt.baseName)
        archiveClassifier.set(FAT_JAR_CLASSIFIER)
        onlyIf {
            appExt.fatJar.get()
        }
        dependsOn(project.tasks.withType<ShadowJar>())
        from(project.tasks.withType<ShadowJar>().map { it.outputs })
    }

    private fun AbstractArchiveTask.bundleArchive(ossExt: cloud.playio.gradle.OSSExtension, appExt: QWEAppExtension) {
        into("${ossExt.baseName.get()}-${project.version}/${GeneratedLayoutExtension.CONF}") {
            from(appExt.layout.find(GeneratedLayoutExtension.CONF)!!.directory)
        }
        into("${ossExt.baseName.get()}-${project.version}/${GeneratedLayoutExtension.SERVICE}") {
            from(appExt.layout.find(GeneratedLayoutExtension.SERVICE)!!.directory)
        }
    }

    private fun configureSourceSet(project: Project, layout: GeneratedLayoutExtension) {
        layout.generatedLayout.get().values.forEach {
            if (it.mode == GeneratedLayoutExtension.LayoutMode.SOURCE) {
                JavaProject.getMainSourceSet(project).java.srcDirs.add(it.directory.get().asFile)
            }
            if (it.mode == GeneratedLayoutExtension.LayoutMode.RESOURCES) {
                JavaProject.getMainSourceSet(project).resources.srcDirs.add(it.directory.get().asFile)
            }
            if (it.mode == GeneratedLayoutExtension.LayoutMode.TEST_SOURCE) {
                JavaProject.getTestSourceSet(project).java.srcDirs.add(it.directory.get().asFile)
            }
            if (it.mode == GeneratedLayoutExtension.LayoutMode.TEST_RESOURCES) {
                JavaProject.getTestSourceSet(project).resources.srcDirs.add(it.directory.get().asFile)
            }
        }
    }

}
