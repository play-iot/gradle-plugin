package cloud.playio.gradle.generator

interface GeneratorSource {

    val sourceName: String
    val successorTaskName: String
    val classpathConfigName: String
}
