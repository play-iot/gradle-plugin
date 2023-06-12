package cloud.playio.gradle

import cloud.playio.gradle.shared.prop
import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import java.net.URI

class NexusConfig {

    companion object {

        const val RELEASE_URL_KEY = "ossrh.release.url"
        const val SNAPSHOT_URL_KEY = "ossrh.snapshot.url"
        const val USER_KEY = "nexus.username"
        @SuppressWarnings("kotlin:S2068")
        const val PASSPHRASE_KEY = "nexus.password"
        const val NEXUS_VERSION_KEY = "nexus.version"

        private fun getConfigVersion(project: Project): NexusVersion = when {
            project.extra.has(NEXUS_VERSION_KEY) -> project.extra[NEXUS_VERSION_KEY] as NexusVersion
            else                                 -> NexusVersion.AFTER_2021_02_24
        }

        fun getReleaseUrl(project: Project): URI =
            project.uri(prop(project, RELEASE_URL_KEY, getConfigVersion(project).releaseUrl))

        fun getSnapshotUrl(project: Project): URI =
            project.uri(prop(project, SNAPSHOT_URL_KEY, getConfigVersion(project).snapshotUrl))
    }

}
