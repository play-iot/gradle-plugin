package cloud.playio.gradle.shared

import org.gradle.api.JavaVersion
import org.gradle.external.javadoc.MinimalJavadocOptions
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import java.nio.charset.StandardCharsets

fun MinimalJavadocOptions.createOptions() {
    encoding(StandardCharsets.UTF_8.name())
    charset(StandardCharsets.UTF_8.name())
    this as StandardJavadocDocletOptions
    this.addBooleanOption("Xdoclint:none", true)
    if (JavaVersion.current().majorVersion <= JavaVersion.VERSION_11.majorVersion) {
        // https://bugs.openjdk.java.net/browse/JDK-8215291
        // https://bugs.openjdk.java.net/browse/JDK-8215582
        this.addBooleanOption("-no-module-directories", true)
    }
    tags = mutableListOf(
        "apiNote:a:API Note:", "implSpec:a:Implementation Requirements:",
        "implNote:a:Implementation Note:"
    )
}
