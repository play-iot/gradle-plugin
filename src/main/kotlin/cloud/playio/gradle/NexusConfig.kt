package cloud.playio.gradle

enum class NexusConfig(val snapshotUrl: String, val releaseUrl: String) {
    CURRENT(
        "https://s01.oss.sonatype.org/content/repositories/snapshots/",
        "https://s01.oss.sonatype.org/service/local/"
    ),
    LEGACY(
        "https://oss.sonatype.org/content/repositories/snapshots/",
        "https://oss.sonatype.org/service/local/staging/deploy/maven2"
    );

    companion object {

        const val RELEASE_URL_KEY = "ossrh.release.url"
        const val SNAPSHOT_URL_KEY = "ossrh.snapshot.url"
        const val USER_KEY = "nexus.username"
        const val PASSPHRASE_KEY = "nexus.password"
    }

}
