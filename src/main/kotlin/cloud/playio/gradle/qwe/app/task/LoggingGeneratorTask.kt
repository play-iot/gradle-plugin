package cloud.playio.gradle.qwe.app.task

import cloud.playio.gradle.shared.getPluginResource
import org.gradle.api.file.RelativePath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property

@Suppress("UnstableApiUsage")
open class LoggingGeneratorTask : QWEGeneratorTask("Generates logging configuration") {

    companion object {

        const val NAME = "genLogConfig"
    }

    @Input
    val appName = project.objects.property<String>().convention(project.name)

    @Input
    val fatJar = project.objects.property<Boolean>().convention(false)

    @Nested
    val logExt = project.objects.property<LoggingExtension>().convention(LoggingExtension(project.objects))

    @TaskAction
    override fun generate() {
        val loggers = logExt.get().otherLoggers.get()
            .map { "<logger name=\"${it.key}\" level=\"${it.value}\"/>" }.joinToString("\r\n")
        val resource = getPluginResource(project, "logger")
        doCopy {
            from(resource.first) {
                include(if (resource.second) "logger/*.xml.template" else "*.xml.template")
                eachFile {
                    relativePath = RelativePath(true, *relativePath.segments.drop(1).toTypedArray())
                }
                includeEmptyDirs = false
                rename("((?!console))+(\\.console)?\\.xml\\.template", "$1.xml")
                filter {
                    it.replace("{{project}}", appName.get())
                        .replace("{{root_level}}", logExt.get().rootLogLevel.get())
                        .replace("{{zero_lib_level}}", logExt.get().zeroLibLevel.get())
                        .replace("{{other_logger}}", loggers)
                }
            }
        }
    }

}
