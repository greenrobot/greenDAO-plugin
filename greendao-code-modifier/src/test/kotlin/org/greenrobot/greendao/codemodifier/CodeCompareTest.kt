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

import org.junit.Test

import org.junit.Assert.*

class CodeCompareTest {
    @Test
    fun unformatCodeJdoc() {
        val codeA =
            """
            /**
             * This is a test method.
             * with multiline java doc
             */
             public void test() {
                System.out.println("Hello world");
             }
            """.trimIndent()

        val codeB =
            """
            /**
             * This is a test method. with multiline java doc
             */
             public void test() {
                System.out.println("Hello world");
             }
            """.trimIndent()

        assertEquals(CodeCompare.unformatCode(codeA), CodeCompare.unformatCode(codeB))
    }

    @Test
    fun unformatCodeMlCommentInBody() {
        val codeA =
            """
             /** Prints a message*/
             public void test() {
                /*
                 * Let's first print a line
                 */
                System.out.println("Hello world");
             }
            """.trimIndent()

        val codeB =
            """
             /** Prints a message*/
             public void test() {
                /* Let's first print a line */
                System.out.println("Hello world");
             }
            """.trimIndent()

        assertEquals(CodeCompare.unformatCode(codeA), CodeCompare.unformatCode(codeB))
    }

    @Test
    fun unformatCodeSinglelineCommentInBody() {
        val codeA =
            """
             /** Prints a message*/
             public void test() {
                //          Let's first print a line
                System.out.println("Hello world");
             }
            """.trimIndent()

        val codeB =
            """
             /** Prints a message*/
             public void test() {
                // Let's first print a line
                System.out.println("Hello world");
             }
            """.trimIndent()

        assertEquals(CodeCompare.unformatCode(codeA), CodeCompare.unformatCode(codeB))
    }

    @Test
    fun codeHashConsiderJdocs() {
        val hashA = CodeCompare.codeHash(
            """
            /** Documentation */
            public transient String name;
            """.trimIndent()
        )

        val hashB = CodeCompare.codeHash(
            """
            /** Good documentation */
            public transient String name;
            """.trimIndent()
        )

        assertNotEquals(hashA, hashB)
    }

    @Test
    fun codeHashConsiderJdocs2() {
        val hashA = CodeCompare.codeHash(
            """
            @Generated(hash = 60841032)
            public Customer() {
            }
            """.trimIndent()
        )

        val hashB = CodeCompare.codeHash(
            """
            @Generated(hash = 60841032)
            public Customer() {
                /** test*/
            }
            """.trimIndent()
        )

        assertNotEquals(hashA, hashB)
    }

    @Test
    fun codeHashIgnoreJdocsFormattingChange() {
        val hashA = CodeCompare.codeHash(
            """
            /**
             * Documentation
             */
            public transient String name;
            """.trimIndent()
        )

        val hashB = CodeCompare.codeHash(
            """
            /** Documentation */
            public transient String name;
            """.trimIndent()
        )

        assertEquals(hashA, hashB)
    }

    @Test
    fun codeHashConsiderSingleLineComments() {
        val hashA = CodeCompare.codeHash(
            """
            public String getName() {
                // TODO
            }
            """.trimIndent()
        )

        val hashB = CodeCompare.codeHash(
            """
            public String getName() {
                // done
            }
            """.trimIndent()
        )

        assertNotEquals(hashA, hashB)
    }

    @Test
    fun codeHashConsiderMultiLineComments() {
        val hashA = CodeCompare.codeHash(
            """
            public String getName() {
                /* TODO
                  There should be some good getter implementation */
            }
            """.trimIndent()
        )

        val hashB = CodeCompare.codeHash(
            """
            public String getName() {
                /* TODO
                  There should be some VERY good getter implementation */
            }
            """.trimIndent()
        )

        assertNotEquals(hashA, hashB)
    }
}