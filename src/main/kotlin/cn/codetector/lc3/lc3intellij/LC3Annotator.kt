package cn.codetector.lc3.lc3intellij

import cn.codetector.lc3.asm.Immediate
import cn.codetector.lc3.asm.Instruction
import cn.codetector.lc3.lc3intellij.psi.LC3PsiImmediate
import cn.codetector.lc3.lc3intellij.psi.LC3PsiInstruction
import cn.codetector.lc3.lc3intellij.psi.LC3PsiValue
import cn.codetector.lc3.lc3intellij.psi.PsiLabelReference
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType

class LC3Annotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        // holder.createInfoAnnotation(element, element.toString())

        if (element is PsiLabelReference) {
            if (element.resolve() == null) {
                holder.createErrorAnnotation(element, "Cannot resolve label")
            }
        }

        if (element is LC3PsiImmediate) {
            val value = element.getIntegerValue()
            if (value != null) {
                val instruction = element.parentOfType<LC3PsiInstruction>()?.getInstruction()

                if (element is PsiLabelReference) {
                    var name = "offset"
                    if (instruction == Instruction.ADD || instruction == Instruction.AND) {
                        name = "imm"
                    }

                    holder.createInfoAnnotation(element, "$name = $value")
                }

                if (instruction?.immediate != null && value !in instruction.immediate!!.range) {
                    val range = instruction.immediate!!.range
                    holder.createErrorAnnotation(element, "$value not in range [${range.first}, ${range.last}]")
                }
            }
        }

        // older.createErrorAnnotation(element, "This is a test")
    }
}