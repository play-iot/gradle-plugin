package io.zero88.gradle.qwe.docker

import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property

@Suppress("UnstableApiUsage")
open class DockerfileExtension(objects: ObjectFactory) {

    val image = objects.property<String>().convention("openjdk:8-jre-slim")
    val user = objects.property<String>().convention("qwe")
    val userId = objects.property<Int>().convention(804)
    val userGroup = objects.property<String>().convention("root")
    val userGroupId = objects.property<Int>().convention(0)
    val userGroupCmd = objects.property<String>()
    val otherCmd = objects.property<String>()
    val ports = objects.listProperty<Int>().convention(listOf(8080, 5000))

    fun generateUserGroupCmd(dataDir: String): String {
        return (if (this.userGroupId.get() == 0) "" else "groupadd -f -g ${this.userGroupId.get()} ${this.userGroup.get()} && ") +
            "useradd -u ${this.userId.get()} -G ${this.userGroup.get()} ${this.user.get()} " +
            "&& chown -R ${this.user.get()}:${this.userGroup.get()} $dataDir && chmod -R 755 $dataDir"
    }
}
