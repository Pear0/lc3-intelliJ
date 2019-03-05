package cn.codetector.lc3.lc3intellij.psi

import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiWhiteSpace
import javax.swing.Icon

open class PsiLabel(node: ASTNode) : LC3PsiElement(node), PsiNameIdentifierOwner {

    fun getPragma(): String? {
        var psi = nextSibling
        while (psi is PsiWhiteSpace) {
            psi = psi.nextSibling
        }

        if (psi is PsiComment) {
            return psi.text.let { if (it.startsWith(";")) it.substring(1) else it }.trim()
        }

        return null
    }

    override fun setName(name: String): PsiElement {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getNameIdentifier(): PsiElement? {
        return this
    }

    override fun getName(): String? {
        return text
    }

    override fun getPresentation(): ItemPresentation? {
        return object : ItemPresentation {
            override fun getLocationString(): String? {
                return "Location String"
            }

            override fun getIcon(unused: Boolean): Icon? {
                return null
            }

            override fun getPresentableText(): String? {
                return "Presentable Text"
            }
        }
    }
}