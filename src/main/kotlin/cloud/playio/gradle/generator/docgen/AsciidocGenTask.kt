package cloud.playio.gradle.generator.docgen

import cloud.playio.gradle.generator.GeneratorTask

abstract class AsciidocGenTask : GeneratorTask() {

    companion object {

        const val NAME = "genAsciidoc"
    }

    init {
        description = "Generate asciidoc from Java code"
    }

    override fun processorArgs(): List<String> = listOf("-Adocgen.output=${destinationDirectory.get()}")
}
