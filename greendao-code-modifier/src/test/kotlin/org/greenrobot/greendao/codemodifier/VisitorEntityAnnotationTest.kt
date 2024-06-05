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
import com.natpryce.hamkrest.isBlank
import org.junit.Assert
import org.junit.Test

/**
 * Tests if the @Entity annotation is properly parsed.
 */
class VisitorEntityAnnotationTest : VisitorTestBase() {

    @Test
    fun entityIsRecognised() {
        val entity = visit(
                //language=java
                """
        import org.greenrobot.greendao.annotation.Entity;

        @Entity class Foobar {}
        """)
        Assert.assertNotNull(entity)
    }

    @Test
    fun entityIsRecognisedQualifiedName() {
        val entity = visit(
                //language=java
                """
        @org.greenrobot.greendao.annotation.Entity class Foobar {}
        """)
        Assert.assertNotNull(entity)
    }

    @Test
    fun entityIsNotRecognized() {
        val entity = visit("class Foobar {}")
        Assert.assertNull(entity)
    }

    @Test
    fun entityIsNotRecognizedIfWrongAnnotation() {
        val entity = visit(
                //language=java
                """
        import myapp.Entity;

        @Entity class Foobar {}
        """)
        Assert.assertNull(entity)
    }

    @Test
    fun entityIsNotRecognizedWithoutAnnotationImport() {
        val entity = visit(
                //language=java
                """
        @Entity class Foobar {}
        """)
        Assert.assertNull(entity)
    }

    @Test
    fun entityIsRecognizedWithWildcardImports() {
        val entity = visit(
                //language=java
                """
        import org.greenrobot.greendao.annotation.*;
        import org.redrobot.reddao.annotations.*;

        @Entity class Foobar {}
        """)
        Assert.assertNotNull(entity)
    }

    @Test
    fun activeEntity() {
        val entity = visit(
                //language=java
                """
        import org.greenrobot.greendao.annotation.Entity;

        @Entity(active = true) class Foobar {}
        """)!!
        Assert.assertTrue(entity.active)
    }

    @Test
    fun entityName() {
        val entity = visit(
                //language=java
                """
        import org.greenrobot.greendao.annotation.*;

        @Entity class Foobar {}
        """)!!
        Assert.assertEquals("Foobar", entity.name)
    }

    @Test
    fun packageName() {
        val entity = visit(
                //language=java
                """
        package com.user.myapp;

        import org.greenrobot.greendao.annotation.*;

        @Entity class Foobar {}
        """)!!
        Assert.assertEquals("com.user.myapp", entity.packageName)
    }

    @Test
    fun noPackageName() {
        val entity = visit(
                //language=java
                """
        import org.greenrobot.greendao.annotation.*;

        @Entity class Foobar {}
        """)!!
        assertThat(entity.packageName, isBlank)
    }

    @Test
    fun noCustomTableName() {
        val entity = visit(
                //language=java
                """
        import org.greenrobot.greendao.annotation.*;

        @Entity class Foobar {}
        """)!!
        Assert.assertNull(entity.dbName)
    }

    @Test
    fun customTableName() {
        val entity = visit(
                //language=java
                """
        import org.greenrobot.greendao.annotation.*;

        @Entity(nameInDb = "BAR")
        class Foobar {}
        """)!!
        Assert.assertEquals("BAR", entity.dbName)
    }

    @Test
    fun defaultSchemaName() {
        val entity = visit(
                //language=java
                """
        import org.greenrobot.greendao.annotation.*;

        @Entity
        class Foobar {}
        """)!!
        Assert.assertEquals("default", entity.schema)
    }

    @Test
    fun customSchemaName() {
        val entity = visit(
                //language=java
                """
        import org.greenrobot.greendao.annotation.*;

        @Entity(schema="custom")
        class Foobar {}
        """)!!
        Assert.assertEquals("custom", entity.schema)
    }

    @Test
    fun noKeepAnnotation() {
        val entity = visit(
                //language=java
                """
        import org.greenrobot.greendao.annotation.*;

        @Entity
        class Foobar {}
        """)!!
        Assert.assertFalse(entity.keepSource)
    }

    @Test
    fun keepAnnotation() {
        val entity = visit(
                //language=java
                """
        import org.greenrobot.greendao.annotation.*;

        @Entity
        @Keep
        class Foobar {}
        """)!!
        Assert.assertTrue(entity.keepSource)
    }

}
