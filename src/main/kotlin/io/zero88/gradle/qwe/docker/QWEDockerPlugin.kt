package io.zero88.gradle.qwe.docker

import com.bmuschko.gradle.docker.DockerRemoteApiPlugin
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import io.zero88.gradle.OSSExtension
import io.zero88.gradle.helper.prop
import io.zero88.gradle.qwe.QWEDecoratorPlugin
import io.zero88.gradle.qwe.QWEExtension
import io.zero88.gradle.qwe.app.QWEAppExtension
import io.zero88.gradle.qwe.app.QWEAppPlugin
import io.zero88.gradle.qwe.docker.task.DockerMultipleRegistriesPushTask
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.*
import org.gradle.language.base.plugins.LifecycleBasePlugin

class QWEDockerPlugin : QWEDecoratorPlugin<QWEDockerExtension> {

    companion object {

        const val GROUP = "QWE Docker"
        const val ARG_DOCKER_REGISTRY = "dockerRegistries"
        const val ARG_DOCKER_TAGS = "dockerTags"
        const val ARG_DOCKER_LABELS = "dockerLabels"
        const val PLUGIN_ID = "io.github.zero88.gradle.qwe.docker"
    }

    override fun applyExternalPlugins(project: Project) {
        project.plugins.apply(DockerRemoteApiPlugin::class.java)
        project.plugins.apply(QWEAppPlugin::class.java)
    }

    override fun pluginId(): String {
        return PLUGIN_ID
    }

    override fun configureExtension(project: Project, ossExt: OSSExtension, qweExt: QWEExtension): QWEDockerExtension {
        val ext = (qweExt as ExtensionAware).extensions.create<QWEDockerExtension>(QWEDockerExtension.NAME)
        project.afterEvaluate {
            val name = ossExt.baseName.get()
            if (ossExt.zero88.get()) {
                ext.maintainer.set("${OSSExtension.DEV_ID} <${OSSExtension.DEV_EMAIL}>")
            }
            val registryParams = prop(project, ARG_DOCKER_REGISTRY, true)?.split(",")?.map { "${it}/${name}" }
            val tagParams = prop(project, ARG_DOCKER_TAGS)?.split(",")?.filter { s -> s.isNotEmpty() }
            val labelParams = prop(project, ARG_DOCKER_LABELS, true)?.split(",")?.filter { s -> s.isNotEmpty() }
            val dl = listOf("version=${project.version}", "maintainer=${ext.maintainer.get()}")
            val labels = dl + (if (labelParams.isNullOrEmpty()) listOf() else labelParams)
            ext.dockerImage.imageRegistries.addAll(registryParams ?: listOf(name))
            ext.dockerImage.tags.addAll(if (tagParams.isNullOrEmpty()) listOf(project.version.toString()) else tagParams)
            ext.dockerImage.labels.putAll(labels.map { s -> s.split("=") }.associate { it[0] to it[1] })
        }
        return ext
    }

    override fun registerAndConfigureTask(
        project: Project,
        ossExt: OSSExtension,
        qweExt: QWEExtension,
        decoratorExt: QWEDockerExtension
    ) {
        val appExt = (qweExt as ExtensionAware).extensions.getByType<QWEAppExtension>()
        project.tasks {
            if (decoratorExt.enabled.get()) {
                val dockerFileProvider = registerCreateDockerfileTask(ossExt.baseName, appExt, decoratorExt)
                val dockerBuildTask = registerDockerBuild(ossExt.baseName, decoratorExt, dockerFileProvider)
                named(LifecycleBasePlugin.BUILD_TASK_NAME).get().dependsOn(dockerBuildTask)
                registerPrintDockerfile(decoratorExt, dockerFileProvider)
                registerDockerPushTask(decoratorExt, dockerBuildTask)
            }
        }

    }

    private fun TaskContainerScope.registerCreateDockerfileTask(
        baseName: Property<String>,
        appExt: QWEAppExtension,
        dockerExt: QWEDockerExtension
    ): TaskProvider<Dockerfile> {
        return register<Dockerfile>("createDockerfile") {
            group = GROUP
            description = "Create Dockerfile"
            onlyIf { dockerExt.enabled.get() }
            val fqn = baseName.get() + "-" + project.version
            val df = dockerExt.dockerfile
            destFile.set(dockerExt.outputDirectory.file(baseName))

            from(df.image.get())
            workingDir(appExt.workingDir)
            addFile("distributions/${fqn}.tar", "./")
            runCommand("cp -rf $fqn/* ./ && rm -rf $fqn && mkdir -p ${appExt.dataDir.get()}")
            runCommand(df.userGroupCmd.orElse(df.generateUserGroupCmd(appExt.dataDir.get())))
            if (df.otherCmd.isPresent) {
                runCommand(df.otherCmd)
            }
            volume(appExt.dataDir.get())
            user(df.user)
            entryPoint("java")
            defaultCommand("-jar", "${fqn}.jar", "-conf", "conf/${appExt.configFile.get()}")
            exposePort(df.ports)
            label(dockerExt.dockerImage.labels)
        }
    }

    private fun TaskContainerScope.registerPrintDockerfile(
        dockerExt: QWEDockerExtension,
        provider: TaskProvider<Dockerfile>
    ) {
        register<DefaultTask>("printDockerfile") {
            group = GROUP
            description = "Show Dockerfile"

            onlyIf { dockerExt.enabled.get() }
            doLast {
                val instructions = provider.get().instructions.get()
                println(instructions.joinToString(System.lineSeparator()) { it.text })
            }
        }
    }

    private fun TaskContainerScope.registerDockerBuild(
        baseName: Property<String>,
        qweDockerExt: QWEDockerExtension,
        dockerFileProvider: TaskProvider<Dockerfile>
    ): TaskProvider<DockerBuildImage> {
        return register<DockerBuildImage>("buildDocker") {
            group = GROUP
            description = "Build Docker image"

            onlyIf { qweDockerExt.enabled.get() }
            dependsOn(project.tasks.getByName(LifecycleBasePlugin.ASSEMBLE_TASK_NAME), dockerFileProvider.get())

            inputDir.set(project.layout.buildDirectory)
            dockerFile.set(qweDockerExt.outputDirectory.file(baseName))
            images.value(qweDockerExt.dockerImage.toFQNImages())
        }
    }

    private fun TaskContainerScope.registerDockerPushTask(
        dockerExt: QWEDockerExtension,
        dockerBuildProvider: TaskProvider<DockerBuildImage>
    ) {
        register<DockerMultipleRegistriesPushTask>("pushDocker") {
            group = GROUP
            description = "Push Docker images to multiple remote registries"

            onlyIf { dockerExt.enabled.get() }
            dependsOn(dockerBuildProvider)
            images.set(dockerExt.dockerImage.toFQNImages())
        }
    }

}
