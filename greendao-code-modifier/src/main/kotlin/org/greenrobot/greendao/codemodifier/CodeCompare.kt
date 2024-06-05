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

import org.greenrobot.essentials.hash.Murmur3F
import org.greenrobot.eclipse.jdt.core.dom.ASTNode

object CodeCompare {
    private val regexTooManySpaces = Regex("[ \\n\\t\\r]+")
    private val regexUselessSpaces = Regex("\\s?(\\W)\\s?")
    private val regexJavaCommentMl = Regex("/\\*([^\\*]|\\*(?!/))*?\\*/")
    private val regexJavaCommentSl = Regex("//(.*)$")
    private val regexGeneratedAnnotation = Regex("@(org.greenrobot.greendao.annotation.)?Generated[(][^)]+[)]")
    private val murmur = Murmur3F()

    /** IMPORTANT: do not change this method without real need, because all the hashes can be invalidated */
    fun unformatCode(code: String) =
        // replace single-line with multi-line to do no mess up the code after \n is replaced
        code.replace(regexJavaCommentSl, "/*$1*/")
            .replace(regexJavaCommentMl, { "/*" + it.value.replace("*", "").trim() + "*/" })
            .replace(regexTooManySpaces, " ")
            .replace(regexUselessSpaces, "$1")
            .trim()

    fun codeHash(code: String): Int {
        murmur.reset()
        val unformattedCode = unformatCode(code.replace(regexGeneratedAnnotation, ""))
        murmur.update(unformattedCode.toByteArray())
        val intHash = murmur.value.toInt()
        // get rid of minus
        return intHash shl 1 ushr 1
    }

    /** checks code without formatting and comments are equal */
    fun isSameCode(node: ASTNode, code: String) : Boolean {
        val nodeCode = CodeCompare.unformatCode(node.toString())
        val unformattedCode = CodeCompare.unformatCode(code)
        return nodeCode == unformattedCode
    }
}

