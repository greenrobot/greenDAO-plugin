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
import com.natpryce.hamkrest.hasSize
import com.natpryce.hamkrest.isEmpty
import org.junit.Assert.*
import org.junit.Ignore
import org.junit.Test

/**
 * Tests if fields, constructors and methods are properly recognized.
 */
class VisitorTest : VisitorTestBase() {

    @Test
    fun fieldDef() {
        val entity = visit(
                //language=java
                """
        import org.greenrobot.greendao.annotation.*;

        @Entity
        class Foobar {
            String name;
            int age;
        }
        """)!!
        assertThat(entity.properties, equalTo(
                listOf(
                        ParsedProperty(Variable(StringType, "name")),
                        ParsedProperty(Variable(IntType, "age"), isNotNull = true)
                )
        ))
    }

    @Test
    fun noDefinitions() {
        val entity = visit(
                //language=java
                """
        import org.greenrobot.greendao.annotation.*;

        @Entity
        class Foobar {}
        """)!!
        assertThat(entity.properties, isEmpty)
        assertThat(entity.transientFields, isEmpty)
        assertThat(entity.methods, isEmpty)
        assertThat(entity.constructors, isEmpty)
        assertThat(entity.oneRelations, isEmpty)
        assertThat(entity.manyRelations, isEmpty)
        assertThat(entity.indexes, isEmpty)
    }

    @Test
    fun transientModifierTest() {
        val entity = visit(
                //language=java
                """
        import org.greenrobot.greendao.annotation.*;

        @Entity
        class Foobar {
            String name;
            transient int age;
        }
        """)!!
        assertThat(entity.properties.size, equalTo(1))
        assertThat(entity.transientFields.size, equalTo(1))
        assertThat(entity.properties[0].variable.name, equalTo("name"))
        assertThat(entity.transientFields[0].variable.name, equalTo("age"))
    }

    @Test
    fun transientAnnotationTest() {
        val entity = visit(
                //language=java
                """
        import org.greenrobot.greendao.annotation.*;

        @Entity
        class Foobar {
            String name;
            @Transient int transientImport;
            @org.greenrobot.greendao.annotation.Transient int transientQualified;
        }
        """)!!
        assertThat(entity.properties.size, equalTo(1))
        assertThat(entity.properties[0].variable.name, equalTo("name"))
        assertThat(entity.transientFields.size, equalTo(2))
        assertThat(entity.transientFields[0].variable.name, equalTo("transientImport"))
        assertThat(entity.transientFields[1].variable.name, equalTo("transientQualified"))
    }

    @Test
    fun staticFieldsAreTransientTest() {
        val entity = visit(
                //language=java
                """
        import org.greenrobot.greendao.annotation.*;

        @Entity
        class Foobar {
            static String staticField;
            static final String CONSTANT_FIELD;
            String name;
        }
        """)!!

        assertThat(entity.properties.size, equalTo(1))
        assertThat(entity.properties[0].variable.name, equalTo("name"))

        assertThat(entity.transientFields.size, equalTo(2))
        assertThat(entity.transientFields[0].variable.name, equalTo("staticField"))
        assertThat(entity.transientFields[1].variable.name, equalTo("CONSTANT_FIELD"))
    }

    @Test
    fun idAnnotation() {
        val entity = visit(
                //language=java
                """
        import org.greenrobot.greendao.annotation.*;

        @Entity
        class Foobar {
            @Id
            String name;
        }
        """)!!
        val entityField = entity.properties[0]
        assertNotNull(entityField.id)
        assertFalse(entityField.id!!.autoincrement)
    }

    @Test
    fun idAutoincrement() {
        val entity = visit(
                //language=java
                """
        import org.greenrobot.greendao.annotation.*;

        @Entity
        class Foobar {
            @Id(autoincrement = true)
            String name;
        }
        """)!!
        assertTrue(entity.properties[0].id!!.autoincrement)
    }

    @Test
    fun columnAnnotation() {
        val entity = visit(
                //language=java
                """
        import org.greenrobot.greendao.annotation.*;

        @Entity
        class Foobar {
            @Property(nameInDb = "SECOND_NAME")
            String name;
        }
        """)!!
        val field = entity.properties[0]
        assertEquals("SECOND_NAME", field.dbName)
    }

    @Test
    fun propertyIndexAnnotation() {
        val entity = visit(
                //language=java
                """
        import org.greenrobot.greendao.annotation.*;

        @Entity
        class Foobar {
            @Index(name = "NAME_INDEX", unique = true)
            String name;
        }
        """)!!
        val field = entity.properties[0]
        val index = field.index!!
        assertEquals("NAME_INDEX", index.name)
        assertTrue(index.unique)
    }

    @Test(expected = RuntimeException::class)
    fun propertyIndexAnnotationDoesNotAcceptIndexSpec() {
        visit(
                //language=java
                """
        import org.greenrobot.greendao.annotation.*;

        @Entity
        class Foobar {
            @Index(value = "name desc")
            String name;
        }
        """)!!
    }

    @Test
    fun propertyUniqueAnnotation() {
        val entity = visit(
                //language=java
                """
        import org.greenrobot.greendao.annotation.*;

        @Entity
        class Foobar {
            @Unique
            String name;
        }
        """)!!
        assertTrue(entity.properties[0].unique)
    }

