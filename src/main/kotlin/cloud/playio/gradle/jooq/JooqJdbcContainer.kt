package cloud.playio.gradle.jooq

import org.gradle.api.Project
import org.gradle.process.JavaForkOptions
import org.jooq.meta.jaxb.Jdbc

/**
 * Load Database version `dbVersion` into systemProperty of Java exec
 * @param project Project
 * @param dbVersion given database version. if provides `null`, the value is got from project property
 */
fun JavaForkOptions.loadDbVersion(project: Project, dbVersion: String? = null) =
    setSystemProp(project, "dbVersion", dbVersion)

/**
 * Load Database schema file `dbSchemaFile` into systemProperty of Java exec
 * @param project Project
 * @param dbSchemaFile given database schema file. if provides `null`, the value is got from project property
 */
fun JavaForkOptions.loadDbSchema(project: Project, dbSchemaFile: String? = null) =
    setSystemProp(project, "dbSchemaFile", dbSchemaFile)

private fun JavaForkOptions.setSystemProp(project: Project, propName: String, propValue: String?) {
    (propValue ?: project.findProperty(propName))?.let { systemProperty(propName, it) }
}

class JooqJdbcContainer {

    companion object {

        /**
         * @see <a href="https://java.testcontainers.org/modules/databases/jdbc/#using-a-classpath-init-script">Init script from classpath</a>
         */
        fun createByClasspath(dbImage: String, schemaFile: String, jdbcUrlParams: Map<String, Any> = mapOf()): Jdbc =
            create(dbImage, jdbcUrlParams.plus("TC_INITSCRIPT" to schemaFile))

        /**
         * @see <a href="https://java.testcontainers.org/modules/databases/jdbc/#using-an-init-script-from-a-file">Init script from file</a>
         */
        fun createBySchema(dbImage: String, schemaFile: String, jdbcUrlParams: Map<String, Any> = mapOf()): Jdbc =
            create(dbImage, jdbcUrlParams.plus("TC_INITSCRIPT" to "file:${schemaFile}"))

        /**
         * @see <a href="https://java.testcontainers.org/modules/databases/jdbc/#using-an-init-function">Init function</a>
         */
        fun createByFunction(dbImage: String, functionName: String, jdbcUrlParams: Map<String, Any> = mapOf()): Jdbc =
            create(dbImage, jdbcUrlParams.plus("TC_INITFUNCTION" to functionName))

        fun create(dbImage: String, jdbcUrlParams: Map<String, Any> = mapOf()): Jdbc {
            val params = optimizeDockerParams().plus(jdbcUrlParams).map { "${it.key}=${it.value}" }.joinToString("&")
            return Jdbc()
                .withDriver("org.testcontainers.jdbc.ContainerDatabaseDriver")
                .withUrl("jdbc:tc:${dbImage}:///jooq?${params}")
        }

        private fun optimizeDockerParams(): Map<String, Any> {
            val os = System.getProperty("os.name").toLowerCase()
            return when {
                // https://docs.docker.com/storage/tmpfs/
                arrayOf("nix", "nux", "aix").any { os.contains(it) } -> mapOf("TC_TMPFS" to "/testtmpfs:rw")
                else                                                 -> mapOf()
            }
        }
    }

}
