@file:Suppress("DEPRECATION")

package io.zero88.gradle.helper

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.getPlugin
import org.gradle.kotlin.dsl.the
import org.gradle.util.GradleVersion

fun prop(project: Project, key: String, forceNull: Boolean = false): String? {
    val prop = prop(project, key, "")
    return if (prop == "" && forceNull) null else prop
}

fun prop(project: Project, key: String, fallback: String?): String {
    val fb = fallback ?: ""
    return if (project.hasProperty(key)) project.property(key) as String? ?: fb else fb
}

fun computeBaseName(project: Project): String {
    return computeProjectName(project, "-")
}

private fun computeProjectName(project: Project, sep: String, firstSep: String? = null): String {
    if (project.parent == null) {
        return prop(project, "baseName", project.name)
    }
    val s = if (project.parent?.parent == null && firstSep != null) firstSep else sep
    return computeProjectName(project.parent!!, sep, firstSep) + s + project.projectDir.name
}

class JavaProject {
    companion object {

        @JvmStatic
        fun getMainSourceSet(project: Project): SourceSet = getSourceSet(project, SourceSet.MAIN_SOURCE_SET_NAME)

        @JvmStatic
        fun getTestSourceSet(project: Project): SourceSet = getSourceSet(project, SourceSet.TEST_SOURCE_SET_NAME)

        @JvmStatic
        fun getSourceSet(project: Project, sourceSetName: String): SourceSet {
            if (GradleVersion.current() >= GradleVersion.version("7.0")) {
                return project.the<JavaPluginExtension>().sourceSets.getByName(sourceSetName)
            }
            return project.convention.getPlugin<JavaPluginConvention>().sourceSets.getByName(sourceSetName)
        }

    }
}

