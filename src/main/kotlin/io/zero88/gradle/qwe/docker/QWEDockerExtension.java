package io.zero88.gradle.qwe.docker;

import org.gradle.api.Action;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import com.bmuschko.gradle.docker.DockerExtension;

@SuppressWarnings("UnstableApiUsage")
public class QWEDockerExtension extends DockerExtension {

    public static final String NAME = "docker";
    public final Property<Boolean> enabled;
    public final Property<String> maintainer;
    public final DockerfileExtension dockerfile;
    public final DockerImageExtension dockerImage;
    public final DirectoryProperty outputDirectory;

    public QWEDockerExtension(ObjectFactory objectFactory, ProjectLayout layout) {
        super(objectFactory);
        this.enabled = objectFactory.property(Boolean.class).convention(true);
        this.maintainer = objectFactory.property(String.class).convention("");
        this.dockerfile = new DockerfileExtension(objectFactory);
        this.dockerImage = new DockerImageExtension(objectFactory);
        this.outputDirectory = objectFactory.directoryProperty().convention(layout.getBuildDirectory().dir("docker"));
    }

    public void dockerfile(Action<DockerfileExtension> configuration) {
        configuration.execute(dockerfile);
    }

    public void dockerImage(Action<DockerImageExtension> configuration) {
        configuration.execute(dockerImage);
    }

}
