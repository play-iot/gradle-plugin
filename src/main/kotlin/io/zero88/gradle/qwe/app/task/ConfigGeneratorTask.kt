package io.zero88.gradle.qwe.app.task

import org.gradle.api.tasks.TaskAction

open class ConfigGeneratorTask : QWEGeneratorTask("Generates application configuration") {

    companion object {

        const val NAME = "genConfig"
    }

    @TaskAction
    override fun generate() {
        doCopy {
            from("src/main/resources") {
                include("*.json")
            }
        }
    }

}
