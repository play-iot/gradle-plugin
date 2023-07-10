package cloud.playio.gradle

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.property

class GitHubConfig(objects: ObjectFactory) {
    companion object {

        const val REGISTRY_URL_KEY = "github.nexus.url"
        const val GITHUB_REPO_KEY = "github.repo"
    }

    val repo = objects.property<String>()
    val url = objects.property<String>().convention("https://github.com")
    val registryUrl = objects.property<String>().convention("https://maven.pkg.github.com")
    val scmConnection = objects.property<String>().convention("scm:git:git://git@github.com")

    /**
     * If `true`, add task `publishToGitHub` to publish artifact to GitHub
     */
    val publishToRegistry = objects.property<Boolean>().convention(false)

    fun getProjectUrl(): Provider<String> = url.zip(repo) { u, r -> "$u/$r" }
    fun getProjectRegistryUrl(): Provider<String> = registryUrl.zip(repo) { u, r -> "$u/$r" }
    fun getScmUrl(): Provider<String> = scmConnection.zip(repo) { u, r -> "$u/$r" }
}
