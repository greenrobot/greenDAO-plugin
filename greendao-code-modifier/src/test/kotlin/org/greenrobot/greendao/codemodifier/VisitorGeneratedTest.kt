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

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Assert
import org.junit.Test

/**
 * Tests if @Generated and @Keep annotations are properly recognized and if generated code modifications are detected.
 */
class VisitorGeneratedTest : VisitorTestBase() {

    @Test
    fun fieldGeneratorHint() {
        val cityHash = CodeCompare.codeHash("@Transient String city;")
        val entity = visit(
                """
        import org.greenrobot.greendao.annotation.*;

        @Entity
        class Foobar {
            @Generated
            transient String name;

            @Keep
            transient int age;

            transient String surname;

            @Generated(hash = $cityHash)
            @Transient
            String city;
        }
        """.trimIndent())!!
        assertThat(entity.transientFields.map { it.hint }, equalTo(
                listOf(GeneratorHint.Generated(-1), GeneratorHint.Keep, null, GeneratorHint.Generated(cityHash))
        ))
    }

    @Test
    fun constructorGeneratorHint() {
        val constructorHash = CodeCompare.codeHash(
                "Foobar(String name, int age){}"
        )

        val entity = visit(
                //language=java
                """
        import org.greenrobot.greendao.annotation.*;

        @Entity
        class Foobar {
            String name;
            int age;

            @Generated
            Foobar(String name) {
            }

            @Generated(hash = $constructorHash)
            Foobar(String name, int age) {
            }

            @Keep
            Foobar(int age) {
            }

            Foobar() {
            }
        }
        """)!!
        Assert.assertEquals(entity.constructors[0].hint, GeneratorHint.Generated(-1))
        Assert.assertEquals(entity.constructors[1].hint, GeneratorHint.Generated(constructorHash))
        Assert.assertEquals(entity.constructors[2].hint, GeneratorHint.Keep)
        Assert.assertNull(entity.constructors[3].hint)
    }

    @Test
    fun methodGeneratorHint() {
        val entity = visit(
                //language=java
                """
        import org.greenrobot.greendao.annotation.*;

        @Entity
        class Foobar {
            String name;
            int age;

            @Generated
            void update() {
            }

            @Keep
            void remove() {
            }

            void refresh() {
            }
        }
        """)!!
        Assert.assertEquals(entity.methods[0].hint, GeneratorHint.Generated(-1))
        Assert.assertEquals(entity.methods[1].hint, GeneratorHint.Keep)
        Assert.assertNull(entity.methods[2].hint)
    }

    @Test(expected = RuntimeException::class)
    fun throwIfGeneratedFieldChanged() {
        visit(
                //language=java
                """
        import org.greenrobot.greendao.annotation.*;

        @Entity
        class Foobar {
            @Generated(hash = 10)
            transient String name;
        }
        """)!!
    }

    @Test(expected = RuntimeException::class)
    fun throwIfGeneratedConstructorChanged() {
        visit(
                //language=java
                """
        import org.greenrobot.greendao.annotation.*;

        @Entity
        class Foobar {
            @Generated(hash = 10)
            Foobar() {
                // body
            }
        }
        """)!!
    }

    @Test(expected = RuntimeException::class)
    fun throwIfGeneratedMethodChanged() {
        visit(
                //language=java
                """
        import org.greenrobot.greendao.annotation.*;

        @Entity
        class Foobar {
            @Generated(hash = 10)
            void hello() {
                // body
            }
        }
        """)!!
    }

}
