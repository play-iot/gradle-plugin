package io.zero88.gradle.qwe.systemd

import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property

@Suppress("UnstableApiUsage") open class QWESystemdExtension(objects: ObjectFactory) {

    companion object {

        const val NAME = "systemd"
    }

    @Input
    val enabled = objects.property<Boolean>().convention(true)

    @Input
    val javaPath = objects.property<String>().convention("/usr/bin/java")

    @Input
    val architectures = objects.listProperty<Arch>().convention(listOf(Arch.X86_64, Arch.ARM_V7))

    @Input
    val jvmProps = objects.listProperty<String>().empty()

    @Input
    val systemProps = objects.listProperty<String>().empty()

    @Input
    val serviceName = objects.property<String>().convention("")

    @Input
    val params = objects.mapProperty<String, String>().empty()

    enum class Arch(val code: String) {
        X86_64("amd64"), ARM_V6("armv6"), ARM_V7("armv7"), ARM_V8("armv8")
    }
}
