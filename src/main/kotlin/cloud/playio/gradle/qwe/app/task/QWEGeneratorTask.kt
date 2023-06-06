package cloud.playio.gradle.qwe.app.task

import org.gradle.api.DefaultTask
import org.gradle.api.file.CopySpec
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.setProperty

@Suppress("UnstableApiUsage")
abstract class QWEGeneratorTask(_description: String) : DefaultTask() {

    @OutputDirectories
    val outputDirs: SetProperty<Provider<Directory>> = project.objects.setProperty<Provider<Directory>>().empty()

    @TaskAction
    abstract fun generate()

    init {
        group = "QWE Generator"
        description = _description
    }

    protected fun doCopy(copySpec: CopySpec.() -> CopySpec) {
        outputDirs.get().forEach {
            project.copy {
                into(it)
                copySpec(this)
            }
        }
    }

}
