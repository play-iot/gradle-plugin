import org.gradle.api.Project

object PluginLibs {

    object Version {

        const val sonarQube = "3.1.1"
        const val jooq = "5.2.1"
        const val nexusStaging = "0.22.0"
        const val docker = "6.7.0"
        const val gradlePluginPublish = "0.14.0"
        const val jvm = "1.3.72"
        const val testcontainers = "1.15.2"
    }

    const val sonarQube = "org.sonarqube"
    const val nexusStaging = "io.codearte.nexus-staging"
    const val gradlePluginPublish = "com.gradle.plugin-publish"
    const val jvm = "jvm"

    object Depends {

        const val docker = "com.bmuschko:gradle-docker-plugin:${Version.docker}"
        const val sonarQube = "org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:${Version.sonarQube}"
        const val jooq = "nu.studer:gradle-jooq-plugin:${Version.jooq}"
        const val testcontainers =  "org.testcontainers:cassandra:${Version.testcontainers}"
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
