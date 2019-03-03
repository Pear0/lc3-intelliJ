package cn.codetector.lc3.lc3intellij.psi

import com.intellij.lang.ASTNode

open class LC3PsiValue(node: ASTNode) : LC3PsiElement(node), LC3PsiImmediate {

    override fun getIntegerValue(): Int? {
        return SimpleLC3Tracer.parseLiteral(text)
    }

}