package cn.codetector.lc3.lc3intellij.psi

import com.intellij.codeInsight.hint.HintManager
import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiNamedElement
import javax.swing.Icon

open class PsiLabel(node: ASTNode) : LC3PsiElement(node), PsiNameIdentifierOwner {

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