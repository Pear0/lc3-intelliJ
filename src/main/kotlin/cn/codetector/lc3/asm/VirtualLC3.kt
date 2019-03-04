package cn.codetector.lc3.asm

import org.fest.swing.util.Arrays
import java.lang.IllegalArgumentException

class VirtualLC3 {
    companion object {
        val SAVED_PC = InternalValue(0, true, "SAVED_PC")
        val INITIAL_BP = InternalValue(0, true, "INITIAL_BP")
        val INITIAL_SP = InternalValue(0x100, false, "INITIAL_SP")
        val MERGE_CONFLICT = InternalValue(0, true, "???")
    }

    data class InternalValue(val value: Int, val magic: Boolean = false, val name: String = "")

    interface InstArg

    data class Value(val value: Int) : InstArg

    enum class Register : InstArg {
        R0, R1, R2, R3, R4, R5, R6, R7, PC;
    }

    data class Conditional(val n: Boolean, val z: Boolean, val p: Boolean) : InstArg {
        companion object {
            val UNCONDITIONAL = Conditional(true, true, true)
        }

        val conditional = !n || !z || !p

        init {
            if (!n && !z && !p) {
                throw IllegalArgumentException("Conditional with no flags set is invalid")
            }
        }

    }

    val registers = Array(9) { InternalValue(0, true, if (it == 8) "PC" else "R$it") }
    val stack = arrayOfNulls<InternalValue>(0x200)

    init {
        clear()
    }

    fun clear() {
        for (i in registers.indices) {
            registers[i] = InternalValue(0, true, if (i == 8) "PC" else "R$i")
        }

        for (i in stack.indices) {
            stack[i] = null
        }
    }

    fun copy(): VirtualLC3 {
        val new = VirtualLC3()
        for (i in registers.indices) {
            new.registers[i] = registers[i]
        }
        for (i in stack.indices) {
            new.stack[i] = stack[i]
        }
        return new
    }

    private fun mergeValues(a: InternalValue?, b: InternalValue?) = if (a == b) a else MERGE_CONFLICT

    fun merge(that: VirtualLC3): VirtualLC3 {
        val merged = VirtualLC3()
        for (i in registers.indices) {
            merged.registers[i] = mergeValues(registers[i], that.registers[i])!!
        }
        for (i in stack.indices) {
            merged.stack[i] = mergeValues(stack[i], that.stack[i])
        }
        return merged
    }

    fun prepareFunction() {
        clear()

        registers[7] = SAVED_PC
        registers[6] = INITIAL_BP
        registers[5] = INITIAL_SP
    }

    private fun wrap16(num: Int): Int {
        var wrapped = num
        while (wrapped > 32767) {
            wrapped -= 65536
        }

        while (wrapped < -32768) {
            wrapped += 65536
        }

        return wrapped
    }

    fun execute(instruction: Instruction, arg0: InstArg?, arg1: InstArg?, arg2: InstArg?): Boolean {
        return when (instruction) {
            Instruction.ADD -> {
                assert(arg0 is Register)
                assert(arg1 is Register)

                val src1 = registers[(arg1 as Register).ordinal]
                val newValue = if (arg2 is Register) {
                    val src2 = registers[arg2.ordinal]

                    val magic = src1.magic || src2.magic
                    val value = if (magic) 0 else wrap16(src1.value + src2.value)

                    InternalValue(value, magic)
                } else {
                    val src2 = arg2 as Value

                    val value = if (src1.magic) 0 else wrap16(src1.value + src2.value)
                    InternalValue(value, src1.magic)
                }

                registers[(arg0 as Register).ordinal] = newValue
                true
            }
            Instruction.AND -> {
                assert(arg0 is Register)
                assert(arg1 is Register)

                val src1 = registers[(arg1 as Register).ordinal]
                val newValue = if (arg2 is Register) {
                    val src2 = registers[arg2.ordinal]

                    val antiMagic = (!src1.magic && src1.value == 0) || (!src2.magic && src2.value == 0)

                    if (antiMagic) {
                        InternalValue(0, false)
                    } else {
                        val magic = src1.magic || src2.magic
                        val value = if (magic) 0 else src1.value and src2.value

                        InternalValue(value, magic)
                    }
                } else {
                    val src2 = arg2 as Value

                    if (src2.value == 0) {
                        InternalValue(0, false)
                    } else {
                        val value = if (src1.magic) 0 else src1.value and src2.value
                        InternalValue(value, src1.magic)
                    }
                }

                registers[(arg0 as Register).ordinal] = newValue
                true
            }
            Instruction.BR -> false
            Instruction.JMP -> false
            Instruction.JSR -> false
            Instruction.JSRR -> false
            Instruction.LD -> false // TODO
            Instruction.LDI -> false // TODO
            Instruction.LDR -> false // TODO
            Instruction.LEA -> false // TODO
            Instruction.NOT -> false // TODO
            Instruction.RET -> false // TODO
            Instruction.RTI -> false
            Instruction.ST -> false // TODO
            Instruction.STI -> false // TODO
            Instruction.STR -> {
                arg0 as Register
                arg1 as Register
                arg2 as Value

                if (registers[arg1.ordinal].magic) {
                    false
                } else {
                    val index = registers[arg1.ordinal].value + arg2.value
                    stack[index] = registers[arg0.ordinal]
                    true
                }
            }
            Instruction.TRAP -> false
        }
    }

    fun execute(inst: FullInstruction): Boolean {
        return execute(inst.instruction, inst.arg0, inst.arg1, inst.arg2)
    }

    fun dumpString(): String {
        val builder = StringBuilder("==== LC3 State ====\n")

        builder.append("Registers:\n")
        for (reg in Register.values()) {
            builder.append("    ${reg.name}: ${registers[reg.ordinal]}\n")
        }

        builder.append("Stack:\n")

        for (i in stack.indices.reversed()) {
            val entry = stack[i] ?: continue
            builder.append("    ${i.toString(16).padStart(4, '0')}: $entry\n")
        }

        return builder.toString()
    }

    fun dump() {

        println("==== LC3 State ====")
        println("Registers:")
        for (reg in Register.values()) {
            println("    ${reg.name}: ${registers[reg.ordinal]}")
        }

        println("Stack:")

        for (i in stack.indices.reversed()) {
            val entry = stack[i] ?: continue
            println("    ${i.toString(16).padStart(4, '0')}: $entry")
        }
    }

    private fun areEqual(a: Array<out InternalValue?>, b: Array<out InternalValue?>): Boolean {
        if (a.size != b.size) {
            return false
        }

        for (i in a.indices) {
            if (a[i] != b[i]) {
                return false
            }
        }

        return true
    }

    override fun equals(other: Any?): Boolean {
        val that = other as? VirtualLC3 ?: return false
        return areEqual(registers, that.registers) && areEqual(stack, that.stack)
    }
}