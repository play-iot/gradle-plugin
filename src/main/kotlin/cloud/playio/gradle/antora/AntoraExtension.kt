package cloud.playio.gradle.antora

import cloud.playio.gradle.shared.Documentation
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property

open class AntoraExtension(objects: ObjectFactory) : Documentation.DocumentationBranch(objects, AntoraPlugin.GROUP) {
    companion object {

        fun create(project: Project): AntoraExtension =
            Documentation.get(project).createBranch(AntoraExtension::class.java, AntoraPlugin.GROUP)
    }

    val antoraSrcDir: Property<String> = objects.property<String>().convention("src/antora")
    val antoraModule: Property<String> = objects.property<String>().convention(AntoraLayout.DEFAULT_MODULE)
    val antoraType: Property<AntoraType> = objects.property<AntoraType>().convention(AntoraType.COMPONENT)
    val asciiAttributes: MapProperty<String, Any> = objects.mapProperty<String, Any>().convention(emptyMap())
    val docVersion: Property<String> = objects.property()
    val javadocTitle: Property<String> = objects.property()
    val javadocProjects: ListProperty<Project> = objects.listProperty<Project>().convention(emptyList())
    val javadocInDir: ConfigurableFileCollection = objects.fileCollection()

}
