package cloud.playio.gradle.generator

import cloud.playio.gradle.helper.JavaProject
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.compile.JavaCompile

@Suppress("MemberVisibilityCanBePrivate")
abstract class GeneratorTask : JavaCompile() {

    @get:Input
    abstract val outDir: Property<String>

    @get:Input
    abstract val processor: Property<String>

    @get:Input
    abstract val sourceSet: Property<String>

    @get:InputFiles
    abstract val runtimeClasspath: ConfigurableFileCollection

    fun setup() {
        val ss = JavaProject.getSourceSet(project, sourceSet.get())
        source = ss.java
        destinationDirectory.set(project.layout.buildDirectory.dir(outDir))
        classpath = ss.compileClasspath + runtimeClasspath
        options.annotationProcessorPath = runtimeClasspath
        options.compilerArgs = buildCompilerArgs()
    }

    protected fun buildCompilerArgs(): List<String> =
        listOf("-proc:only", "-processor", processor.get()) + processorArgs()

    protected abstract fun processorArgs(): List<String>
}
