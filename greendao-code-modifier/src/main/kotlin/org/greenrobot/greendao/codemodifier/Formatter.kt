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

import org.greenrobot.eclipse.jdt.core.formatter.CodeFormatter
import org.greenrobot.eclipse.jdt.internal.formatter.DefaultCodeFormatter
import org.greenrobot.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions
import org.greenrobot.eclipse.jface.text.Document
import org.greenrobot.eclipse.text.edits.TextEdit

/** formats the code with JDT according specified formatting */
class Formatter(var formatting: Formatting) {
    private val formatter : DefaultCodeFormatter

    init {
        val options = DefaultCodeFormatterOptions(DefaultCodeFormatterOptions.getJavaConventionsSettings().map)
        options.tab_size = formatting.tabulation.size
        options.tab_char = if (formatting.tabulation.tabChar == ' ') DefaultCodeFormatterOptions.SPACE
                           else DefaultCodeFormatterOptions.TAB
        options.page_width = formatting.lineWidth

        this.formatter = DefaultCodeFormatter(options)
    }

    fun format(javaCode: String): String {
        val formatEdits: TextEdit? =
            formatter.format(CodeFormatter.K_CLASS_BODY_DECLARATIONS, javaCode, 0, javaCode.length, 0, "\n")
        val doc = Document(javaCode)
        if (formatEdits != null) {
            formatEdits.apply(doc)
        } else {
            throw RuntimeException("Can't format the code. Check syntax correctness of the code.")
        }
        return doc.get()
    }
}
