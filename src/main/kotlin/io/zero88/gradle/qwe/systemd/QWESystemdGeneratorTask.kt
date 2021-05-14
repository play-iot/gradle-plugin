package io.zero88.gradle.qwe.systemd

import io.zero88.gradle.helper.getPluginResource
import io.zero88.gradle.helper.readResourceProperties
import io.zero88.gradle.qwe.app.task.QWEGeneratorTask
import org.gradle.api.file.RelativePath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property

open class QWESystemdGeneratorTask : QWEGeneratorTask("Generates Systemd Linux service file") {

    @Input
    val baseName = project.objects.property<String>()

    @Input
    val projectDes = project.objects.property<String>()

    @Input
    val configFile = project.objects.property<String>()

    @Input
    val workingDir = project.objects.property<String>()

    @Input
    val dataDir = project.objects.property<String>()

    @Nested
    val systemdProp = project.objects.property<QWESystemdExtension>()

    @TaskAction
    override fun generate() {
        val jarFile = baseName.get() + "-" + project.version
        val resource = getPluginResource(project, "service")
        val systemd = systemdProp.get()
        val configParam = configFile.map { "-conf $it" }.getOrElse("")
        val params = systemd.params.map { it.entries.map { kv -> "-${kv.key} ${kv.value}" }.joinToString { " " } }
            .getOrElse("")
        val serviceName = systemd.serviceName.get().ifBlank { baseName.get() }
        systemd.architectures.get().forEach { arch ->
            val props = readResourceProperties("service/java.${arch.code}.properties")
            val jvmProps = if (systemd.jvmProps.get().isNotEmpty())
                systemd.jvmProps.map { it.joinToString { " " } }.get() else props?.getProperty("jvm") ?: ""
            val systemProps = if (systemd.systemProps.get().isNotEmpty())
                systemd.systemProps.map { it.joinToString(" ", "-D") }.get() else props?.getProperty("system") ?: ""
            project.copy {
                into(outputDir.get())
                from(resource.first) {
                    include("service/systemd.service.template")
                    rename { serviceName.plus("-").plus(arch.code).plus(".service") }
                    eachFile {
                        relativePath = RelativePath(true, *relativePath.segments.drop(1).toTypedArray())
                    }
                    includeEmptyDirs = false
                    filter {
                        it.replace("{{java_path}}", systemd.javaPath.get())
                            .replace("{{description}}", projectDes.get())
                            .replace("{{jvm}}", jvmProps)
                            .replace("{{system}}", systemProps)
                            .replace("{{working_dir}}", workingDir.get())
                            .replace("{{jar_file}}", jarFile)
                            .replace("{{params}}", configParam + params)
                    }
                }
            }
        }

    }
}
