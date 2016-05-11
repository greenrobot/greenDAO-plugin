package org.greenrobot.greendao.codechanger

import org.eclipse.jdt.core.dom.AST
import org.eclipse.jdt.core.dom.ASTParser
import org.eclipse.jdt.core.dom.CompilationUnit
import java.io.File

class EntityClassParser(val jdtOptions: MutableMap<Any, Any>) {
    fun parse(javaFile : File, classesInPackage: List<String>) : EntityClass? {
        val source = javaFile.readText()
        val parser = ASTParser.newParser(AST.JLS8)
        parser.setCompilerOptions(jdtOptions)
        parser.setKind(ASTParser.K_COMPILATION_UNIT)

        // uncomment to have bindings resolved
        //        parser.setEnvironment(emptyArray(), sourceRoots.toTypedArray(), null, true)
        //        parser.setUnitName("/" + javaFile.path)
        //        parser.setBindingsRecovery(true)
        //        parser.setResolveBindings(true)

        parser.setSource(source.toCharArray())
        val astRoot = parser.createAST(null) as CompilationUnit

        val problems = astRoot.problems;
        if (problems != null && problems.size > 0) {
            println("Got {} problems compiling the source file: ${problems.size}");
            problems.forEach { println(it) }
        }

        val visitor = EntityClassASTVisitor(classesInPackage)
        astRoot.accept(visitor)

        return visitor.toEntityClass(javaFile, source)
    }
}