package cn.codetector.lc3.lc3intellij.psi

import cn.codetector.lc3.lc3intellij.LC3Language
import cn.codetector.lc3.lc3intellij.filetype.LC3ASMFileType
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

class LC3File(viewProvider: FileViewProvider): PsiFileBase(viewProvider, LC3Language.INSTANCE) {
    override fun getFileType(): FileType {
        return LC3ASMFileType
    }



    override fun toString(): String {
        return "LC3 Assembly File"
    }
}