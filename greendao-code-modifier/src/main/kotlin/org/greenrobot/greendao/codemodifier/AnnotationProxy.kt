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
import org.greenrobot.eclipse.jdt.core.dom.Annotation
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * Proxies JDT's {@link org.greenrobot.eclipse.jdt.core.dom.Annotation} to the actual annotation object,
 * allows to read annotation's data in the regular java's reflection way
 *
 * <p>
 * E.g. given the annotation:
 * '@interface Entity {
 *      String name() default "nonaname";
 * }
 *
 * End using class:
 * '@Entity("NOTES")
 * class Note { ... }
 *
 * Use AnnotationProxy like so:
 * val proxy = AnnotationProxy<Entity>(noteClassJdtAnnotationNode)
 * assert(proxy.name == "NOTES")
 * </p>
 *
 * <p>
 * Supported features:
 * - return default values, if no value is provided
 * - raw string literals
 * - raw boolean literals
 * - annotation as values, e.g. Table annotation is proxied automatically in "@Entity(table = @Table(name = "Hello"))"
 * - arrays of all above
 * </p>
 * <p>
 * Unsupported, but easy to implement:
 * - other primitive literals (int, long, byte...)
 * - references to enum values, which are available in classpath
 * </p>
 * <p>
 * Unsupported and hard to implement:
 * - class references (usually referenced classes are in user code, so they are not available in the current classpath)
 * - references to any primitive or String values from user code (e.g. "@Entity(name = MyConstants.ENTITY_NAME)")
 * - references to enum values from user code
 * </p>
 */
object AnnotationProxy {
    class Handler(val jdtAnnotation: Annotation) : InvocationHandler {
        override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any? {
            return when (jdtAnnotation) {
                is SingleMemberAnnotation -> if (method.name == "value") {
                    jdtAnnotation.value.javaValue(method.name, method.returnType)
                } else {
                    method.defaultValue
                }
                is NormalAnnotation -> {
                    jdtAnnotation[method.name]
                        ?.let { it.javaValue(method.name, method.returnType) }
                        ?: method.defaultValue
                }
                else -> method.defaultValue
            }
        }
    }

    operator fun invoke(jdtAnnotation: Annotation, type: Class<*>) : Any =
        Proxy.newProxyInstance(type.classLoader, arrayOf(type), Handler(jdtAnnotation))

    inline operator fun <reified T : kotlin.Annotation> invoke(jdtAnnotation: Annotation) : T =
        this(jdtAnnotation, T::class.java) as T

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any, C : Class<out T>> Expression.javaValue(methodName: String, expected: C) : T {
        when {
            expected.isArray -> if (this is ArrayInitializer) {
                val componentType = expected.componentType
                val expressions = this.expressions()
                val result = java.lang.reflect.Array.newInstance(componentType, expressions.size)
                expressions.forEachIndexed { i, exp ->
                    java.lang.reflect.Array.set(result, i, (exp as Expression).javaValue(methodName, componentType))
                }
                return result as T
            }
            expected == Boolean::class.javaPrimitiveType -> if (this is BooleanLiteral) {
                return booleanValue() as T
            } else if (this is Expression) {
                val constantValue = this.resolveConstantExpressionValue()
                if (constantValue != null && constantValue is Boolean) {
                    return constantValue as T
                }
            }
            expected == Int::class.javaPrimitiveType -> when(this) {
                is NumberLiteral, is PrefixExpression ->
                    // does not handle binary numbers
                    return Integer.decode(toString()) as T
            }
            expected == String::class.java -> if (this is StringLiteral) {
                return literalValue as T
            } else if (this is Expression) {
                val constantValue = this.resolveConstantExpressionValue()
                if (constantValue != null && constantValue is String) {
                    return constantValue as T
                }
            }
            expected.isAnnotation -> if (this is Annotation) {
                return AnnotationProxy(this, expected) as T
            }
        }

        throw RuntimeException("Value for $methodName should be of type ${expected.simpleName} " +
                "(could not convert from ${this.javaClass}). Note: only inline constants are supported.")
    }
}
