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

import org.greenrobot.eclipse.jdt.core.compiler.IProblem
import org.greenrobot.eclipse.jdt.core.dom.AST
import org.greenrobot.eclipse.jdt.core.dom.ASTParser
import org.greenrobot.eclipse.jdt.core.dom.Comment
import org.greenrobot.eclipse.jdt.core.dom.CompilationUnit
import java.io.File
import java.util.Hashtable

class EntityClassParser(val jdtOptions: Hashtable<String, String>, val encoding: String) {

    companion object {
        val AST_PARSER_LEVEL: Int = AST.JLS8

        // ignore errors about broken references to types/names defined outside of the entity class
        // the number is (problem id & IProblem.IgnoreCategoriesMask) as shown in log output
        val ignoredProblemIds: IntArray = intArrayOf(
                IProblem.UndefinedType, // 2
                IProblem.UndefinedName, // 50, external class refs, like TextUtils
                IProblem.UndefinedField, // 70
                IProblem.UnresolvedVariable, // 83
                IProblem.UndefinedMethod, // 100 entities with super class
                IProblem.MissingTypeInMethod, // 120
                IProblem.MissingTypeInConstructor, // 129
                IProblem.MissingTypeInLambda, // 271
                IProblem.ImportNotFound, // 390
                IProblem.AbstractMethodMustBeImplemented, // 400 Comparable<T>.compareTo(T) can not be checked
                IProblem.PublicClassMustMatchFileName, // 325 our tests violate this
                IProblem.UnhandledWarningToken, // 631 SuppressWarnings tokens supported by IntelliJ, but not Eclipse
                IProblem.MethodMustOverrideOrImplement // 634 Inner defined PropertyConverter overrides
        )

        fun shouldReportProblem(problemId: Int): Boolean {
            return !ignoredProblemIds.contains(problemId)
        }
    }

    fun parse(javaFile: File, classesInPackage: List<String>): ParsedEntity? {
        val source = javaFile.readText(charset(encoding))

        val parser = ASTParser.newParser(AST_PARSER_LEVEL)
        parser.setCompilerOptions(jdtOptions)
        parser.setKind(ASTParser.K_COMPILATION_UNIT)

        // resolve bindings
        parser.setEnvironment(emptyArray(), emptyArray(), null, true)
        parser.setUnitName("/" + javaFile.path)
        parser.setBindingsRecovery(true)
        parser.setResolveBindings(true)

        parser.setSource(source.toCharArray())
        val astRoot = parser.createAST(null) as CompilationUnit

        // filtering type and import errors as bindings are only resolved inside of the entity class
        // in a future version we might include the whole classpath so all bindings can be resolved
        val problems = astRoot.problems?.filter {
            val problemId = it.id
            val keep = shouldReportProblem(problemId)
            if (!keep) {
                System.out.println("[Verbose] Ignoring parser problem in ${javaFile}:${it.sourceLineNumber}: $it.")
            }
            keep
        }
        if (problems != null && problems.isNotEmpty()) {
            System.err.println("Found ${problems.size} problem(s) parsing \"${javaFile}\":")
            problems.forEachIndexed { i, problem ->
                System.err.println("#${i + 1} @${problem.sourceLineNumber}: $problem" +
                        " (ID: ${problem.id}; error: ${problem.isError})")
            }
            val first = problems[0]
            throw RuntimeException("Found ${problems.size} problem(s) parsing \"${javaFile}\". First problem:\n" +
                    first + " (${first.id} at line ${first.sourceLineNumber}).\n" +
                    "Run gradle with --info for more details.")
        }

        // try to find legacy KEEP FIELDS section
        val commentVisitor = KeepCommentVisitor(astRoot, source.split("\n"))
        val commentList = astRoot.commentList
        commentList.forEach {
            if (it is Comment) {
                it.accept(commentVisitor)
            }
        }
        commentVisitor.validateLineNumbers()

        val visitor = EntityClassASTVisitor(source, classesInPackage,
                commentVisitor.keepFieldsStartLineNumber, commentVisitor.keepFieldsEndLineNumber)
        astRoot.accept(visitor)

        return visitor.createParsedEntity(javaFile, source)
    }


}