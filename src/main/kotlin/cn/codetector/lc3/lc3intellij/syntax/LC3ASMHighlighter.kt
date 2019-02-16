package cn.codetector.lc3.lc3intellij.syntax

import cn.codetector.lc3.lc3intellij.lang.LC3ASMLexer
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.tree.IElementType

object LC3ASMHighlighter : SyntaxHighlighter{
    override fun getTokenHighlights(tokenType: IElementType?): Array<TextAttributesKey> = when (tokenType) {
        else -> emptyArray()
    }

    override fun getHighlightingLexer(): Lexer {
        return LC3LexerAdapter()
    }
}

class LC3ASMHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter = LC3ASMHighlighter
}