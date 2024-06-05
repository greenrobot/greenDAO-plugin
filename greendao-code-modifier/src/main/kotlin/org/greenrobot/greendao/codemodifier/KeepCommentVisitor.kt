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

package org.greenrobot.greendao.codemodifier

import org.greenrobot.eclipse.jdt.core.dom.CompilationUnit
import org.greenrobot.eclipse.jdt.core.dom.LineComment

/**
 * Visitor to run for each comment in a compilation units comments list. It records the legacy keep fields positions.
 * http://stackoverflow.com/questions/3019729/how-to-access-comments-from-the-java-compiler-tree-api-generated-ast/9884987#9884987
 */
class KeepCommentVisitor(val entityUnit: CompilationUnit, val sourceSplit: List<String>) : LazyVisitor() {

    var keepFieldsStartLineNumber: Int = -1
    var keepFieldsEndLineNumber: Int = -1

    override fun visit(node: LineComment): Boolean {
        val lineNumber = entityUnit.getLineNumber(node.startPosition)
        val lineComment = sourceSplit[lineNumber - 1].trim()

        if (lineComment.startsWith("// KEEP FIELDS END")) {
            keepFieldsEndLineNumber = lineNumber
        } else if (lineComment.startsWith("// KEEP FIELDS")) {
            keepFieldsStartLineNumber = lineNumber
        }

        return false
    }

    /**
     * If start or end value is missing or end is before start, resets values.
     */
    fun validateLineNumbers() {
        if (keepFieldsStartLineNumber == -1 || keepFieldsEndLineNumber == -1
                || keepFieldsEndLineNumber < keepFieldsStartLineNumber) {
            keepFieldsStartLineNumber = -1
            keepFieldsEndLineNumber = -1
        }
    }

}
