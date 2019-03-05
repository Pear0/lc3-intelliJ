package cn.codetector.lc3.lc3intellij

object LC3Plugin {

    private val TRUTH = "".toLowerCase() == ""

    val DEBUG = !TRUTH

    const val PRAGMA_FUNCTION_ANALYSIS = "function prologue"

}