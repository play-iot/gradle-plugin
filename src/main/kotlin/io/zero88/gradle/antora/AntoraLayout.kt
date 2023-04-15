package io.zero88.gradle.antora

import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

/**
 * Defines Antora standard file and directory set.
 *
 * See [Antora Layout](https://docs.antora.org/antora/latest/standard-directories/)
 */
interface AntoraLayout {

    companion object {

        const val DESCRIPTOR_FILE = "antora.yml"
        const val NAV_FILE = "nav.adoc"
        const val DEFAULT_MODULE = "ROOT"

        @JvmStatic
        fun create(dir: Provider<Directory>, module: Provider<String>) = AntoraLayoutImpl(dir, module)
    }

    val dir: Provider<Directory>
    val module: Provider<String>

    fun descriptorFile(): Provider<RegularFile> = dir.map { it.file(DESCRIPTOR_FILE) }
    fun navFile(): Provider<RegularFile> = dir.map { it.file(NAV_FILE) }

    fun moduleDir(module: String? = null): Provider<Directory> {
        val modulesDir = dir.map { it.dir("modules") }
        return when {
            module.isNullOrBlank() -> modulesDir.flatMap { it.dir(this.module) }
            else                   -> modulesDir.map { it.dir(module) }
        }
    }

    fun pagesDir(module: String? = null): Provider<Directory> = toDir(module, AntoraDirectory.PAGES)
    fun partialsDir(module: String? = null): Provider<Directory> = toDir(module, AntoraDirectory.PARTIALS)
    fun attachmentsDir(module: String? = null): Provider<Directory> = toDir(module, AntoraDirectory.ATTACHMENTS)
    fun examplesDir(module: String? = null): Provider<Directory> = toDir(module, AntoraDirectory.EXAMPLES)
    fun imagesDir(module: String? = null): Provider<Directory> = toDir(module, AntoraDirectory.IMAGES)

    fun toDir(module: String?, dir: AntoraDirectory): Provider<Directory> =
        moduleDir(module).map { it.dir(dir.name.toLowerCase()) }

}
