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

import java.io.File

/**
 * Parses index spec from string, e.g.: "fieldA asc, fieldB DESC, fieldC"
 * */
fun parseIndexSpec(spec: String): List<OrderProperty> {
    require(spec.isNotBlank()) {
        "Index spec should not be empty"
    }
    return spec.split(',').map { it.trim() }.map { columnSpec ->
        require(columnSpec.length > 0, { "Wrong index spec: $spec" })
        val specPair = columnSpec.split(' ')
        when {
            specPair.size == 1 -> OrderProperty(specPair[0], Order.ASC)
            else -> OrderProperty(specPair[0], Order.valueOf(specPair[1].toUpperCase()))
        }
    }
}

fun String.nullIfBlank() : String? = if (isBlank()) null else this

/** More optimized than standard groupBy{}.maxBy{} */
fun <T> Sequence<T>.mostPopular(): T? {
    val counts = mutableMapOf<T, Int>()
    for (t in this) counts.put(t, counts.getOrElse(t, {0}) + 1)
    return counts.asSequence().maxBy { it.value }?.key
}

fun File.getJavaClassNames(): List<String> {
    require(this.isDirectory) { "The file should be a directory" }
    return list().filter { it.endsWith(".java", ignoreCase = true) }
        .map { File(this, it) }
        .filter { it.isFile }
        .map { it.nameWithoutExtension }
}

/** Measure block execution time */
inline fun <T> logTime(action: String, block: () -> T) : T {
    val start = System.currentTimeMillis()
    val result = block()
    val time = System.currentTimeMillis() - start;
    println("$action took $time ms")
    return result
}
