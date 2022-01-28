package io.zero88.gradle.helper

import org.gradle.tooling.UnsupportedVersionException
import org.gradle.util.GradleVersion

private val MIN_GRADLE_VERSION = GradleVersion.version("6.5")

@Throws(UnsupportedVersionException::class)
fun checkMinGradleVersion(pluginId: String) {
    if (GradleVersion.current() >= MIN_GRADLE_VERSION) {
        return
    }
    throw UnsupportedVersionException("The plugin '${pluginId}' requires at least Gradle ${MIN_GRADLE_VERSION.version} to be run.")
}
