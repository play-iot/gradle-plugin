package cloud.playio.gradle

enum class NexusVersion(val snapshotUrl: String, val releaseUrl: String) {
    AFTER_2021_02_24(
        "https://s01.oss.sonatype.org/content/repositories/snapshots/",
        "https://s01.oss.sonatype.org/service/local/"
    ),
    BEFORE_2021_02_24(
        "https://oss.sonatype.org/content/repositories/snapshots/",
        "https://oss.sonatype.org/service/local/staging/deploy/maven2"
    );
}
