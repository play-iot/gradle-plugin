package cloud.playio.gradle.generator.codegen

import cloud.playio.gradle.generator.GeneratorTask
import cloud.playio.gradle.helper.JavaProject

abstract class VertxCodegenTask : GeneratorTask() {
    init {
        description = "Generate Java code"
    }

    override fun processorArgs(): List<String> {
        val ss = JavaProject.getSourceSet(project, sourceSet.get())
        return ss.java.srcDirs.map { "-Acodegen.output=${it}" }
    }
}

