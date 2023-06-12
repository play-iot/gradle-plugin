package cloud.playio.gradle.shared

import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyConstraintHandler

interface ProjectConstraint {

    fun verifyProject(project: Project) {
        verifyDepConstraints(project)
    }

    fun verifyDepConstraints(project: Project) {
        project.dependencies.constraints { enforceLog4j2() }
    }

    fun DependencyConstraintHandler.enforceLog4j2() {
        add("implementation", "org.apache.logging.log4j:log4j-core") {
            version {
                strictly("[2.17, 3[")
                prefer("2.17.0")
            }
            because(
                "CVE-2021-44228, CVE-2021-45046, CVE-2021-45105: " +
                    "Log4j vulnerable to remote code execution and other critical security vulnerabilities"
            )
        }
    }
}
