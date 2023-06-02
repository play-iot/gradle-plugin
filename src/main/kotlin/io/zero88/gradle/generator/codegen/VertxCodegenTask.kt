package io.zero88.gradle.generator.codegen

import io.zero88.gradle.generator.GeneratorTask
import io.zero88.gradle.helper.JavaProject

abstract class VertxCodegenTask : GeneratorTask() {
    init {
        description = "Generate Java code"
    }

    override fun processorArgs(): List<String> {
        val ss = JavaProject.getSourceSet(project, sourceSet.get())
        return ss.java.srcDirs.map { "-Acodegen.output=${it}" }
    }
}

