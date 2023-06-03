package cloud.playio.gradle.qwe.app

import org.gradle.api.file.Directory
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.mapProperty

open class GeneratedLayoutExtension(objects: ObjectFactory, layout: ProjectLayout) {

    companion object {

        const val CONF = "conf"
        const val SERVICE = "service"
        const val JAVA = "java"
        const val KOTLIN = "kotlin"
        const val RESOURCES = "resources"
        const val TEST_JAVA = "testJava"
        const val TEST_KOTLIN = "testKotlin"
        const val TEST_RESOURCES = "testResources"
    }

    private val generatedDir = objects.directoryProperty().convention(layout.buildDirectory.dir("generated"))
    private val generatedSrcDir = objects.directoryProperty().convention(generatedDir.dir("main"))
    private val generatedTestDir = objects.directoryProperty().convention(generatedDir.dir("test"))
    private val defaultLayout = mapOf(
        CONF to Layout(LayoutMode.ARTIFACT, generatedDir.dir(CONF)),
        SERVICE to Layout(LayoutMode.ARTIFACT, generatedDir.dir(SERVICE)),
        JAVA to Layout(LayoutMode.SOURCE, generatedSrcDir.dir(JAVA)),
        KOTLIN to Layout(LayoutMode.SOURCE, generatedSrcDir.dir(KOTLIN)),
        RESOURCES to Layout(LayoutMode.RESOURCES, generatedSrcDir.dir(RESOURCES)),
        TEST_JAVA to Layout(LayoutMode.TEST_SOURCE, generatedTestDir.dir(JAVA)),
        TEST_KOTLIN to Layout(LayoutMode.TEST_SOURCE, generatedTestDir.dir(KOTLIN)),
        TEST_RESOURCES to Layout(LayoutMode.TEST_RESOURCES, generatedTestDir.dir(RESOURCES))
    )
    val generatedLayout = objects.mapProperty<String, Layout>().convention(defaultLayout)

    fun find(key: String): Layout? {
        return generatedLayout.get()[key] ?: defaultLayout[key]
    }

    enum class LayoutMode {
        SOURCE, RESOURCES, ARTIFACT, TEST_SOURCE, TEST_RESOURCES, TMP
    }

    class Layout(val mode: LayoutMode, val directory: Provider<Directory>)
}
