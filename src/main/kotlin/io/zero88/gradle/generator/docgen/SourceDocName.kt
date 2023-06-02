package io.zero88.gradle.generator.docgen

import io.zero88.gradle.generator.GeneratorSource

enum class SourceDocName(
    override val sourceName: String,
    override val successorTaskName: String,
    override val classpathConfigName: String
) : GeneratorSource {

    ASCIIDOC("main", "", "compileOnly"),
}
