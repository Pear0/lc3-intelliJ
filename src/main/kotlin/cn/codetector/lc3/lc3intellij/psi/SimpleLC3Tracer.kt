package cn.codetector.lc3.lc3intellij.psi

import cn.codetector.lc3.lc3intellij.psi.impl.LC3ASMDirectiveImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.LeafPsiElement
import java.lang.NumberFormatException

object SimpleLC3Tracer {

    data class LineInfo(val address: Int)

    fun parseLiteral(text: String): Int? {
        if (text.isEmpty()) return null

        if (text.startsWith("x")) {
            return try {
                text.substring(1).toInt(16)
            } catch (e: NumberFormatException) {
                null
            }
        }

        if (text.startsWith("#")) {
            return try {
                text.substring(1).toInt()
            } catch (e: NumberFormatException) {
                null
            }
        }

        return try {
            text.toInt()
        } catch (e: NumberFormatException) {
            null
        }
    }

    fun findInfoForLine(file: PsiFile, line: Int): LineInfo? {
        val children = file.children

        var currentAddress: Int? = null // null when outside an .orig block
        var currentLine = 0
        var hasInfo = false
        var memorySize = 0

        for (child in children) {
            if (child is LeafPsiElement && child.elementType == LC3Types.CRLF) {
                if (currentLine == line) {
                    return if (hasInfo) {
                        currentAddress?.let { LineInfo(it) }
                    } else {
                        null
                    }
                }

                hasInfo = false

                currentLine += 1
                currentAddress?.let { currentAddress = it + memorySize }
                memorySize = 0
                continue
            }

            when (child) {
                is LC3ASMDirectiveImpl -> {
                    val origDir = child.origDir
                    val endDir = child.endDir
                    val fillDir = child.fillDir
                    val blkwDir = child.blkwDir
                    val stringzDir = child.stringzDir

                    if (origDir != null) {

                        if (currentAddress != null) {
                            // TODO should indicate in editor this error
                        }

                        val addressString = origDir.lastChild.text
                        currentAddress = parseLiteral(addressString)
                    }

                    if (endDir != null) {
                        if (currentAddress == null) {
                            // TODO should indicate in editor this error
                        }
                        currentAddress = null
                    }

                    if (fillDir != null) {
                        hasInfo = true
                        memorySize = 1
                    }

                    if (blkwDir != null) {
                        hasInfo = true
                        memorySize = parseLiteral(blkwDir.lastChild.text) ?: 0
                    }

                    if (stringzDir != null) {
                        hasInfo = true

                        val literal = stringzDir.lastChild.text
                        if (literal != null && literal.startsWith("\"") && literal.endsWith("\"")) {
                            memorySize = literal.length - 1 // -2 for the quotes, +1 for the NUL byte
                        }
                    }

                }

                is PsiLabel -> {
                    hasInfo = true
                }
                is LC3PsiInstruction -> {
                    memorySize = 1
                    hasInfo = true
                }
            }

        }

        if (currentLine == line) {
            return if (hasInfo) {
                currentAddress?.let { LineInfo(it) }
            } else {
                null
            }
        }

        return null
    }

    fun findLineForElement(element: PsiElement): Int {
        var elem: PsiElement? = element
        while (elem!!.parent !is PsiFile) {
            elem = elem.parent
        }

        var lineCount = 0

        while (elem != null) {
            if (elem is LeafPsiElement && elem.elementType == LC3Types.CRLF) {
                lineCount += 1
            }

            elem = elem.prevSibling
        }

        return lineCount
    }

    fun findInfoForElement(element: PsiElement): LineInfo? {
        return findInfoForLine(element.containingFile, findLineForElement(element))
    }

}