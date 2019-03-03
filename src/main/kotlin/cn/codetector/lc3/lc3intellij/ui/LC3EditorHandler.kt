package cn.codetector.lc3.lc3intellij.ui

import cn.codetector.lc3.lc3intellij.LC3AddressGutter
import cn.codetector.lc3.lc3intellij.filetype.LC3ASMFileType
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vcs.actions.VcsAnnotateUtil.getEditors
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener


private class CustomEditorFactoryListener : EditorFactoryListener {
    override fun editorCreated(event: EditorFactoryEvent) {
        val editor = event.editor

        val vFile = FileDocumentManager.getInstance().getFile(editor.document) ?: return

        if (vFile.fileType !is LC3ASMFileType) {
            return
        }

        val memAddress = LC3AddressGutter()

        editor.gutter.registerTextAnnotation(memAddress)

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

    private var disposable: Disposable? = null

    private val editorFactoryListener = CustomEditorFactoryListener()

    override fun initComponent() {
        println("LC3EditorHandler.initComponent()")
        disposable?.let { Disposer.dispose(it) }

        val disp = Disposer.newDisposable("LC3EditorHandler.disposable")
        disposable = disp

        EditorFactory.getInstance().addEditorFactoryListener(editorFactoryListener, disp)
    }

    override fun disposeComponent() {
        disposable?.let { Disposer.dispose(it) }
        disposable = null
    }

}