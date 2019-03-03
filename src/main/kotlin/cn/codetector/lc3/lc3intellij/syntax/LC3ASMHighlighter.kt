package cn.codetector.lc3.lc3intellij.syntax

import cn.codetector.lc3.lc3intellij.lang.LC3ASMLexer
import cn.codetector.lc3.lc3intellij.psi.LC3Types
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.tree.IElementType

object LC3ASMHighlighter : SyntaxHighlighter {
    val INSTRUCTION_OP_CODE = arrayOf(createTextAttributesKey("LC3_INSTRUCTION_OP_CODE", DefaultLanguageHighlighterColors.KEYWORD))
    val REGISTER = arrayOf(createTextAttributesKey("LC3_REGISTER", DefaultLanguageHighlighterColors.MARKUP_ATTRIBUTE))
    val COMMENT = arrayOf(createTextAttributesKey("LC3_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT))
    val NUMBER = arrayOf(createTextAttributesKey("LC3_NUMBER", DefaultLanguageHighlighterColors.NUMBER))
    val STRING = arrayOf(createTextAttributesKey("LC3_STRING", DefaultLanguageHighlighterColors.STRING))
    val NAMED_LABEL = arrayOf(createTextAttributesKey("LC3_LABEL", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION))
    val REF_LABEL = arrayOf(createTextAttributesKey("LC3_LABEL", DefaultLanguageHighlighterColors.FUNCTION_CALL))
    val DIRECTIVE = arrayOf(createTextAttributesKey("LC3_DIRECTIVE", DefaultLanguageHighlighterColors.CONSTANT))
    override fun getTokenHighlights(tokenType: IElementType?): Array<TextAttributesKey> = when (tokenType) {
        LC3Types.ADD_OP,
        LC3Types.AND_OP,
        LC3Types.BR_OP,
        LC3Types.JMP_OP,
        LC3Types.JSR_OP,
        LC3Types.JSRR_OP,
        LC3Types.LD_OP,
        LC3Types.LDI_OP,
        LC3Types.LDR_OP,
        LC3Types.LEA_OP,
        LC3Types.NOT_OP,
        LC3Types.RET_OP,
        LC3Types.RTI_OP,
        LC3Types.ST_OP,
        LC3Types.STI_OP,
        LC3Types.STR_OP,
        LC3Types.GETC_OP,
        LC3Types.OUT_OP,
        LC3Types.PUTS_OP,
        LC3Types.IN_OP,
        LC3Types.HALT_OP -> INSTRUCTION_OP_CODE

        LC3Types.COMMENT -> COMMENT
        LC3Types.REGISTER -> REGISTER
        LC3Types.INTEGER -> NUMBER
        LC3Types.STRING -> STRING
        LC3Types.NAMED_LABEL -> NAMED_LABEL
        LC3Types.REF_LABEL -> REF_LABEL

        LC3Types.ORIG_SYM, LC3Types.FILL_SYM, LC3Types.BLKW_SYM,
        LC3Types.STRINGZ_SYM, LC3Types.END_SYM -> DIRECTIVE
        else -> emptyArray()
    }

    override fun getHighlightingLexer(): Lexer {
        return LC3LexerAdapter()
    }
}

class LC3ASMHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter = LC3ASMHighlighter
}