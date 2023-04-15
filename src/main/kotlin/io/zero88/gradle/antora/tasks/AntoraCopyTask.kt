package io.zero88.gradle.antora.tasks

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.internal.file.copy.CopyAction
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.Sync
import java.io.File

abstract class AntoraCopyTask : Sync() {

    @get:OutputDirectory
    abstract val target: DirectoryProperty

    override fun createCopyAction(): CopyAction {
        into(target)
        return super.createCopyAction();
    }

    override fun getDestinationDir(): File {
        return target.asFile.get()
    }

}
