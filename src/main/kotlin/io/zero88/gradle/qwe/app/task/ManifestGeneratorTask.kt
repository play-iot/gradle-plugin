package io.zero88.gradle.qwe.app.task

import io.zero88.gradle.helper.prop
import io.zero88.gradle.qwe.app.QWEAppPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.java.archives.ManifestException
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.named

open class ManifestGeneratorTask : DefaultTask() {
    init {
        group = "QWE Generator"
        description = "Generate Java manifest"
    }

    @TaskAction
    fun generate() {
        val mainClass = prop(project, "mainClass", QWEAppPlugin.MAIN_CLASS)
        val mainVerticle = prop(project, "mainVerticle", "")
        if (mainClass.trim() == "" || mainVerticle.trim() == "") {
            throw ManifestException("Missing Vertx mainClass or mainVerticle")
        }
        val runtime = project.configurations.getByName("runtimeClasspath")
        val classPath = if (runtime.isEmpty) "" else runtime.files.joinToString(" ") { "lib/${it.name}" }

        project.tasks.named<Jar>(JavaPlugin.JAR_TASK_NAME) {
            manifest.attributes.putAll(
                mapOf(
                    "Main-Class" to mainClass,
                    "Main-Verticle" to mainVerticle,
                    "Class-Path" to "$classPath conf/"
                )
            )
        }
    }
}
