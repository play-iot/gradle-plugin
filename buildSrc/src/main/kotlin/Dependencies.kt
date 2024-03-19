import org.gradle.api.Project

object PluginLibs {

    object Version {

        const val jvm = "1.5.31"
        const val sonarQube = "3.3"

        const val nexusPublish = "1.3.0"
        const val gradlePluginPublish = "1.2.0"

        const val jacocoLogger = "2.0.0"
        const val testLogger = "3.1.0"

        const val jooq = "3.14.13"
        const val docker = "8.1.0"
        const val shadow = "7.1.2"
        const val testcontainers = "1.17.3"
        const val yaml = "2.2"
    }

    const val sonarQube = "org.sonarqube"
    const val nexusPublish = "io.github.gradle-nexus.publish-plugin"
    const val gradlePluginPublish = "com.gradle.plugin-publish"
    const val jvm = "jvm"

    object Depends {

        const val docker = "com.bmuschko:gradle-docker-plugin:${Version.docker}"
        const val jacocoLogger = "gradle.plugin.org.barfuin.gradle.jacocolog:gradle-jacoco-log:${Version.jacocoLogger}"
        const val nexusPublish = "io.github.gradle-nexus:publish-plugin:${Version.nexusPublish}"
        const val sonarQube = "org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:${Version.sonarQube}"
        const val shadow = "com.github.johnrengelman.shadow:com.github.johnrengelman.shadow.gradle.plugin:${Version.shadow}"
        const val testLogger = "com.adarshr:gradle-test-logger-plugin:${Version.testLogger}"
        const val yaml = "org.yaml:snakeyaml:${Version.yaml}"

        const val jooq = "org.jooq:jooq-meta:${Version.jooq}"
        const val testcontainers =  "org.testcontainers:testcontainers:${Version.testcontainers}"
    }
}

object TestLibs {

    object Version {

        const val junit5 = "5.7.0"
    }

    const val junit5Api = "org.junit.jupiter:junit-jupiter-api:${Version.junit5}"
    const val junit5Engine = "org.junit.jupiter:junit-jupiter-engine:${Version.junit5}"
}

fun prop(project: Project, key: String, fallback: String = ""): String {
    return if (project.hasProperty(key)) project.property(key) as String? ?: fallback else fallback
}
