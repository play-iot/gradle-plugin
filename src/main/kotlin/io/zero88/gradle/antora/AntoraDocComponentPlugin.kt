package io.zero88.gradle.antora

import io.zero88.gradle.OSSProjectPlugin
import io.zero88.gradle.antora.tasks.AntoraDescriptorTask
import io.zero88.gradle.antora.tasks.AntoraInitTask
import io.zero88.gradle.antora.tasks.AntoraTask
import io.zero88.gradle.helper.JavaProject
import io.zero88.gradle.helper.checkMinGradleVersion
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.kotlin.dsl.*
import java.nio.file.Paths

class AntoraDocComponentPlugin : Plugin<Project> {

    companion object {

        const val PLUGIN_ID = "io.github.zero88.gradle.antora"
    }

    override fun apply(project: Project): Unit = project.run {
        project.logger.info("Applying plugin '${PLUGIN_ID}'")
        checkMinGradleVersion(OSSProjectPlugin.PLUGIN_ID)
        val ext = project.extensions.create<AntoraDocComponentExtension>(AntoraDocComponentExtension.NAME)
        project.afterEvaluate {
            val config = evaluateProject(project, ext)
            project.tasks {
                registerTasks(project, config)
            }
        }
    }

    private fun evaluateProject(project: Project, ext: AntoraDocComponentExtension): AntoraConfig {
        if (!ext.javadocInDir.isEmpty && !ext.javadocProjects.orNull.isNullOrEmpty()) {
            throw IllegalArgumentException("Provide only one of javadocInDir or javadocProjects")
        }
        if (!project.findProperty("antoraOutDir")?.toString().isNullOrBlank()) {
            val p = Paths.get(project.findProperty("antoraOutDir").toString())
            ext.antoraOutDir.set(if (p.isAbsolute) p.toString() else project.rootDir.toPath().resolve(p).toString())
        }
        val srcAntora = AntoraCompLayout.create(
            project.layout.projectDirectory.dir(ext.antoraSrcDir).get(),
            ext.antoraModule.get()
        )
        val destAntora = AntoraCompLayout.create(
            project.layout.buildDirectory.dir(ext.antoraOutDir).get(),
            ext.antoraModule.get()
        )
        return AntoraConfig(ext, srcAntora, destAntora)
    }

    private fun TaskContainerScope.registerTasks(project: Project, config: AntoraConfig) {
        register<AntoraInitTask>(AntoraInitTask.NAME) {
            inputDir.set(config.srcAntora.getDir())
            outputDir.set(config.destAntora.getDir())
            antoraModule.set(config.ext.antoraModule)
            antoraType.set(config.ext.antoraType)
        }
        register<AntoraDescriptorTask>(AntoraDescriptorTask.NAME) {
            dependsOn(AntoraInitTask.NAME)
            onlyIf { config.ext.antoraType.orNull?.isComponent() == true }
            inputFile.set(config.srcAntora.descriptorFile())
            outputFile.set(config.destAntora.descriptorFile())
            docVersion.set(config.ext.docVersion)
            asciiAttributes.set(config.ext.asciiAttributes)
        }
        register<JavaCompile>("asciidoc") {
            group = "documentation"
            description = "Generate asciidoc from source code"
            dependsOn(AntoraDescriptorTask.NAME)
            val ss = JavaProject.getMainSourceSet(project)
            val dest: Directory = config.destAntora.partialsDir()
            val src: Directory = when {
                config.ext.antoraType.get().isComponent() -> config.srcAntora.pagesDir()
                else                                      -> config.srcAntora.getDir()
            }
            source = ss.java
            classpath = ss.compileClasspath
            destinationDirectory.set(src)
            options.annotationProcessorPath = ss.compileClasspath
            options.compilerArgs = listOf(
                "-proc:only",
                "-processor",
                "io.vertx.docgen.JavaDocGenProcessor",
                "-Adocgen.output=${dest}",
                "-Adocgen.source=${src}/*.adoc"
            )
        }
        named<Javadoc>("javadoc") {
            onlyIf { !config.ext.javadocProjects.orNull.isNullOrEmpty() }
            options {
                this as StandardJavadocDocletOptions
                if (JavaVersion.current().majorVersion <= JavaVersion.VERSION_11.majorVersion) {
                    this.addBooleanOption("-no-module-directories", true)
                }
            }
            doFirst {
                val ss: List<SourceSet> = config.ext.javadocProjects.get().map { JavaProject.getMainSourceSet(it) }
                if (config.ext.javadocTitle.isPresent) {
                    title = config.ext.javadocTitle.get()
                }
                source = ss.map { it.java.asFileTree }.reduceOrNull { acc, fileTree -> acc.plus(fileTree) }!!
                classpath = project.files(ss.map { it.compileClasspath })
                setDestinationDir(project.buildDir.resolve("docs").resolve("javadoc"))
            }
        }
        register<AntoraTask>(AntoraTask.NAME) {
            dependsOn("asciidoc", "javadoc", project.subprojects.flatMap { it.tasks.withType<AntoraTask>() })
            antoraType.set(config.ext.antoraType)
            antoraModule.set(config.ext.antoraModule)
            javadocDir.from(if (config.ext.javadocInDir.isEmpty) named<Javadoc>("javadoc") else javadocDir.from(config.ext.javadocInDir))
            outputDir.set(config.destAntora.getDir())
        }
        named("assemble") {
            dependsOn(AntoraTask.NAME)
        }
    }

}
