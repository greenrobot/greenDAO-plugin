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

import org.junit.Assert
import org.junit.Test

/**
 * Tests if types are properly recognized.
 */
class VisitorImportsTest : VisitorTestBase() {

    @Test(expected = RuntimeException::class)
    fun ambigousImport() {
        visit(
                //language=java
                """
        package com.example;

        import org.greenrobot.greendao.annotation.Entity;
        import org.greenrobot.greendao.annotation.ToOne;

        import net.yetanotherlib.*;

        @Entity
        class Foo {
            long barId;

            @ToOne(mappedBy = "barId")
            Bar bars;
        }
        """)
    }

    @Test
    fun resolveQualifiedNameInSamePackage() {
        val entity = visit(
                //language=java
                """
        package com.example;

        import org.greenrobot.greendao.annotation.Entity;
        import org.greenrobot.greendao.annotation.ToOne;

        import net.yetanotherlib.*;

        @Entity
        class Foo {
            long barId;

            @ToOne(mappedBy = "barId")
            Bar bars;
        }
        """, listOf("Bar"))!!

        Assert.assertEquals(BarType, entity.oneRelations[0].variable.type)
    }

    @Test
    fun resolveInternalClassInSamePackage() {
        val entity = visit(
                //language=java
                """
        package com.example;

        import org.greenrobot.greendao.annotation.Entity;
        import org.greenrobot.greendao.annotation.ToOne;

        import net.yetanotherlib.*;

        @Entity
        class Foo {
            long barId;

            @ToOne(mappedBy = "barId")
            Bar.Item bars;
        }
        """, listOf("Bar"))!!

        Assert.assertEquals(BarItemType, entity.oneRelations[0].variable.type)
    }

    @Test
    fun resolveFullyQualifiedNameIternalPackage() {
        val entity = visit(
                //language=java
                """
        package com.example2;

        import org.greenrobot.greendao.annotation.Entity;
        import org.greenrobot.greendao.annotation.ToOne;

        import net.yetanotherlib.*;

        @Entity
        class Foo {
            long barId;

            @ToOne(mappedBy = "barId")
            com.example.Bar.Item bars;
        }
        """)!!

        val fullBarItemType = VariableType("com.example.Bar.Item", false, "com.example.Bar.Item")
        Assert.assertEquals(fullBarItemType, entity.oneRelations[0].variable.type)
    }

    @Test
    fun resolveFullyQualifiedName() {
        val entity = visit(
                //language=java
                """
        package com.example2;

        import org.greenrobot.greendao.annotation.Entity;
        import org.greenrobot.greendao.annotation.ToOne;

        import net.yetanotherlib.*;

        @Entity
        class Foo {
            long barId;

            @ToOne(mappedBy = "barId")
            com.example.Bar bars;
        }
        """)!!

        val fullBarType = VariableType("com.example.Bar", false, "com.example.Bar")
        Assert.assertEquals(fullBarType, entity.oneRelations[0].variable.type)
    }

}
