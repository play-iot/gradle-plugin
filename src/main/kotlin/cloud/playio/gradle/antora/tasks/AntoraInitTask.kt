package cloud.playio.gradle.antora.tasks

import cloud.playio.gradle.antora.AntoraPlugin

abstract class AntoraInitTask : AntoraCopyTask() {

    companion object {

        const val NAME = "antoraInitializer"
    }

    init {
        group = AntoraPlugin.GROUP
        description = "Initialize Antora document component"
    }
}
