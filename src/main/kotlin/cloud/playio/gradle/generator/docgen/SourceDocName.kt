package cloud.playio.gradle.generator.docgen

import cloud.playio.gradle.generator.GeneratorSource

enum class SourceDocName(
    override val sourceName: String,
    override val successorTaskName: String,
    override val classpathConfigName: String
) : GeneratorSource {

    ASCIIDOC("main", "", "compileOnly"),
}
