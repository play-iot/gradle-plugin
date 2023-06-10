package cloud.playio.gradle.shared

import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import java.nio.file.Paths

abstract class Documentation : TreeExtension {
    companion object {

        const val NAME = "documentation"
        const val OUT_DIR = "docs"

        fun get(project: Project): Documentation = TreeExtension.get(Documentation::class.java, project, NAME)

    }

    abstract class DocumentationBranch(objects: ObjectFactory, branch: String) {

        val outDir: Property<String> = objects.property<String>().convention(Paths.get(OUT_DIR, branch).toString())
    }
}
