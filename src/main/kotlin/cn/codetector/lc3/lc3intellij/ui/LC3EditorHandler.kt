package cn.codetector.lc3.lc3intellij.ui

import cn.codetector.lc3.asm.VirtualLC3
import cn.codetector.lc3.lc3intellij.LC3AddressGutter
import cn.codetector.lc3.lc3intellij.LC3Annotator
import cn.codetector.lc3.lc3intellij.filetype.LC3ASMFileType
import cn.codetector.lc3.lc3intellij.psi.SimpleLC3Tracer
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.AbstractProjectComponent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.table.JBTable
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.util.*
import javax.swing.JPanel
import javax.swing.JTextPane
import javax.swing.table.AbstractTableModel
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants
import kotlin.collections.HashMap


class EditorSidebar(val editor: Editor) : CaretListener {

    private val rightBorder: JPanel
    private val stackTable: JBTable
    private val registerTable: JBTable
    private val functionName: JTextPane
    private val textArea: JTextPane

    private var stateMap = HashMap<SimpleLC3Tracer.LineInfo, Pair<LC3Annotator.FuncStart, VirtualLC3>>()

    init {
        val memAddress = LC3AddressGutter()

        editor.gutter.registerTextAnnotation(memAddress)

        val model = object : AbstractTableModel() {
            override fun getRowCount(): Int {
                return 5
            }

            override fun getColumnCount(): Int {
                return 3
            }

            override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
                return "$rowIndex - $columnIndex"
            }

            override fun getColumnName(column: Int): String {
                return "Column $column"
            }
        }


        stackTable = JBTable(model)
        registerTable = JBTable(model)

        val stackTableContainer = JPanel(BorderLayout())
        stackTableContainer.add(stackTable, BorderLayout.CENTER)
        stackTableContainer.add(stackTable.tableHeader, BorderLayout.NORTH)

        val registerTableContainer = JPanel(BorderLayout())
        registerTableContainer.add(registerTable, BorderLayout.CENTER)
        registerTableContainer.add(registerTable.tableHeader, BorderLayout.NORTH)

        functionName = JTextPane()

        functionName.text = "text"

