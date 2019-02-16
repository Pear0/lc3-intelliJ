package cn.codetector.lc3.lc3intellij

import cn.codetector.lc3.lc3intellij.parser.LC3Parser
import cn.codetector.lc3.lc3intellij.psi.LC3File
import cn.codetector.lc3.lc3intellij.psi.LC3Types
import cn.codetector.lc3.lc3intellij.syntax.LC3LexerAdapter
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

class LC3ParserDefinition  : ParserDefinition{

    companion object {
        val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
        val COMMENTS = TokenSet.create(LC3Types.COMMENT)

        val FILE = IFileElementType(LC3Language.INSTANCE)
    }

    override fun createParser(project: Project?): PsiParser {
        return LC3Parser()
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        return LC3File(viewProvider)
    }

    override fun getStringLiteralElements(): TokenSet {
        return TokenSet.EMPTY
    }

    override fun getFileNodeType(): IFileElementType {
        return FILE
    }

    override fun createLexer(project: Project?): Lexer {
        return LC3LexerAdapter()
    }

    override fun createElement(node: ASTNode?): PsiElement {
        return LC3Types.Factory.createElement(node)
    }

    override fun getCommentTokens(): TokenSet {
        return COMMENTS
    }
}