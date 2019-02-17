package cn.codetector.lc3.lc3intellij.completion

import cn.codetector.lc3.lc3intellij.LC3Language
import cn.codetector.lc3.lc3intellij.psi.LC3ASMInstruction
import cn.codetector.lc3.lc3intellij.psi.LC3TokenType
import cn.codetector.lc3.lc3intellij.psi.LC3Types
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.TokenType
import com.intellij.psi.util.contextOfType
import com.intellij.util.ProcessingContext

val LC3_OpCodes = arrayListOf<LookupElement>(
    LookupElementBuilder.create("add"),
    LookupElementBuilder.create("and"),
    LookupElementBuilder.create("br"),
    LookupElementBuilder.create("brn"),
    LookupElementBuilder.create("brz"),
    LookupElementBuilder.create("brp"),
    LookupElementBuilder.create("brnz"),
    LookupElementBuilder.create("brnp"),
    LookupElementBuilder.create("brzp"),
    LookupElementBuilder.create("jmp"),
    LookupElementBuilder.create("jsr"),
    LookupElementBuilder.create("jsrr"),
    LookupElementBuilder.create("ld"),
    LookupElementBuilder.create("ldi"),
    LookupElementBuilder.create("ldr"),
    LookupElementBuilder.create("lea"),
    LookupElementBuilder.create("not"),
    LookupElementBuilder.create("ret"),
    LookupElementBuilder.create("rti"),
    LookupElementBuilder.create("st"),
    LookupElementBuilder.create("sti"),
    LookupElementBuilder.create("str"),
    LookupElementBuilder.create("getc"),
    LookupElementBuilder.create("out"),
    LookupElementBuilder.create("puts"),
    LookupElementBuilder.create("in"),
    LookupElementBuilder.create("halt")
)
class SimpleLC3Completion : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withLanguage(LC3Language.INSTANCE)
                .andOr(PlatformPatterns.psiElement().afterSibling(PlatformPatterns.psiElement(LC3Types.CRLF)),
                    PlatformPatterns.psiElement().afterSiblingSkipping(PlatformPatterns.psiElement(TokenType.WHITE_SPACE), PlatformPatterns.psiElement(LC3Types.LABEL))
            ), object: CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext?,
                    result: CompletionResultSet
                ) {
                    result.addAllElements(LC3_OpCodes)
                }
            }
        )
    }
}