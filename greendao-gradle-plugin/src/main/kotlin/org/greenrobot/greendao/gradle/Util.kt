package org.greenrobot.greendao.gradle

import org.codehaus.groovy.runtime.MethodClosure

/** helper to create Groovy's Closure from Kotlin's block */
fun <P, R> Closure(block: (P) -> R) = MethodClosure(block, "invoke")

