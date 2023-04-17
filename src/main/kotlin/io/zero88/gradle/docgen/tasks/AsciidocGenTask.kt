package io.zero88.gradle.docgen.tasks

import io.zero88.gradle.helper.JavaProject
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.compile.JavaCompile

abstract class AsciidocGenTask : JavaCompile() {

    companion object {

        const val NAME = "genAsciidoc"
    }

    init {
        group = "docgen"
        description = "Generate asciidoc from Java code"
    }

    @get:Input
    abstract val docgenOutDir: Property<String>

    @get:Input
    abstract val processor: Property<String>

    @get:InputFiles
    abstract val runtimeClasspath: ConfigurableFileCollection

    override fun getDestinationDirectory(): DirectoryProperty {
        return project.objects.directoryProperty()
            .value(project.layout.buildDirectory.dir(docgenOutDir).map { it.dir("ascii") })
    }

    fun setup() {
        val ss = JavaProject.getMainSourceSet(project)
        source = ss.java
        classpath = ss.compileClasspath + runtimeClasspath
        options.annotationProcessorPath = runtimeClasspath
        options.compilerArgs = listOf(
            "-proc:only",
            "-processor",
            processor.get(),
            "-Adocgen.output=${destinationDirectory.get()}"
        )
    }
}
