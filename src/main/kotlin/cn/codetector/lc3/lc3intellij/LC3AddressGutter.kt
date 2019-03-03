package cn.codetector.lc3.lc3intellij

import cn.codetector.lc3.lc3intellij.psi.SimpleLC3Tracer
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.TextAnnotationGutterProvider
import com.intellij.openapi.editor.colors.ColorKey
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.vcs.actions.ColorMode
import com.intellij.openapi.vcs.actions.ShowAnnotationColorsAction
import com.intellij.openapi.vcs.annotate.FileAnnotation
import com.intellij.psi.PsiDocumentManager
import java.awt.Color

class LC3AddressGutter : TextAnnotationGutterProvider {

    override fun getPopupActions(line: Int, editor: Editor): MutableList<AnAction> {
        return ArrayList()
    }

    override fun getColor(line: Int, editor: Editor): ColorKey? {
        return null
    }

    override fun getLineText(line: Int, editor: Editor): String? {
        val file = PsiDocumentManager.getInstance(editor.project!!).getPsiFile(editor.document)

        val info = SimpleLC3Tracer.findInfoForLine(file!!, line)

        return info?.let { "x${it.address.toString(16)}" } ?: ""
    }

    override fun getToolTip(line: Int, editor: Editor): String? {
        return null
    }

    override fun getStyle(line: Int, editor: Editor): EditorFontType {
        return EditorFontType.PLAIN
    }

    override fun getBgColor(line: Int, editor: Editor): Color? {
        return null
//        if (myColorScheme == null) return null
//        val type = ShowAnnotationColorsAction.getType()
//        val colorMap = if (type == ColorMode.AUTHOR) myColorScheme!!.second else myColorScheme!!.first
//        if (colorMap == null || type == ColorMode.NONE) return null
//        val number = myAnnotation.getLineRevisionNumber(line) ?: return null
//        return colorMap.get(number)
    }

    override fun gutterClosed() {
        println("OOF")
    }
}