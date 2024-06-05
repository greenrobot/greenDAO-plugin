/*
 * greenDAO Build Tools
 * Copyright (C) 2016-2024 greenrobot (https://greenrobot.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.greenrobot.greendao.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.ChangeType
import org.gradle.work.InputChanges
import java.io.File
import java.nio.charset.Charset

/**
 * The task scans content of [sourceFiles] that were changes since the last build and detect if it is a possible
 * candidate for an entity.
 * All candidates (if they were changed since the last build) are written then as a list of paths
 * into [candidatesListFile].
 *
 * The [candidatesListFile] is written also in case the list itself was not changed, but there were changes in the
 * content of any candidate-file
 *
 * NOTE class should be opened because gradle inherits from it
 */
open class DetectEntityCandidatesTask : DefaultTask() {

    @InputFiles
    var sourceFiles: FileCollection? = null

    @OutputFile
    var candidatesListFile: File? = null

    @Input
    var version: String = "unknown"

    @Input
    var charset: String = "UTF-8"

    private val token = "org.greenrobot.greendao.annotation".toCharArray()

    @TaskAction
    fun execute(inputChanges: InputChanges) {
        val candidatesFile = this.candidatesListFile ?: throw IllegalStateException("candidates should be defined")
        val sourceFiles = this.sourceFiles ?: throw IllegalStateException("source files should be defined")
        val charset = charset(charset)

        if (inputChanges.isIncremental && candidatesFile.exists()) {
            processIncremental(inputChanges, sourceFiles, candidatesFile, charset)
        } else {
            processComplete(candidatesFile, sourceFiles, charset)
        }
    }

    private fun processComplete(candidatesFile: File, sourceFiles: FileCollection, charset: Charset) {
        val buffer = CharArray(DEFAULT_BUFFER_SIZE)

        sourceFiles.asSequence()
            .filter { Util.containsIgnoreSpaces(it, token, buffer, charset) }
            .map { it.path }
            .let { writeCandidates(it, candidatesFile) }
    }

    private fun processIncremental(
        inputChanges: InputChanges,
        sourceFiles: FileCollection,
        candidatesFile: File,
        charset: Charset
    ) {
        val buffer = CharArray(DEFAULT_BUFFER_SIZE)

        // read candidates, skipping first line with timestamp
        val oldCandidates = candidatesFile.readLines().drop(1).toSet()
        val newCandidates = oldCandidates.toMutableSet()
        // will be set to true if any previously known candidate was modified
        var modifiedExisting = false

        inputChanges.getFileChanges(sourceFiles).forEach { change ->
            if (change.changeType == ChangeType.ADDED || change.changeType == ChangeType.MODIFIED) {
                // check each modified file and add/remove candidates
                val file = change.file
                if (file.isFile) {
                    if (Util.containsIgnoreSpaces(file, token, buffer, charset)) {
                        val listChanged = newCandidates.add(file.path)
                        modifiedExisting = !listChanged or modifiedExisting
                    } else {
                        newCandidates.remove(file.path)
                    }
                }
            } else if (change.changeType == ChangeType.REMOVED) {
                // removed candidates for removed files
                newCandidates.remove(change.file.path)
            }
        }

        // only if list is changed or previously added candidate is changed
        // the latter one is a way to notify existing task about changed file
        if (newCandidates != oldCandidates || modifiedExisting) {
            writeCandidates(newCandidates.asSequence(), candidatesFile)
        }
    }

    /** write timestamp and list of [candidates] into [outputFile] */
    private fun writeCandidates(candidates: Sequence<String>, outputFile: File) {
        outputFile.printWriter().use { writer ->
            // first write timestamp to make file hash changed
            writer.println(System.currentTimeMillis())
            // Write candidates into file line by line and sort them. This avoids
            // the order of entity classes, e.g. in imports, changing dependent on
            // the OS a build runs on (Windows sorts A-Z, macOS no order).
            // https://github.com/greenrobot/greenDAO/issues/880
            candidates.sorted().forEach {
                writer.println(it)
            }
        }
    }

}