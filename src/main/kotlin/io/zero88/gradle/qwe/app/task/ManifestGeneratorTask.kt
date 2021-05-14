package io.zero88.gradle.qwe.app.task

import org.gradle.api.java.archives.ManifestException
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.property

open class ManifestGeneratorTask : QWEGeneratorTask("Generates manifest") {

    @Input
    val launcher = project.objects.property<String>()

    @Input
    val appVerticle = project.objects.property<String>()

    override fun generate() {
        val mainClass = launcher.get()
        val mainVerticle = appVerticle.get()
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