        val doc = functionName.styledDocument
        val center = SimpleAttributeSet()
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER)
        doc.setParagraphAttributes(0, doc.length, center, false)

        textArea = JTextPane()

        textArea.text = """
        I_R* are initial values of registers.
        They are callee saved and should be
        restored before returning.

        The first table is the stack I_SP is
        the initial stack pointer when the
        function gets called.

        Unlike the other registers, R5 (FP)
        and R6 (SP) are displayed relative
        to the initial stack pointer.

        When a value is unknown, it is
        represented by ???. There are many
        reasons a value may be unknown and
        it is not necessarily a problem
        with your code. However, if R5 or
        R6 is unknown, the symbolic
        execution engine will not be able
        to trace your code properly.

        FP = frame pointer
        SP = stack pointer

        """.trimIndent()


        rightBorder = JPanel()
        rightBorder.layout = GridBagLayout() // BoxLayout(rightBorder, BoxLayout.Y_AXIS)

        val myInsets = Insets(0, 4, 0, 0)

        rightBorder.add(functionName, GridBagConstraints().apply {
            gridx = 0
            gridy = 0
            weightx = 1.0
            insets = myInsets
        })
        rightBorder.add(stackTableContainer, GridBagConstraints().apply {
            gridx = 0
            gridy = 1
            weightx = 1.0
            fill = GridBagConstraints.HORIZONTAL
            insets = myInsets
        })
        rightBorder.add(registerTableContainer, GridBagConstraints().apply {
            gridx = 0
            gridy = 2
            weightx = 1.0
            fill = GridBagConstraints.HORIZONTAL
            insets = myInsets
        })
        rightBorder.add(textArea, GridBagConstraints().apply {
            gridx = 0
            gridy = 3
            weightx = 1.0
            weighty = 1.0
            fill = GridBagConstraints.BOTH
            insets = myInsets
        })

        editor.component.add(rightBorder, BorderLayout.EAST)

        editor.caretModel.addCaretListener(this)


    }

    private fun updateSidebar(position: LogicalPosition) {

        val lineLookup = HashMap<Int, Triple<SimpleLC3Tracer.LineInfo, LC3Annotator.FuncStart, VirtualLC3>>()

        for ((info, stuff) in stateMap) {
            val (func, lc3) = stuff

            if (info.psi != null) {
                val psiLoc = editor.offsetToLogicalPosition(info.psi.textOffset)
                lineLookup[psiLoc.line] = Triple(info, func, lc3)
            }
        }

        var line = position.line

        while (line >= 0) {
            val lineStart = editor.document.getLineStartOffset(line)
            val lineEnd = editor.document.getLineEndOffset(line)
            val lineText = editor.document.getText(TextRange(lineStart, lineEnd))

            val lineLength = lineText.takeWhile { it != ';' }.trim().length

            if (line in lineLookup) {
                val (info, func, lc3) = lineLookup[line]!!

                functionName.text = "Function: ${func.label}"

                val doc = functionName.styledDocument
                val center = SimpleAttributeSet()
                StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER)
                doc.setParagraphAttributes(0, doc.length, center, false)

                displayLc3State(lc3)
                rightBorder.isVisible = true
                return
            }

            if (lineLength > 0) {
                break
            }

            line -= 1
        }

        rightBorder.isVisible = false
    }

    override fun caretPositionChanged(e: CaretEvent) {
        updateSidebar(e.newPosition)
    }

    fun setText(text: String) {
        textArea.text = text
    }

    fun updateStateMap(map: HashMap<SimpleLC3Tracer.LineInfo, Pair<LC3Annotator.FuncStart, VirtualLC3>>) {
        stateMap = map
        updateSidebar(editor.caretModel.logicalPosition)
    }

    fun displayLc3State(lc3: VirtualLC3) {
        val expectedValues = arrayOf("arg0", "RV", "SAVED_PC", "I_FP", "data")

        stackTable.model = object : AbstractTableModel() {
            override fun getRowCount(): Int {
                return 10
            }

            override fun getColumnCount(): Int {
                return 3
            }

            override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
                return when (columnIndex) {
                    0 -> {
                        val loc = VirtualLC3.INITIAL_SP.value - rowIndex

                        val regs = ArrayList<String>()

                        if (VirtualLC3.INITIAL_SP.value == loc) {
                            regs.add(VirtualLC3.INITIAL_SP.name)
                        }

                        if (!lc3.registers[5].magic && lc3.registers[5].value == loc) {
                            regs.add("FP")
                        }

                        if (!lc3.registers[6].magic && lc3.registers[6].value == loc) {
                            regs.add("SP")
                        }

                        // location
                        regs.joinToString(" / ")
                    }
                    1 -> {
                        val loc = VirtualLC3.INITIAL_SP.value - rowIndex
                        val value = lc3.stack[loc]

                        if (value == null) {
                            ""
                        } else if (value.magic) {
                            if (value.name != "") value.name else "???"
                        } else {
                            value.value.toString()
                        }
                    }
                    2 -> expectedValues.getOrNull(rowIndex) ?: ""
                    else -> ""
                }
            }

            override fun getColumnName(column: Int): String {
                return when (column) {
                    0 -> "Location"
                    1 -> "Value"
                    2 -> "Expected"
                    else -> "???"
                }
            }
        }

        registerTable.model = object : AbstractTableModel() {
            override fun getRowCount(): Int {
                return 8
            }

            override fun getColumnCount(): Int {
                return 2
            }

            override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
                return when (columnIndex) {
                    0 -> "R$rowIndex"
                    1 -> {
                        val value = lc3.registers[rowIndex]

                        if (!value.magic) {
                            if (rowIndex == 5 || rowIndex == 6) {
                                val diff = value.value - VirtualLC3.INITIAL_SP.value
                                when {
                                    diff == 0 -> "ISP"
                                    diff > 0 -> "ISP + $diff"
                                    diff < 0 -> "ISP - ${-diff}"
                                    else -> "???"
                                }
                            } else {
                                "${value.value}"
                            }
                        } else if (value.name.isNotBlank()) {
                            value.name
                        } else {
                            "???"
                        }
                    }
                    else -> ""
                }
            }

            override fun getColumnName(column: Int): String {
                return when (column) {
                    0 -> "Register"
                    1 -> "Value"
                    else -> "???"
                }
            }
        }

    }

    fun dispose() {

    }

}


private class CustomEditorFactoryListener(private val handler: LC3EditorHandler) : EditorFactoryListener {
    override fun editorCreated(event: EditorFactoryEvent) {
        val editor = event.editor

        if (editor.project != handler.project) {
            return
        }

        val vFile = FileDocumentManager.getInstance().getFile(editor.document) ?: return

        if (vFile.fileType !is LC3ASMFileType) {
            return
        }

        val sidebar = EditorSidebar(editor)

        handler.editorSidebars.add(sidebar)

    }

    override fun editorReleased(event: EditorFactoryEvent) {
        val editor = event.editor

        handler.editorSidebars.filter { it.editor == editor }.forEach { it.dispose() }
        handler.editorSidebars.removeIf { it.editor == editor }

    }
}

class LC3EditorHandler(val project: Project) : AbstractProjectComponent(project) {
    companion object {
        private var instance_: LC3EditorHandler? = null

        val instance: LC3EditorHandler
            get() = instance_!!
    }

    val editorSidebars = ArrayList<EditorSidebar>()

    private var disposable: Disposable? = null

    private val editorFactoryListener = CustomEditorFactoryListener(this)

    override fun initComponent() {
        instance_ = this
        println("LC3EditorHandler.initComponent()")
        disposable?.let { Disposer.dispose(it) }

        val disp = Disposer.newDisposable("LC3EditorHandler.disposable")
        disposable = disp

        EditorFactory.getInstance().addEditorFactoryListener(editorFactoryListener, disp)

    }

    override fun disposeComponent() {
        disposable?.let { Disposer.dispose(it) }
        disposable = null
        instance_ = null
    }

    fun forFile(vf: VirtualFile, f: EditorSidebar.() -> Unit) {
        for (sidebar in editorSidebars) {
            val vFile = FileDocumentManager.getInstance().getFile(sidebar.editor.document) ?: continue
            if (vFile == vf) {
                sidebar.f()
            }
        }
    }

}