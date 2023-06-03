package cloud.playio.gradle.pandoc.tasks

import com.github.dockerjava.api.command.InspectContainerResponse
import org.testcontainers.containers.GenericContainer
import java.io.File

class PandocImage(dockerImageName: String) : GenericContainer<PandocImage>(dockerImageName) {

    private lateinit var output: File;

    fun withOutputFile(output: File): PandocImage {
        this.output = output
        return this
    }

    override fun containerIsStopping(containerInfo: InspectContainerResponse?) {
        this.copyFileFromContainer("${workingDirectory}/${output.name}", output.absolutePath)
    }

}
