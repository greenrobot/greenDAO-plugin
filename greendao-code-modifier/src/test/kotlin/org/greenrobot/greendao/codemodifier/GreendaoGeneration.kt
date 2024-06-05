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

object GreendaoGeneration {
    @JvmStatic fun main(args: Array<String>) {
        val files = listOf("notes/Note", "orders/Order", "orders/Customer",
            "orders/Employee", "orders/EmployeeOrder").map {
            File("../greendao-example/src/main/java/com/example/greendao/${it}.java")
        }

        val formattingOptions = FormattingOptions().apply {
            lineWidth = 120
        }

        val schemaOptions = SchemaOptions(
            name = "default",
            version = 1,
            daoPackage = null,
            outputDir = File("../greendao-example/src/generated/main/java"),
            testsOutputDir = File("../greendao-example/src/generated/test/java")
        )

//        val notesSchemaOptions = schemaOptions.copy(
//            name = "notes",
//            daoPackage = "com.example.greendao.notes",
//            version = 2
//        )

        Greendao3Generator(formattingOptions).run(files, mapOf(
            "default" to schemaOptions
//            "notes" to notesSchemaOptions
        ))
    }
}
