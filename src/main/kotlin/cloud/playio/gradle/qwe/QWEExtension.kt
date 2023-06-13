package cloud.playio.gradle.qwe

import cloud.playio.gradle.shared.TreeExtension
import org.gradle.api.Project

abstract class QWEExtension : TreeExtension {

    companion object {

        const val NAME = "qwe"

        fun get(project: Project): QWEExtension = TreeExtension.get(QWEExtension::class.java, project, NAME)
    }
}
