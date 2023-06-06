package cloud.playio.gradle.antora

import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider

class AntoraLayoutImpl(
    override val dir: Provider<Directory>,
    override val module: Provider<String>
) : AntoraLayout
