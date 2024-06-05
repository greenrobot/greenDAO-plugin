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
import org.greenrobot.eclipse.jdt.core.dom.ASTParser
import org.greenrobot.eclipse.jdt.core.dom.CompilationUnit
import org.greenrobot.eclipse.jdt.internal.compiler.impl.CompilerOptions
import org.junit.Assert
import org.mockito.Mockito
import java.io.File

/**
 * WARNING: this should behave similar to code in JdtCodeContext and EntityClassParser.
 */
fun parseCompilationUnit(javaCode: String): CompilationUnit {
    val jdtOptions = JavaCore.getOptions()
    jdtOptions.put(CompilerOptions.OPTION_Source, JdtCodeContext.JAVA_LEVEL)
    jdtOptions.put(CompilerOptions.OPTION_Compliance, JdtCodeContext.JAVA_LEVEL)
    jdtOptions.put(CompilerOptions.OPTION_Encoding, "UTF-8")

    val parser = ASTParser.newParser(EntityClassParser.AST_PARSER_LEVEL)
    parser.setCompilerOptions(jdtOptions)
    parser.setKind(ASTParser.K_COMPILATION_UNIT)

    // resolve bindings
    parser.setEnvironment(emptyArray(), emptyArray(), null, true)
    parser.setUnitName("/")
    parser.setBindingsRecovery(true)
    parser.setResolveBindings(true)

    parser.setSource(javaCode.toCharArray())
    val astRoot = parser.createAST(null) as CompilationUnit

    val problems = astRoot.problems
    if (problems != null && problems.isNotEmpty()) {
        System.err.println("Found ${problems.size} problem(s) parsing:")
        var fail = false
        problems.forEachIndexed { i, problem ->
            val message = "#${i + 1} @${problem.sourceLineNumber}: $problem (ID: ${problem.id}; error: ${problem.isError})"
            if (EntityClassParser.shouldReportProblem(problem.id)) {
                System.err.println(message)
                fail = true
            } else {
                System.out.println("[IGNORED] $message")
            }
        }
        if (fail) {
            Assert.fail("There were parsing problems, see log messages.")
        }
    }

    return astRoot
}

fun tryParseEntity(javaCode: String, classesInPackage: List<String> = emptyList()): ParsedEntity? {
    val visitor = EntityClassASTVisitor(javaCode, classesInPackage, -1, -1)
    val unit = parseCompilationUnit(javaCode)
    unit.accept(visitor)
    return visitor.createParsedEntity(Mockito.mock(File::class.java), javaCode)
}

fun parseEntity(javaCode: String): ParsedEntity = tryParseEntity(javaCode)!!

val StringType = VariableType("String", isPrimitive = false, originalName = "String")
val IntType = VariableType("int", isPrimitive = true, originalName = "int")