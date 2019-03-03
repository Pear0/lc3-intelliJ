package cn.codetector.lc3.lc3intellij.psi

import com.intellij.codeInsight.hint.HintManager
import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReference
import com.intellij.psi.search.PsiElementProcessor
import com.intellij.psi.util.PsiElementFilter
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.lang.regexp.psi.RegExpGroup
import java.util.*
import javax.swing.Icon

open class PsiLabelReference(node: ASTNode) : LC3PsiElement(node), PsiReference, LC3PsiImmediate {

    override fun getElement(): PsiElement {
        return node.psi
    }

    override fun getReference(): PsiReference? {
        return this
    }

    override fun resolve(): PsiElement? {
        val ref = this

        val processor = PsiElementProcessor.FindFilteredElement<PsiElement>(object : PsiElementFilter {
            override fun isAccepted(element: PsiElement): Boolean {
                return (element is PsiNameIdentifierOwner) && ref.isReferenceTo(element)
            }
        })
        PsiTreeUtil.processElements(this.containingFile, processor)

        return if (processor.foundElement is PsiNameIdentifierOwner) processor.foundElement else null
    }

    override fun getVariants(): Array<Any> {

        val processor = PsiElementProcessor.CollectFilteredElements<PsiElement>(object : PsiElementFilter {
            override fun isAccepted(element: PsiElement): Boolean {
                return (element is PsiNameIdentifierOwner)
            }
        })
        PsiTreeUtil.processElements(this.containingFile, processor)

        return processor.collection.toList().toTypedArray()
    }

    override fun getIntegerValue(): Int? {
        val label = resolve() ?: return null
        val labelAddress = SimpleLC3Tracer.findInfoForElement(label)?.address ?: return null
        val myAddress = SimpleLC3Tracer.findInfoForElement(this)?.address ?: return null

        return labelAddress - (myAddress + 1)
    }

    override fun getRangeInElement(): TextRange {
        return TextRange.from(0, textRange.length)
    }

    override fun getCanonicalText(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun handleElementRename(newElementName: String?): PsiElement {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun bindToElement(element: PsiElement): PsiElement {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isSoft(): Boolean {
        return false
    }

    override fun isReferenceTo(element: PsiElement?): Boolean {
        return text == element?.text
    }

}