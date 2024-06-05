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

package org.greenrobot.greendao.gradle

import org.junit.Assert.*
import org.junit.Test

class UtilKtTest {
    val buffer = CharArray(DEFAULT_BUFFER_SIZE)
    val token = "org.greenrobot.greendao.annotation".toCharArray()

    @Test
    fun containsIgnoreSpaces() {
        val source = "import org.greenrobot.greendao.annotation.*;".toByteArray().inputStream()
        assertTrue(Util.containsIgnoreSpaces(source, token, buffer, Charsets.UTF_8))
    }

    @Test
    fun containsIgnoreSpaces2() {
        val source = "@org\t.greenrobot .greendao\n\n\r.annotation.Entity".toByteArray().inputStream()
        assertTrue(Util.containsIgnoreSpaces(source, token, buffer, Charsets.UTF_8))
    }

    @Test
    fun containsIgnoreSpacesFalse() {
        val source = "@org\t.greenrobot .greendao\n\n\r fail".toByteArray().inputStream()
        assertFalse(Util.containsIgnoreSpaces(source, token, buffer, Charsets.UTF_8))
    }
}