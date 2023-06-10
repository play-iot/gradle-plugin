package cloud.playio.gradle.shared;

import java.util.Optional;

import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionContainer;

public interface TreeExtension extends ExtensionAware {

    static <T extends TreeExtension> T get(Class<T> type, Project project, String name) {
        final ExtensionContainer extensions = project.getExtensions();
        return Optional.ofNullable(extensions.findByType(type)).orElseGet(() -> extensions.create(name, type));
    }

    default <E> E createBranch(Class<E> type, String name, Object... args) {
        return getExtensions().create(name, type, args);
    }

}
