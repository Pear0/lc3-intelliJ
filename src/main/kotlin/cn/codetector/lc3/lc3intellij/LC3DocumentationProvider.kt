package cn.codetector.lc3.lc3intellij

import cn.codetector.lc3.asm.Instruction
import cn.codetector.lc3.lc3intellij.psi.LC3PsiImmediate
import cn.codetector.lc3.lc3intellij.psi.LC3PsiInstruction
import cn.codetector.lc3.lc3intellij.psi.PsiLabel
import cn.codetector.lc3.lc3intellij.psi.PsiLabelReference
import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.util.parentOfType

class LC3DocumentationProvider : DocumentationProvider {

    override fun getUrlFor(element: PsiElement?, originalElement: PsiElement?): MutableList<String> {
        return ArrayList()
    }

    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
        if (originalElement is PsiLabelReference) {
            assert(element is PsiLabel)

            val value = originalElement.getIntegerValue()
            if (value != null) {
                val instruction = originalElement.parentOfType<LC3PsiInstruction>()?.getInstruction()

                var name = "offset"
                if (instruction == Instruction.ADD || instruction == Instruction.AND) {
                    name = "imm"
                }

                return "Label \"${originalElement.text}\"\n$name = $value"
            }
        }

        return null
    }

    override fun getDocumentationElementForLookupItem(
        psiManager: PsiManager?,
        `object`: Any?,
        element: PsiElement?
    ): PsiElement? {
        return null
    }

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        return null
    }

    override fun getDocumentationElementForLink(
        psiManager: PsiManager?,
        link: String?,
        context: PsiElement?
    ): PsiElement? {
        return null
    }
}