package cn.codetector.lc3.lc3intellij.filetype

import cn.codetector.lc3.lc3intellij.LC3Language
import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

object LC3ASMFileType : LanguageFileType(LC3Language.INSTANCE) {
    override fun getIcon(): Icon? {
        return null
    }

    override fun getName(): String {
        return "LC3 Assembly File"
    }

    override fun getDefaultExtension(): String {
        return ".asm"
    }

    override fun getDescription(): String {
        return "LC3 Assembly File"
    }
}