package cn.codetector.lc3.lc3intellij.ui

import cn.codetector.lc3.asm.VirtualLC3
import cn.codetector.lc3.lc3intellij.LC3AddressGutter
import cn.codetector.lc3.lc3intellij.filetype.LC3ASMFileType
import cn.codetector.lc3.lc3intellij.psi.SimpleLC3Tracer
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.table.JBTable
import java.awt.BorderLayout
import javax.swing.*
import javax.swing.table.*


class EditorSidebar(val editor: Editor) : CaretListener {

    val table: JBTable
    val textArea: JTextArea

    private var stateMap = HashMap<SimpleLC3Tracer.LineInfo, VirtualLC3>()

    init {
        val memAddress = LC3AddressGutter()

        editor.gutter.registerTextAnnotation(memAddress)

        val columnNames = arrayOf("First Name", "Last Name", "Sport", "# of Years", "Vegetarian")

        val data = arrayOf(
            arrayOf("Kathy", "Smith", "Snowboarding", 5, false),
            arrayOf("John", "Doe", "Rowing", 3, true),
            arrayOf("Sue", "Black", "Knitting", 2, false),
            arrayOf("Jane", "White", "Speed reading", 20, true),
            arrayOf("Joe", "Brown", "Pool", 10, false)
        )

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


        table = JBTable(model)

        val tableContainer = JPanel(BorderLayout())
        tableContainer.add(table, BorderLayout.CENTER)
        tableContainer.add(table.tableHeader, BorderLayout.NORTH)

        textArea = JTextArea("")

        val rightBorder = JPanel()
        rightBorder.layout = BoxLayout(rightBorder, BoxLayout.Y_AXIS)

        rightBorder.add(tableContainer)
        rightBorder.add(textArea)

        editor.component.add(rightBorder, BorderLayout.EAST)

        editor.caretModel.addCaretListener(this)


    }

    private fun updateSidebar(position: LogicalPosition) {
        for ((info, lc3) in stateMap) {
            if (info.psi != null) {
                val psiLoc = editor.offsetToLogicalPosition(info.psi.textOffset)
                if (psiLoc.line == position.line) {

                    textArea.text = lc3.dumpString()
                    displayFunctionPrologue(lc3)
                }
            }
        }
    }

    override fun caretPositionChanged(e: CaretEvent) {
        updateSidebar(e.newPosition)
    }

    fun setText(text: String) {
        textArea.text = text
    }

    fun updateStateMap(map: HashMap<SimpleLC3Tracer.LineInfo, VirtualLC3>) {
        stateMap = map
        updateSidebar(editor.caretModel.logicalPosition)
    }

    fun displayFunctionPrologue(lc3: VirtualLC3) {
        val expectedValues = arrayOf("arg0", "RV", "SAVED_PC", "INITIAL_BP", "data")

        table.model = object : AbstractTableModel() {
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
                            regs.add("SP")
                        }

                        if (!lc3.registers[6].magic && lc3.registers[6].value == loc) {
                            regs.add("BP")
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

    }

    fun dispose() {

    }

}


private class CustomEditorFactoryListener(private val handler: LC3EditorHandler) : EditorFactoryListener {
    override fun editorCreated(event: EditorFactoryEvent) {
        val editor = event.editor

        val vFile = FileDocumentManager.getInstance().getFile(editor.document) ?: return

        if (vFile.fileType !is LC3ASMFileType) {
            return
        }

        val sidebar = EditorSidebar(editor)

        handler.editorSidebars.add(sidebar)


//        val vFile = FileDocumentManager.getInstance().getFile(editor.document) ?: return
//
//        var documentChangeTracker = changeTrackers.get(vFile)
//        if (documentChangeTracker == null) {
//            documentChangeTracker = DocumentChangeTracker(editor.document)
//            changeTrackers.put(vFile, documentChangeTracker)
//        }
//        documentChangeTracker!!.getEditors().add(editor)
//
//        for (review in RevuUtils.getActiveReviewsForCurrentUser(project)) {
//            val issues = review.getIssues(vFile)
//
//            for (issue in issues) {
//                addMarker(editor, issue, false)
//            }
//        }
    }

    override fun editorReleased(event: EditorFactoryEvent) {
        val editor = event.editor

        handler.editorSidebars.filter { it.editor == editor }.forEach { it.dispose() }
        handler.editorSidebars.removeIf { it.editor == editor }

//        val editor = event.editor
//
//        val vFile = FileDocumentManager.getInstance().getFile(editor.document) ?: return
//
//        val documentChangeTracker = changeTrackers.get(vFile)
//        if (documentChangeTracker != null) {
//            val editors = documentChangeTracker!!.getEditors()
//            editors.remove(editor)
//            if (editors.isEmpty()) {
//                changeTrackers.remove(vFile)
//                documentChangeTracker!!.release()
//            }
//        }
//
//        renderers.remove(editor)
//        highlighters.remove(editor)
    }
}

class LC3EditorHandler : ProjectComponent {
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