    @Test
    fun convertAnnotation() {
        val entity = visit(
                //language=java
                """
        package com.example.myapp;

        import org.greenrobot.greendao.annotation.Entity;
        import org.greenrobot.greendao.annotation.Convert;
        import com.example.myapp.Converter;

        @Entity
        class Foobar {
            @Convert(converter = Converter.class, columnType = String.class)
            MyType name;
        }
        """, listOf("MyType"))!!
        val field = entity.properties[0]
        assertEquals(
                ParsedProperty(
                        Variable(VariableType("com.example.myapp.MyType", isPrimitive = false, originalName = "MyType"), "name"),
                        customType = CustomType(
                                "com.example.myapp.Converter", StringType
                        )
                ),
                field
        )
    }

    @Test
    fun convertAnnotation_innerClass() {
        val entity = visit(
                //language=java
                """
        package com.example.myapp;

        import org.greenrobot.greendao.annotation.Entity;
        import org.greenrobot.greendao.annotation.Convert;
        import org.greenrobot.greendao.converter.PropertyConverter;

        @Entity
        class Foobar {
            @Convert(converter = InnerConverter.class, columnType = String.class)
            MyType name;
            public static class InnerConverter implements PropertyConverter<MyType, String> {
                @Override
                public MyType convertToEntityProperty(String databaseValue) {
                    return null;
                }

                @Override
                public String convertToDatabaseValue(MyType entityProperty) {
                    return null;
                }
            }
        }
        """, listOf("MyType"))!!
        val field = entity.properties[0]
        assertEquals(
                ParsedProperty(
                        Variable(VariableType("com.example.myapp.MyType", isPrimitive = false, originalName = "MyType"), "name"),
                        customType = CustomType(
                                "com.example.myapp.Foobar.InnerConverter", StringType
                        )
                ),
                field
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun convertAnnotation_innerClassNotStatic() {
        visit(
                //language=java
                """
        package com.example.myapp;

        import org.greenrobot.greendao.annotation.Entity;
        import org.greenrobot.greendao.annotation.Convert;
        import org.greenrobot.greendao.converter.PropertyConverter;

        @Entity
        class Foobar {
            @Convert(converter = InnerConverter.class, columnType = String.class)
            MyType name;
            public class InnerConverter implements PropertyConverter<MyType, String> {
                @Override
                public MyType convertToEntityProperty(String databaseValue) {
                    return null;
                }

                @Override
                public String convertToDatabaseValue(MyType entityProperty) {
                    return null;
                }
            }
        }
        """, listOf("MyType"))!!
    }

    @Test(expected = IllegalArgumentException::class)
    fun convertAnnotation_innerTypeNotStatic() {
        visit(
                //language=java
                """
        package com.example.myapp;

        import org.greenrobot.greendao.annotation.Entity;
        import org.greenrobot.greendao.annotation.Convert;
        import org.greenrobot.greendao.converter.PropertyConverter;

        @Entity
        class Foobar {
            @Convert(converter = MyConverter.class, columnType = String.class)
            MyType name;

            public class MyType {
            }
        }
        """, listOf("MyConverter"))!!
    }

    @Test
    fun multiIndexes() {
        val entity = visit(
                //language=java
                """
        import org.greenrobot.greendao.annotation.*;

        @Entity(indexes = {
            @Index(value = "name DESC, age", unique = true, name = "NAME_AGE_INDEX"),
            @Index("age, name")
        })
        class Foobar {
            String name;
            int age;
        }
        """)!!

        assertThat(entity.indexes, equalTo(listOf(
                TableIndex("NAME_AGE_INDEX", listOf(
                        OrderProperty("name", Order.DESC),
                        OrderProperty("age", Order.ASC)
                ), unique = true),
                TableIndex(null, listOf(
                        OrderProperty("age", Order.ASC),
                        OrderProperty("name", Order.ASC)
                ), unique = false)
        )))
    }

    @Test
    fun constructors() {
        val entity = visit(
                //language=java
                """
        import org.greenrobot.greendao.annotation.*;

        @Entity
        class Foobar {
            String name;
            int age;

            @Generated(hash = -1)
            Foobar(String name, int age) {
            }

            Foobar() {
            }
        }
        """)!!
        assertThat(entity.constructors.size, equalTo(2))
        assertThat(entity.constructors[0].parameters, equalTo(
                listOf(Variable(StringType, "name"), Variable(IntType, "age"))
        ))
        assertNull(entity.constructors[1].hint)
        assertThat(entity.constructors[1].parameters, equalTo(emptyList()))
        assertFalse(entity.constructors[1].generated)
    }

    @Test
    fun methods() {
        val entity = visit(
                //language=java
                """
        import org.greenrobot.greendao.annotation.*;

        @Entity
        class Foobar {
            String name;

            public void setName(String name) {
                this.name = name;
            }

            public String getName() {
                return this.name;
            }
        }
        """)!!

        assertEquals(2, entity.methods.size)
        assertEquals("setName", entity.methods[0].name)
        assertThat(entity.methods[0].parameters, equalTo(listOf(Variable(StringType, "name"))))
        assertEquals("getName", entity.methods[1].name)
        assertThat(entity.methods[1].parameters, equalTo(emptyList()))
    }

}