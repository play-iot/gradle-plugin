package io.zero88.gradle.antora.tasks

import io.zero88.gradle.antora.AntoraPlugin
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.compile.JavaCompile

@CacheableTask
abstract class AntoraAsciidocTask : JavaCompile() {

    companion object {

        const val NAME = "antoraAsciidoc"
    }

    init {
        group = AntoraPlugin.GROUP
        description = "Generate asciidoc from Java code"
    }

}
