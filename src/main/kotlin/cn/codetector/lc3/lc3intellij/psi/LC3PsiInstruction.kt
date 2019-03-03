package cn.codetector.lc3.lc3intellij.psi

import cn.codetector.lc3.asm.Instruction
import com.intellij.lang.ASTNode

open class LC3PsiInstruction(node: ASTNode) : LC3PsiElement(node) {

    fun getInstruction(): Instruction? {
        val instructionPsi = children[0].children[0]

        return when (instructionPsi) {
            is LC3ASMAddInst -> Instruction.ADD
            is LC3ASMAndInst -> Instruction.AND
            is LC3ASMBrInst -> Instruction.BR
            is LC3ASMJmpInst -> Instruction.JMP
            is LC3ASMJsrInst -> Instruction.JSR
            is LC3ASMJsrrInst -> Instruction.JSRR
            is LC3ASMLdInst -> Instruction.LD
            is LC3ASMLdiInst -> Instruction.LDI
            is LC3ASMLdrInst -> Instruction.LDR
            is LC3ASMLeaInst -> Instruction.LEA

            is LC3ASMNotInst -> Instruction.NOT
            is LC3ASMRetInst -> Instruction.RET
            is LC3ASMRtiInst -> Instruction.RTI
            is LC3ASMStInst -> Instruction.ST
            is LC3ASMStiInst -> Instruction.STI
            is LC3ASMStrInst -> Instruction.STR

            else -> null
        }
    }

}