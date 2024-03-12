package cloud.playio.gradle

/**
 * @see [sonatype](https://central.sonatype.org/publish/publish-guide/#accessing-repositories)
 */
enum class NexusVersion(val snapshotUrl: String, val releaseUrl: String) {

    AFTER_2021_02_24(
        "https://s01.oss.sonatype.org/content/repositories/snapshots/",
        "https://s01.oss.sonatype.org/service/local/"
    ),
    BEFORE_2021_02_24(
        "https://oss.sonatype.org/content/repositories/snapshots/",
        "https://oss.sonatype.org/service/local/"
    );
}
