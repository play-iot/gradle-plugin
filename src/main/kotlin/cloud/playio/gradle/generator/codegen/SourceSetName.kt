package cloud.playio.gradle.generator.codegen

import cloud.playio.gradle.generator.GeneratorSource
import cloud.playio.gradle.helper.JavaProject
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet

enum class SourceSetName(
    override val sourceName: String,
    override val successorTaskName: String,
    override val classpathConfigName: String
) : GeneratorSource {

    MAIN("main", "compileJava", "compileOnly"),
    TEST("test", "compileTestJava", "testImplementation"),
    TEST_FIXTURES("testFixtures", "compileTestFixturesJava", "testFixturesCompileOnly");

    companion object {

        fun createTaskName(sourceSetName: GeneratorSource, prefix: String): String =
            if (sourceSetName == MAIN) prefix else prefix + "For${sourceSetName.sourceName.capitalize()}"

        fun getSourceSet(sourceSetName: GeneratorSource, project: Project): SourceSet =
            JavaProject.getSourceSet(project, sourceSetName.sourceName)

        fun createGenerator(sourceSetName: GeneratorSource, suffix: String): String =
            if (sourceSetName == MAIN) suffix else sourceSetName.sourceName + suffix.capitalize()
    }
}
