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

import org.greenrobot.eclipse.jdt.core.JavaCore
import org.greenrobot.eclipse.jdt.internal.compiler.impl.CompilerOptions
import java.io.File
import java.util.Hashtable

/**
 * Context for parsing and transformation
 */
class JdtCodeContext(val formattingOptions: FormattingOptions? = null, encoding: String) {

    companion object {
        val JAVA_LEVEL: String = CompilerOptions.VERSION_1_7
    }

    private val jdtOptions: Hashtable<String, String>
    private val classParser: EntityClassParser

    init {
        jdtOptions = JavaCore.getOptions()
        jdtOptions.put(CompilerOptions.OPTION_Source, JAVA_LEVEL)
        jdtOptions.put(CompilerOptions.OPTION_Compliance, JAVA_LEVEL)
        // it could be the encoding is never used by JDT itself for our use case, but just to be sure (and for future)
        jdtOptions.put(CompilerOptions.OPTION_Encoding, encoding)

        classParser = EntityClassParser(jdtOptions, encoding)
    }

    fun parse(javaFile: File, classesInPackage: List<String>) = classParser.parse(javaFile, classesInPackage)

    fun transformer(parsedEntity: ParsedEntity) = EntityClassTransformer(parsedEntity, jdtOptions, formattingOptions)

}
