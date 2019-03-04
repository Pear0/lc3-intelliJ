package cn.codetector.lc3.lc3intellij.psi

import cn.codetector.lc3.asm.FullInstruction
import cn.codetector.lc3.asm.Instruction
import cn.codetector.lc3.asm.VirtualLC3
import cn.codetector.lc3.lc3intellij.psi.impl.LC3ASMDirectiveImpl
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.LeafPsiElement
import java.lang.NumberFormatException

object SimpleLC3Tracer {

    data class LineInfo(
        val address: Int,
        val label: String? = null,
        val data: Int? = null,
        val invalid: Boolean = false,
        val instruction: FullInstruction? = null,
        val psi: PsiElement? = null,
        val pragma: String? = null
    )

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

    fun processInstruction(inst: LC3PsiInstruction): FullInstruction? {
        val opcode = inst.getInstruction() ?: return null

        val actualInstruction = inst.firstChild.firstChild

        val args = ArrayList<VirtualLC3.InstArg>()
        var child: PsiElement? = actualInstruction.firstChild
        while (child != null) {
            if (child is LeafPsiElement && child.elementType == LC3Types.BR_OP) {
                val opText = child.text.toUpperCase()
                assert(opText.startsWith("BR"))

                val flags = opText.substring(2)
                if (flags.isEmpty()) {
                    args.add(VirtualLC3.Conditional.UNCONDITIONAL)
                } else {
                    args.add(VirtualLC3.Conditional(
                        flags.contains('N'),
                        flags.contains('Z'),
                        flags.contains('P')
                    ))
                }
            }

            if (child is LeafPsiElement && child.elementType == LC3Types.REGISTER) {
                args.add(VirtualLC3.Register.valueOf(child.text.toUpperCase().trim()))
            }
            if (child is LC3ASMImmediate) {
                val value = when {
                    child.refLabel != null -> (child.refLabel!! as LC3PsiImmediate).getIntegerValue()
                    child.value != null -> (child.value!! as LC3PsiImmediate).getIntegerValue()
                    else -> null
                }

                if (value == null) {
                    println("Unresolved immediate, aborting")
                    return null
                }

                args.add(VirtualLC3.Value(value))
            }
            child = child.nextSibling
        }

        return FullInstruction(opcode, args.getOrNull(0), args.getOrNull(1), args.getOrNull(2))
    }

    fun parseFile(file: PsiFile): List<LineInfo> {

        val infos = ArrayList<LineInfo>()

        var currentAddress: Int? = null // null when outside an .orig block
        var currentLine = 0
        var memorySize = 0
        var currentLabel: String? = null
        var currentPragma: String? = null

        for (child in file.children) {
            if (child is LeafPsiElement && child.elementType == LC3Types.CRLF) {
                currentLine += 1
                currentAddress?.let { currentAddress = it + memorySize }
                memorySize = 0
                continue
            }

            if (child is PsiComment && LC3Pragma.isPragma(child)) {
                val pragma = LC3Pragma.getPragma(child)

                // if this pragma is after a label, attach to the next instruction
                // otherwise, attach to the previous instruction

                if (currentLabel == null) {
                    if (infos.isNotEmpty()) {
                        val li = infos.size - 1
                        infos[li] = infos[li].copy(pragma = pragma)
                    }
                } else {
                    currentPragma = pragma
                }
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
                        memorySize = 1

                        val literal = parseLiteral(fillDir.lastChild.text)

                        infos.add(LineInfo(
                            currentAddress!!,
                            currentLabel,
                            literal,
                            literal == null,
                            null,
                            child
                        ))

                        currentLabel = null
                    }

                    if (blkwDir != null) {

                        val size = parseLiteral(blkwDir.lastChild.text)
                        memorySize = size ?: 0

                        for (i in 0 until (size ?: 0)) {
                            infos.add(LineInfo(
                                currentAddress!! + i,
                                currentLabel,
                                0,
                                false,
                                null,
                                child
                            ))

                            currentLabel = null
                        }

                    }

                    if (stringzDir != null) {

                        val literal = stringzDir.lastChild.text
                        if (literal != null && literal.startsWith("\"") && literal.endsWith("\"")) {
                            memorySize = literal.length - 1 // -2 for the quotes, +1 for the NUL byte
                        }

                        if (literal != null) {
                            for (i in 0 until memorySize) {
                                val char = if (i < memorySize - 1) literal[i + 1].toInt() else 0

                                infos.add(
                                    LineInfo(
                                        currentAddress!! + i,
                                        currentLabel,
                                        char,
                                        false,
                                        null,
                                        child
                                    )
                                )

                                currentLabel = null
                            }
                        }


                    }

                }

                is PsiLabel -> {
                    currentLabel = child.text
                }
                is LC3PsiInstruction -> {
                    memorySize = 1

                    val currentInstruction = processInstruction(child)

                    infos.add(LineInfo(
                        currentAddress!!,
                        currentLabel,
                        null,
                        currentInstruction == null,
                        currentInstruction,
                        child,
                        currentPragma
                    ))

                    currentLabel = null
                    currentPragma = null
                }
            }

        }

        return infos
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