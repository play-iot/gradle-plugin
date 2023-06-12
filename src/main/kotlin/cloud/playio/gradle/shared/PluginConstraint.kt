package cloud.playio.gradle.shared

import org.gradle.tooling.UnsupportedVersionException
import org.gradle.util.GradleVersion

interface PluginConstraint {

    fun checkPlugin(pluginId: String) {
        checkGradleVersion(pluginId)
    }

    fun minGradleVersion(): GradleVersion = GradleVersion.version("6.5")

    @Throws(UnsupportedVersionException::class)
    fun checkGradleVersion(pluginId: String) {
        val version = minGradleVersion()
        if (GradleVersion.current() >= version) {
            return
        }
        throw UnsupportedVersionException("The plugin '${pluginId}' requires at least Gradle ${version.version} to be run.")
    }
}
