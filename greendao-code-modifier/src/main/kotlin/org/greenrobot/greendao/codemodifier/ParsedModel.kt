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

import org.greenrobot.eclipse.jdt.core.dom.*
import java.io.File

/**
 * @param name is fully qualified name (if it was resolved)
 * @param originalName original type name how it was appeared in the source (qualified or not)
 */
data class VariableType(val name: String, val isPrimitive: Boolean, val originalName: String,
                        val typeArguments: List<VariableType>? = null) {
    val simpleName: String
        get() = name.substringAfterLast('.')
}

data class Variable(val type: VariableType, val name: String)

data class ParsedProperty(val variable: Variable,
                          val id: EntityIdParams? = null,
                          val index: PropertyIndex? = null,
                          val isNotNull: Boolean = false,
                          val dbName: String? = null,
                          val customType: CustomType? = null,
                          val unique: Boolean = false)

data class TransientField(val variable: Variable,
                          override val node: FieldDeclaration,
                          override val hint: GeneratorHint?) : Generatable<FieldDeclaration>

data class CustomType(val converterClassName: String,
                      val columnJavaType: VariableType)

data class EntityIdParams(val autoincrement: Boolean)

data class PropertyIndex(val name: String?, val unique: Boolean)

data class TableIndex(val name: String?, val fields: List<OrderProperty>, val unique: Boolean)

data class OrderProperty(val name: String, val order: Order)

data class Method(val name: String, val parameters: List<Variable>,
                  override val node: MethodDeclaration,
                  override val hint: GeneratorHint?) : Generatable<MethodDeclaration> {
    fun hasSignature(name: String, paramsTypes: List<String>): Boolean {
        return this.name == name
                && (this.parameters.map { it.type.name } == paramsTypes
                || this.parameters.map { it.type.simpleName } == paramsTypes)
    }

    fun hasFullSignature(name: String, params: List<Variable>): Boolean {
        return this.name == name && this.parameters == params
    }
}

data class JoinOnProperty(val source: String, val target: String)

data class JoinEntitySpec(val entityName: String, val sourceIdProperty: String, val targetIdProperty: String)

data class OneRelation(val variable: Variable, val foreignKeyField: String? = null, val columnName: String? = null,
                       val isNotNull: Boolean = false, val unique: Boolean = false)

data class ManyRelation(val variable: Variable, val mappedBy: String? = null,
                        val joinOnProperties: List<JoinOnProperty> = emptyList(),
                        val joinEntitySpec: JoinEntitySpec? = null,
                        val order: List<OrderProperty>? = null)

data class ParsedEntity(val name: String, val schema: String,
                        val active: Boolean,
                        val properties: List<ParsedProperty>, val transientFields: List<TransientField>,
                        val legacyTransientFields: List<TransientField>,
                        val constructors: List<Method>, val methods: List<Method>,
                        val node: TypeDeclaration,
                        val imports: List<ImportDeclaration>, val packageName: String,
                        val dbName: String?,
                        val oneRelations: List<OneRelation>, val manyRelations: List<ManyRelation>,
                        val indexes: List<TableIndex>,
                        val sourceFile: File, val source: String,
                        val keepSource: Boolean,
                        val createInDb: Boolean,
                        val generateConstructors: Boolean,
                        val generateGettersSetters: Boolean,
                        val protobufClassName: String?,
                        val notNullAnnotation: String?,
                        val lastFieldDeclaration: FieldDeclaration?) {

    val qualifiedClassName: String
        get() = "$packageName.$name"

    val lastMethodDeclaration: MethodDeclaration?
        get() = methods.lastOrNull()?.node

    val lastConstructorDeclaration: MethodDeclaration?
        get() = constructors.lastOrNull()?.node

    /** @return entity fields in order of constructor parameters, if all-fields constructor exist,
     *          otherwise null */
    fun getPropertiesInConstructorOrder(): List<ParsedProperty>? {
        val fieldVarsSet = properties.map { it.variable }.toSet()
        return constructors.find { it.parameters.toSet() == fieldVarsSet }
                ?.let { constructor -> properties.sortedBy { constructor.parameters.indexOf(it.variable) } }
    }

}

sealed class GeneratorHint {
    object Keep : GeneratorHint()

    class Generated(val hash: Int) : GeneratorHint() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Generated) return false

            if (hash != other.hash) return false

            return true
        }

        override fun hashCode(): Int {
            return hash
        }

        override fun toString(): String {
            return "Generated(hash=$hash)"
        }
    }
}

enum class Order { ASC, DESC }

interface Generatable<NodeType : BodyDeclaration> {
    val hint: GeneratorHint?
    val node: NodeType
    val generated: Boolean
        get() = hint is GeneratorHint.Generated
    val keep: Boolean
        get() = hint == GeneratorHint.Keep
}