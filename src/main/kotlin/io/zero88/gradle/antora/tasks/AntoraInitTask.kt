package io.zero88.gradle.antora.tasks

import io.zero88.gradle.antora.AntoraPlugin

abstract class AntoraInitTask : AntoraCopyTask() {

    companion object {

        const val NAME = "antoraInitializer"
    }

    init {
        group = AntoraPlugin.GROUP
        description = "Initialize Antora document component"
    }
}
