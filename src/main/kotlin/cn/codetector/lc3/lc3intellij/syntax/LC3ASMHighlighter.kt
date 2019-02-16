package cn.codetector.lc3.lc3intellij.syntax

import cn.codetector.lc3.lc3intellij.lang.LC3ASMLexer
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.psi.tree.IElementType

object LC3ASMHighlighter : SyntaxHighlighter{
    override fun getTokenHighlights(tokenType: IElementType?): Array<TextAttributesKey> = when (tokenType) {
        else -> emptyArray()
    }

    override fun getHighlightingLexer(): Lexer {
        return LC3LexerAdapter()
    }
}