package org.greenrobot.greendao.codechanger

import org.eclipse.jdt.core.formatter.CodeFormatter
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatter
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions
import org.eclipse.jface.text.Document
import org.eclipse.text.edits.TextEdit

/** formats the code with JDT according specified formatting */
class Formatter(var formatting: Formatting) {
    private val formatter : DefaultCodeFormatter

    init {
        val options = DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getJavaConventionsSettings())
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
            throw RuntimeException("Can't format. Check syntax correctness of the code.")
        }
        return doc.get()
    }
}
