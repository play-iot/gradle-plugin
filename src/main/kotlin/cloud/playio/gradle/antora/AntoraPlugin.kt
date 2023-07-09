package cloud.playio.gradle.antora

import cloud.playio.gradle.antora.tasks.AntoraCopyTask
import cloud.playio.gradle.antora.tasks.AntoraDescriptorTask
import cloud.playio.gradle.antora.tasks.AntoraInitTask
import cloud.playio.gradle.antora.tasks.AntoraTask
import cloud.playio.gradle.shared.JavaProject
import cloud.playio.gradle.shared.PluginConstraint
import cloud.playio.gradle.shared.createOptions
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.kotlin.dsl.*
import java.nio.file.Paths

class AntoraPlugin : Plugin<Project>, PluginConstraint {

    companion object {

        const val PLUGIN_ID = "cloud.playio.gradle.antora"
        const val GROUP = "antora"
    }

    override fun apply(project: Project): Unit = project.run {
        project.logger.info("Applying plugin '${PLUGIN_ID}'")
        checkPlugin(PLUGIN_ID)
        val ext = AntoraExtension.create(project)
        val srcAntora = AntoraLayout.create(project.layout.projectDirectory.dir(ext.antoraSrcDir), ext.antoraModule)
        val destAntora = AntoraLayout.create(project.layout.buildDirectory.dir(ext.outDir), ext.antoraModule)
        val config = AntoraConfig(ext, srcAntora, destAntora)
        project.afterEvaluate { evaluateProject(project, ext) }
        project.tasks { registerTasks(project, config) }
    }

    private fun evaluateProject(project: Project, ext: AntoraExtension) {
        if (!ext.javadocInDir.isEmpty && !ext.javadocProjects.orNull.isNullOrEmpty()) {
            throw IllegalArgumentException("Provide only one of javadocInDir or javadocProjects")
        }
        if (!project.findProperty("antoraOutDir")?.toString().isNullOrBlank()) {
            val p = Paths.get(project.findProperty("antoraOutDir").toString())
            ext.outDir.convention(
                if (p.isAbsolute) p.toString() else project.rootDir.toPath().resolve(p).toString()
            )
        }
    }

    @SuppressWarnings("kotlin:S3776")
    private fun TaskContainerScope.registerTasks(project: Project, config: AntoraConfig) {
        register<AntoraInitTask>(AntoraInitTask.NAME) {
            from(config.srcAntora.dir)
            outDir.convention(config.ext.antoraType.flatMap { if (it.isComponent()) config.destAntora.dir else config.destAntora.moduleDir() })
        }
        withType<Javadoc> {
            onlyIf { !config.ext.javadocProjects.orNull.isNullOrEmpty() }
            doFirst {
                val ss: List<SourceSet> = config.ext.javadocProjects.get().map { JavaProject.getMainSourceSet(it) }
                if (config.ext.javadocTitle.isPresent) {
                    title = config.ext.javadocTitle.get()
                }
                source = ss.map { it.java.asFileTree }.reduceOrNull { acc, fileTree -> acc.plus(fileTree) }!!
                classpath = project.files(ss.map { it.compileClasspath })
                setDestinationDir(project.buildDir.resolve("docs").resolve("javadoc"))
                options { createOptions() }
            }
        }
        AntoraDirectory.values().forEach {
            val name = it.name.toLowerCase().capitalize()
            register<AntoraCopyTask>("antora${name}") {
                group = GROUP
                description = "Copy external resources into Antora $name folder"
                outDir.convention(config.destAntora.toDir(config.ext.antoraModule.orNull, it))
                preserve { include("*") }
                val deps = withType<AntoraInitTask>()
                if (it == AntoraDirectory.ATTACHMENTS) {
                    deps.plus(withType<Javadoc>())
                    from(if (config.ext.javadocInDir.isEmpty) named<Javadoc>("javadoc") else config.ext.javadocInDir) {
                        into("javadoc")
                    }
                }
                dependsOn(deps)
            }
        }
        register<AntoraDescriptorTask>(AntoraDescriptorTask.NAME) {
            onlyIf { config.ext.antoraType.map { it.isComponent() }.getOrElse(false) }
            dependsOn(withType<AntoraInitTask>())
            inputFile.convention(config.srcAntora.descriptorFile())
            outputFile.convention(config.destAntora.descriptorFile())
            docVersion.convention(config.ext.docVersion)
            asciiAttributes.convention(config.ext.asciiAttributes)
        }
        register<AntoraTask>(AntoraTask.NAME) {
            dependsOn(
                withType<AntoraInitTask>(),
                withType<AntoraCopyTask>(),
                withType<AntoraDescriptorTask>(),
                project.subprojects.flatMap { it.tasks.withType<AntoraTask>() }
            )
            antoraType.convention(config.ext.antoraType)
            outputDir.convention(config.destAntora.dir)
        }
        named("assemble") {
            dependsOn(AntoraTask.NAME)
        }
    }

}
