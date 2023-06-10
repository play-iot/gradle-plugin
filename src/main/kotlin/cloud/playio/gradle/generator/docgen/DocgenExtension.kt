package cloud.playio.gradle.generator.docgen

import cloud.playio.gradle.shared.Documentation
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory

open class DocgenExtension(objects: ObjectFactory) : Documentation.DocumentationBranch(objects, DocgenPlugin.GROUP) {
    companion object {

        fun create(project: Project): DocgenExtension =
            Documentation.get(project).createBranch(DocgenExtension::class.java, DocgenPlugin.GROUP)
    }

    val ascii: AsciidocExtension = AsciidocExtension(objects)

    fun ascii(configuration: Action<AsciidocExtension>) {
        configuration.execute(ascii)
    }
}
