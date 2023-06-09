package cloud.playio.gradle

import org.gradle.api.provider.Provider
import org.gradle.api.publish.maven.MavenPomDeveloper

class DeveloperInfo {
    companion object {

        val Individual: (MavenPomDeveloper).() -> Unit = {
            id.set("zero88")
            email.set("sontt246@gmail.com")
        }
        val Organization: (MavenPomDeveloper).() -> Unit = {
            id.set("playio-dev")
            email.set("dev@playio.cloud")
            organization.set("playio")
            organizationUrl.set("playio.cloud")
        }

        fun displayName(developer: MavenPomDeveloper): Provider<String> =
            developer.id.zip(developer.email) { id, email -> "$id <${email}>" }
    }
}
