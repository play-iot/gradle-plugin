package io.zero88.gradle.generator

interface GeneratorSource {

    val sourceName: String
    val successorTaskName: String
    val classpathConfigName: String
}
