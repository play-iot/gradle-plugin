package io.zero88.gradle.generator

import org.gradle.api.DomainObjectSet
import org.gradle.api.provider.Property

interface GeneratorTool<T : GeneratorSource> {

    /**
     * Tool name
     */
    val name: Property<String>

    /**
     * Tool lib name in format "<group-id>:<artifact-id>"
     */
    val lib: Property<String>

    /**
     * Tool lib version
     */
    val version: Property<String>

    /**
     * Tool processor name that stands for java annotation processor in java compiler
     */
    val processor: Property<String>

    /**
     * Sources to generate
     */
    val sources: DomainObjectSet<T>
}
