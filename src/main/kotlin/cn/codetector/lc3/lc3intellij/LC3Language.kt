package cn.codetector.lc3.lc3intellij

import com.intellij.lang.Language

class LC3Language : Language("LC3ASM", "text/$LC3_ASM_EXTENSION") {

    companion object {
        val INSTANCE = LC3Language()
    }

    override fun isCaseSensitive(): Boolean {
        return true
    }
}