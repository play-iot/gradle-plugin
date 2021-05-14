package io.zero88.gradle.qwe.app.task

import io.zero88.gradle.helper.getPluginResource
import io.zero88.gradle.helper.readResourceProperties
import org.gradle.api.file.RelativePath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property

open class SystemdServiceGeneratorTask : QWEGeneratorTask("Generates Systemd Linux service file") {

    @Input
    val baseName = project.objects.property<String>()

    @Input
    val projectDes = project.objects.property<String>()

    @Nested
    val systemdProp = project.objects.property<SystemdServiceExtension>()

    @TaskAction
    override fun generate() {
        val jarFile = baseName.get() + "-" + project.version
        val resource = getPluginResource(project, "service")
        val input = systemdProp.get()
        val configParam = input.configFile.map { "-conf $it" }.getOrElse("")
        val params = input.params.map { it.entries.map { kv -> "-${kv.key} ${kv.value}" }.joinToString { " " } }
            .getOrElse("")
        input.architectures.get().forEach { arch ->
            val props = readResourceProperties("service/java.${arch.code}.properties")
            val jvmProps = if (input.jvmProps.get().isNotEmpty())
                input.jvmProps.map { it.joinToString { " " } }.get() else props?.getProperty("jvm") ?: ""
            val systemProps = if (input.systemProps.get().isNotEmpty())
                input.systemProps.map { it.joinToString(" ", "-D") }.get() else props?.getProperty("system") ?: ""
            project.copy {
                into(outputDir.get())
                from(resource.first) {
                    include("service/systemd.service.template")
                    rename { input.serviceName.getOrElse(baseName.get()).plus(arch.code).plus(".service") }
                    eachFile {
                        relativePath = RelativePath(true, *relativePath.segments.drop(1).toTypedArray())
                    }
                    includeEmptyDirs = false
                    filter {
                        it.replace("{{java_path}}", input.javaPath.get())
                            .replace("{{description}}", projectDes.get())
                            .replace("{{jvm}}", jvmProps)
                            .replace("{{system}}", systemProps)
                            .replace("{{working_dir}}", input.workingDir.get())
                            .replace("{{jar_file}}", jarFile)
                            .replace("{{params}}", configParam + params)
                    }
                }
            }
        }

    }
}
