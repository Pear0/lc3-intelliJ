package cn.codetector.lc3.lc3intellij

import cn.codetector.lc3.asm.Instruction
import cn.codetector.lc3.asm.VirtualLC3
import cn.codetector.lc3.lc3intellij.psi.*
import cn.codetector.lc3.lc3intellij.ui.LC3EditorHandler
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import java.util.*
import java.util.prefs.BackingStoreException
import kotlin.collections.HashMap

class LC3Annotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element is PsiFile) {
            parseFile(element, holder)
        }

        // holder.createInfoAnnotation(element, element.toString())

        if (element is PsiLabelReference) {
            if (element.resolve() == null) {
                holder.createErrorAnnotation(element, "Cannot resolve label")
            }
        }

        if (element is LC3PsiImmediate) {
            val value = element.getIntegerValue()
            if (value != null) {
                val instruction = element.parentOfType<LC3PsiInstruction>()?.getInstruction()

                if (instruction?.immediate != null && value !in instruction.immediate!!.range) {
                    val range = instruction.immediate!!.range
                    holder.createErrorAnnotation(element, "$value not in range [${range.first}, ${range.last}]")
                }
            }
        }

        if (element is PsiComment) {
            if (LC3Pragma.isPragma(element)) {
                val pragma = LC3Pragma.getPragma(element)

                if (pragma == "function prologue") {
                    holder.createWarningAnnotation(element, "I'm a pragma! = $pragma")

                    simulateExecution(element, holder)

                } else {
                    holder.createErrorAnnotation(element, "Invalid pragma: $pragma")
                }
            }
        }

    }

    data class ForwardEdge(val from: Int, val target: Int, var block: BasicBlock? = null)

    data class BasicBlock(
        val start: Int,
        val lines: ArrayList<SimpleLC3Tracer.LineInfo>,
        val edges: List<ForwardEdge>,
        val invalid: Boolean = false
    )

    private fun parseFile(file: PsiFile, holder: AnnotationHolder) {
        println("Re-evaluating psi file at ${System.currentTimeMillis()}")


        val infos = SimpleLC3Tracer.parseFile(file)
        val instLookup = HashMap<Int, SimpleLC3Tracer.LineInfo>()

        for (child in infos) {
            instLookup[child.address] = child
            // println(child)
        }

        val blockStarts = ArrayList<Int>()
        val branches = HashMap<Int, Int>() // branches from -> to

        val regs = arrayOfNulls<Int?>(8)
        var lastInstruction = -5

        for (info in infos) {
            if (info.instruction != null) {
                if (info.address - lastInstruction != 1) {
                    blockStarts.add(info.address)
                }
                lastInstruction = info.address

                if (info.label != null) {
                    blockStarts.add(info.address)
                }

                if (info.instruction.instruction == Instruction.BR) {
                    val target = info.address + 1 + (info.instruction.arg1 as VirtualLC3.Value).value
                    blockStarts.add(target)
                    branches[info.address] = target

                    if ((info.instruction.arg0 as VirtualLC3.Conditional).conditional) {
                        blockStarts.add(info.address + 1)
                    }

                }
            }
        }

        blockStarts.sort() // will usually have duplicates

        val blocks = ArrayList<BasicBlock>()
        val blockAddressMap = HashMap<Int, BasicBlock>()

        var lastStart = -5 // arbitrary

        for (i in blockStarts.indices) {
            val blockStart = blockStarts[i]
            if (blockStart <= lastStart) {
                continue
            }
            lastStart = blockStart

            var address = blockStart
            val nextBlockStart = blockStarts.firstOrNull { it > blockStart }

            val addresses = ArrayList<Int>()
            val lines = ArrayList<SimpleLC3Tracer.LineInfo>()
            val edges = ArrayList<ForwardEdge>()
            var isInvalid = false

            while (true) {
                if (address == nextBlockStart) {
                    edges.add(ForwardEdge(address - 1, address))
                    break
                }

                val info = instLookup[address] ?: break
                if (info.invalid || info.instruction == null) {
                    isInvalid = info.invalid
                    break
                }

                addresses.add(address)
                lines.add(info)

                val branch = branches[address]
                if (branch != null) {
                    edges.add(ForwardEdge(address, branch))
                }

                if (info.instruction.instruction == Instruction.BR && !(info.instruction.arg0 as VirtualLC3.Conditional).conditional) {
                    break
                }

                if (info.instruction.instruction == Instruction.RET || info.instruction.instruction == Instruction.RTI) {
                    break
                }

                address += 1
            }

            val block = BasicBlock(blockStart, lines, edges, invalid = isInvalid)
            blocks.add(block)

            for (addr in addresses) {
                blockAddressMap[addr] = block
            }

        }

        val functionStartBlocks = ArrayList<BasicBlock>()
        val flowedInstructions = HashSet<Int>()

        for (block in blocks) {
            println(block)

            for (edge in block.edges) {
                edge.block = blockAddressMap[edge.target]
                if (edge.block != null && edge.target != edge.block!!.start) {
                    throw AssertionError("Cannot jump into middle of basic block")
                }
            }

            block.lines.firstOrNull()?.let { line ->
                if (line.pragma == "function prologue") {
                    functionStartBlocks.add(block)
                }
            }

            for (line in block.lines) {
                flowedInstructions.add(line.address)
                if (line.psi != null) {
                    holder.createInfoAnnotation(line.psi, "Block x${block.start.toString(16)}")
                }
            }
        }

        for (info in infos) {
            // if an instruction is not in any basic blocks at this point, it is probably unreachable

            if (info.address !in flowedInstructions && info.psi != null && !info.invalid && info.instruction != null) {
                holder.createWarningAnnotation(info.psi, "instruction only reachable by indirect jump")
            }
        }

        val stateMap = HashMap<SimpleLC3Tracer.LineInfo, VirtualLC3>()

        for (funcStart in functionStartBlocks) {
            val blockStartState = HashMap<BasicBlock, VirtualLC3>()
            blockStartState[funcStart] = VirtualLC3().apply { prepareFunction() }

            val functionBlocks = ArrayDeque<BasicBlock>()
            functionBlocks.add(funcStart)

            while (functionBlocks.isNotEmpty()) {
                val block = functionBlocks.poll() ?: break
                println("Processing block $block")
                val lc3 = blockStartState[block]!!.copy()

                val edgeMap = HashMap<Int, ForwardEdge>()
                for (edge in block.edges) {
                    edgeMap[edge.from] = edge
                }

                val skipInstructions = listOf<Instruction>(Instruction.BR, Instruction.RET, Instruction.RTI)

                for (line in block.lines) {
                    if (line.instruction!!.instruction == Instruction.RET) {
                        println("At the end of the function, the LC3 be like:")
                        lc3.dump()
                    }

                    if (line.instruction.instruction !in skipInstructions && !lc3.execute(line.instruction)) {
                        println("Failed at execution of ${line.instruction}")
                        lc3.dump()
                        return
                    }
                    if (line.psi != null) {
                        holder.createInfoAnnotation(line.psi, lc3.dumpString())
                    }

                    stateMap[line] = lc3.copy()

                    edgeMap[line.address]?.let { edge ->
                        val targetBlock = edge.block
                        if (targetBlock != null) {
                            val existingLc3 = blockStartState[targetBlock]

                            if (existingLc3 == null) {
                                blockStartState[targetBlock] = lc3
                                functionBlocks.add(targetBlock)
                            } else {
                                val merged = existingLc3.merge(lc3)
                                blockStartState[targetBlock] = merged

                                if (merged != existingLc3) {
                                    functionBlocks.add(targetBlock)
                                }
                            }
                        }
                    }
                }

                println("Finished block $block")
            }
        }

        LC3EditorHandler.instance.forFile(file.virtualFile!!) {
            updateStateMap(stateMap)
        }

    }

    private fun simulateExecution(startElement: PsiElement, holder: AnnotationHolder) {
        println("\n".repeat(10))

//        val lc3 = VirtualLC3()
//        lc3.prepareFunction()
//
//        var elem: PsiElement? = startElement
//        var keepGoing = true
//        while (keepGoing && elem != null) {
//            if (elem is LC3PsiInstruction) {
//                val inst = SimpleLC3Tracer.processInstruction(elem)
//                keepGoing = inst != null && lc3.execute(inst)
//            }
//            elem = elem.nextSibling
//        }
//
//        val vf = startElement.containingFile.virtualFile
//        LC3EditorHandler.instance.forFile(vf) {
//            displayFunctionPrologue(lc3)
//        }

    }


}