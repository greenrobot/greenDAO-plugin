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

/**
 * Problems the AST parse may have when looking at entity source code but which have no impact on the plugin working
 * correctly. See [EntityClassParser.ignoredProblemIds].
 */
class VisitorIgnoredProblemsTest : VisitorTestBase() {

    @Test
    fun comparable() {
        visit(
                //language=java
                """
        import org.greenrobot.greendao.annotation.Entity;
        import org.jetbrains.annotations.NotNull;

        @Entity
        class Foobar implements Comparable<SomeType> {

            // Comparable implementation of unresolvable type
            @Override
            public int compareTo(@NotNull SomeType another) {
                return 0;
            }

        }
        """
        )
    }

    @Test
    fun missingTypeInMethod() {
        visit(
                //language=java
                """
        import org.greenrobot.greendao.annotation.Entity;

        @Entity
        class Foobar {

            public void doSomething() {
                // unresolvable type a method refers to
                SomeType some = getSomeType();
            }

            public SomeType getSomeType() {
                return null;
            }

        }
        """
        )
    }

    @Test
    fun undefinedName() {
        visit(
                //language=java
                """
        import org.greenrobot.greendao.annotation.Entity;
        import android.text.TextUtils;

        @Entity
        class Foobar {

            public void doSomething() {
                // unresolvable external class reference, like TextUtils
                TextUtils.isEmpty("");
            }

        }
        """
        )
    }

    @Test
    fun undefinedMethod() {
        visit(
                //language=java
                """
        import org.greenrobot.greendao.annotation.Entity;
        import android.text.TextUtils;

        // the imagined super class
        //class SuperFoobar {
        //    public void doSomethingSuper(String string) {
        //    }
        //}

        @Entity
        class Foobar extends SuperFoobar {

            public void doSomething() {
                // unresolvable super class reference
                doSomethingSuper("");
            }

        }
        """
        )
    }

    @Test
    fun unsupportedSuppressWarnings() {
        visit(
                //language=java
                """
        import org.greenrobot.greendao.annotation.Entity;

        // SuppressWarnings tokens supported by IntelliJ, but not Eclipse
        @SuppressWarnings("WeakerAccess")
        @Entity
        class Foobar {
        }
        """
        )
    }

}
