package cn.codetector.lc3

import cn.codetector.lc3.asm.Instruction
import cn.codetector.lc3.lc3intellij.psi.LC3ElementType
import cn.codetector.lc3.lc3intellij.psi.LC3Types

fun foo() {
    LC3Types.ADD_INST
}

val LC3ElementType.instruction: Instruction?
    get() = when (this) {
        LC3Types.ADD_INST -> Instruction.ADD
        LC3Types.AND_INST -> Instruction.AND

        else -> null
    }