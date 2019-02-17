package cn.codetector.lc3.lc3intellij.psi

import cn.codetector.lc3.lc3intellij.LC3Language
import com.intellij.psi.tree.IElementType

class LC3TokenType(debugName: String) : IElementType(debugName, LC3Language.INSTANCE) {
    override fun toString(): String {
        return "${super.toString()}".toLowerCase().capitalize()
    }
}