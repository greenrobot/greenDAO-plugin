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

data class Formatting(val tabulation: Tabulation, val lineWidth : Int) {
  companion object {
    fun detect(text : String, options: FormattingOptions? = null) : Formatting {
      val lines = text.lines()
      val lineWidth = options?.lineWidth
          // take closest "ten" for the max line width, but not less than 80 characters
          ?: Math.max(80, Math.round((lines.asSequence().map { it.length }.max() ?: 0) / 10f) * 10)
      val tabulation = options?.tabulation ?: run {
        // detect tabulation
        val spaces = lines.map { line ->
          line.takeWhile { it == ' ' }.length
        }.filter { it > 1 }
        val tabs = lines.map { line ->
          line.takeWhile { it == '\t' }.length
        }.filter { it > 0 }
        // detect tab chars
        if (spaces.count { it > 0 } > tabs.count { it > 0 }) {
          // more probably the file is formatted with spaces
          // detect tab length
          val tabSize = detectTabLength(spaces, 4, 2)
          Tabulation(' ', tabSize)
        } else {
          // more probably the file is formatted with tabs
          // detect tab length
          val tabSize = detectTabLength(tabs, 1, 1)
          Tabulation('\t', tabSize)
        }
      }

      return Formatting(tabulation, lineWidth = lineWidth)
    }

    fun detectTabLength(tabLengths : List<Int>, defaultLength : Int, min: Int) =
      // calculate differences between lines and find the most popular non-zero one
      tabLengths.asSequence().mapIndexed { index, tab ->
          if (index == 0) {
              tab
          } else {
              tab - tabLengths[index - 1]
          }
      }.filter{it >= min}.mostPopular() ?: defaultLength
  }
}

data class Tabulation(val tabChar : Char, val size: Int)

class FormattingOptions(var tabulation: Tabulation? = null,
                        var lineWidth: Int? = null) {
    /** @return formatting if all options are defined, otherwise null */
    fun toFormatting(): Formatting? {
        val tabulation = this.tabulation
        val lineWidth = this.lineWidth
        return if (tabulation != null && lineWidth != null) {
            Formatting(tabulation, lineWidth)
        } else {
            null
        }
    }
}