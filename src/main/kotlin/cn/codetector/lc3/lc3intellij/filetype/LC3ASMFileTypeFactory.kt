package cn.codetector.lc3.lc3intellij.filetype

import cn.codetector.lc3.lc3intellij.LC3_ASM_EXTENSION
import com.intellij.openapi.fileTypes.FileTypeConsumer
import com.intellij.openapi.fileTypes.FileTypeFactory

class LC3ASMFileTypeFactory : FileTypeFactory(){
    override fun createFileTypes(consumer: FileTypeConsumer) {
        consumer.consume(LC3ASMFileType, LC3_ASM_EXTENSION)
    }
}