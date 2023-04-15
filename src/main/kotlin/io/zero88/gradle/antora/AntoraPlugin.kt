package io.zero88.gradle.antora

import io.zero88.gradle.antora.tasks.*
import io.zero88.gradle.helper.JavaProject
import io.zero88.gradle.helper.checkMinGradleVersion
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.kotlin.dsl.*
import java.nio.file.Paths

class AntoraPlugin : Plugin<Project> {

    companion object {

        const val PLUGIN_ID = "io.github.zero88.gradle.antora"
        const val GROUP = "antora"
    }

    override fun apply(project: Project): Unit = project.run {
        project.logger.info("Applying plugin '${PLUGIN_ID}'")
        checkMinGradleVersion(PLUGIN_ID)
        val ext = project.extensions.create<AntoraExtension>(AntoraExtension.NAME)
        val srcAntora = AntoraLayout.create(project.layout.projectDirectory.dir(ext.antoraSrcDir), ext.antoraModule)
        val destAntora = AntoraLayout.create(project.layout.buildDirectory.dir(ext.antoraOutDir), ext.antoraModule)
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
            ext.antoraOutDir.convention(
                if (p.isAbsolute) p.toString() else project.rootDir.toPath().resolve(p).toString()
            )
        }
    }

    private fun TaskContainerScope.registerTasks(project: Project, config: AntoraConfig) {
        register<AntoraInitTask>(AntoraInitTask.NAME) {
            target.convention(config.ext.antoraType.flatMap { if (it.isComponent()) config.destAntora.dir else config.destAntora.moduleDir() })
            from(config.srcAntora.dir)
        }
        register<AntoraAsciidocTask>(AntoraAsciidocTask.NAME) {
            dependsOn(withType<AntoraInitTask>())
            val dest: Provider<Directory> = config.destAntora.partialsDir()
            val src: Provider<Directory> = when {
                config.ext.antoraType.get().isComponent() -> config.srcAntora.pagesDir()
                else                                      -> config.srcAntora.dir
            }
            val ss = JavaProject.getMainSourceSet(project)
            source = ss.java
            classpath = ss.compileClasspath
            destinationDirectory.set(src)
            options.annotationProcessorPath = ss.compileClasspath
            options.compilerArgs = listOf(
                "-proc:only",
                "-processor",
                "io.vertx.docgen.JavaDocGenProcessor",
                "-Adocgen.output=${dest.get()}",
                "-Adocgen.source=${src.get()}/*.adoc"
            )
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
                options {
                    this as StandardJavadocDocletOptions
                    if (JavaVersion.current().majorVersion <= JavaVersion.VERSION_11.majorVersion) {
                        this.addBooleanOption("-no-module-directories", true)
                    }
                }
            }
        }
        AntoraDirectory.values().forEach {
            val name = it.name.toLowerCase().capitalize()
            register<AntoraCopyTask>("antora${name}") {
                group = GROUP
                description = "Copy external resources into Antora $name folder"
                target.convention(config.destAntora.toDir(config.ext.antoraModule.orNull, it))

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
                withType<AntoraAsciidocTask>(),
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
