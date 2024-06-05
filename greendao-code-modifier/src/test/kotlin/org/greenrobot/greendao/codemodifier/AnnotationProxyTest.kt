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

import org.greenrobot.eclipse.jdt.core.dom.*
import org.greenrobot.eclipse.jdt.core.dom.Annotation
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Test

class AnnotationProxyTest {

    @Test
    fun defaultValuesMarkerAnnotation() {
        val proxy = proxy("@MyAnnotation class SomeClass {}")
        assertEquals("default-value", proxy.value)
        assertFalse(proxy.logical)
        assertEquals(42, proxy.number)
        assertArrayEquals(arrayOf("john", "jack"), proxy.names)
        assertArrayEquals(booleanArrayOf(), proxy.logicals)
        assertEquals("default-child-value", proxy.child.value)
        assertArrayEquals(arrayOf(), proxy.more)
    }

    @Test
    fun defaultValuesSingleValueAnnotation() {
        val proxy = proxy("""@MyAnnotation("newvalue") class SomeClass {}""")
        assertFalse(proxy.logical)
    }

    @Test
    fun defaultValuesNormalAnnotation() {
        val proxy = proxy("""@MyAnnotation(value = "newvalue") class SomeClass {}""")
        assertFalse(proxy.logical)
    }

    @Test
    fun stringLiteralSingleValue() {
        val proxy = proxy("""@MyAnnotation("newvalue") class SomeClass {}""")
        assertEquals("newvalue", proxy.value)
    }

    @Test
    fun stringLiteralNormalValue() {
        val proxy = proxy("""@MyAnnotation(value = "newvalue") class SomeClass {}""")
        assertEquals("newvalue", proxy.value)
    }

    @Test
    fun intLiteralNormalValue() {
        val proxy = proxy("""@MyAnnotation(number = 104) class SomeClass {}""")
        assertEquals(104, proxy.number)
    }

    @Test
    fun intLiteralNormalNegativeValue() {
        val proxy = proxy("""@MyAnnotation(number = -104) class SomeClass {}""")
        assertEquals(-104, proxy.number)
    }

    @Test
    fun booleanLiteralValue() {
        val proxy = proxy("""@MyAnnotation(logical = true) class SomeClass {}""")
        assertTrue(proxy.logical)
    }

    @Test
    fun internalAnnotationValue() {
        val proxy = proxy("""@MyAnnotation(child = @Child("son")) class SomeClass {}""")
        assertEquals("son", proxy.child.value)
    }

    @Test
    fun stringArrayValue() {
        val proxy = proxy("""@MyAnnotation(names = {"hans", "jacob"}) class SomeClass {}""")
        assertArrayEquals(arrayOf("hans", "jacob"), proxy.names)
    }

    @Test
    fun booleanArrayValue() {
        val proxy = proxy("""@MyAnnotation(logicals = {true, false}) class SomeClass {}""")
        assertArrayEquals(booleanArrayOf(true, false), proxy.logicals)
    }

    @Test
    fun annotationArrayValue() {
        val proxy = proxy("""@MyAnnotation(more = {@Child("child1"), @Child("child2")}) class SomeClass {}""")
        assertEquals(2, proxy.more.size)
        assertEquals("child1", proxy.more[0].value)
        assertEquals("child2", proxy.more[1].value)
    }

    fun proxy(code: String): MyAnnotation {
        val node = parseAnnotation(code)
        return AnnotationProxy<MyAnnotation>(node)
    }

    fun parseAnnotation(code: String): Annotation {
        val unit = parseCompilationUnit(code)
        val visitor = AnnotationVisitor()
        unit.accept(visitor)
        Assert.assertNotNull("Can't parse annotation from the source", visitor.node)
        return visitor.node!!
    }

    class AnnotationVisitor : ASTVisitor() {
        var node: Annotation? = null

        fun visitAnnotation(node: Annotation): Boolean {
            if (node.parent is TypeDeclaration) {
                this.node = node
            }
            return true
        }

        override fun visit(node: MarkerAnnotation): Boolean = visitAnnotation(node)
        override fun visit(node: SingleMemberAnnotation): Boolean = visitAnnotation(node)
        override fun visit(node: NormalAnnotation): Boolean = visitAnnotation(node)
    }

    annotation class MyAnnotation(val value: String = "default-value", val logical: Boolean = false,
                                  val number: Int = 42,
                                  val names: Array<String> = arrayOf("john", "jack"),
                                  val logicals: BooleanArray = booleanArrayOf(),
                                  val child: Child = Child(),
                                  val more: Array<Child> = arrayOf())

    annotation class Child(val value: String = "default-child-value")
}