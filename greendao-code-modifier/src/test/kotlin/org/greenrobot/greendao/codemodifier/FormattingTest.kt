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

import org.junit.Assert.*
import org.junit.Test

class FormattingTest {
    @Test
    fun detect4Spaces() {
        val formatting = Formatting.detect(
//language=java
"""
/**
 *
 * Long jdoc
 *
 */
class Foobar {
    /**
     * Long
     * jdoc
     */
    int age;
}
"""
        )
        assertEquals(Tabulation(' ', 4), formatting.tabulation)
    }

    @Test
    fun detect2Spaces() {
        val formatting = Formatting.detect(
            //language=java
            """
/**
 *
 * Long jdoc
 *
 */
class Foobar {
  int age;

  void run() {
    age++;
  }
}
"""
        )
        assertEquals(Tabulation(' ', 2), formatting.tabulation)
    }

    @Test
    fun detectTab() {
        val formatting = Formatting.detect(
            """
/**
 *
 * Long jdoc
 *
 */
class Foobar {
\tint age;
\tvoid run() {
\t\tage++;
\t}
}
"""
        )
        assertEquals(Tabulation('\t', 1), formatting.tabulation)
    }
}
