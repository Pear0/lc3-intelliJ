package cn.codetector.lc3.asm


class VirtualLC3 {
    companion object {
        val SAVED_PC = InternalValue(0, true, "SAVED_PC")
        val INITIAL_BP = InternalValue(0, true, "I_FP")
        val INITIAL_SP = InternalValue(0x1000, false, "I_SP")
        val MERGE_CONFLICT = InternalValue(0, true, "???")
        val UNKNOWN = InternalValue(0, true, "???")

        val DEFAULT_MEMORY = object : Memory {
            override fun getValue(address: Int) = UNKNOWN
        }

        private fun prototypeRegs() = Array(9) { InternalValue(0, true, if (it == 8) "PC" else "I_R$it") }
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

    interface Memory {
        fun getValue(address: Int): InternalValue?
    }

    fun Memory.resolveValue(address: Int) = getValue(address) ?: UNKNOWN

    val registers = prototypeRegs()
    val stack = arrayOfNulls<InternalValue>(0x2000)

    init {
        clear()
    }

    fun setPC(address: Int?) {
        if (address != null) {
            registers[Register.PC.ordinal] = InternalValue(address, false)
        } else {
            registers[Register.PC.ordinal] = InternalValue(0, true, "PC")
        }
    }

    fun clear() {
        val pristineRegs = prototypeRegs()
        for (i in registers.indices) {
            registers[i] = pristineRegs[i]
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
        registers[6] = INITIAL_SP
        registers[5] = INITIAL_BP
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

    fun execute(mem: Memory, instruction: Instruction, arg0: InstArg?, arg1: InstArg?, arg2: InstArg?): Boolean {
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
            Instruction.BR -> false // handled externally
            Instruction.JMP -> false  // handled externally
            Instruction.JSR -> {

                registers[6] = if (registers[6].magic) registers[6] else InternalValue(registers[6].value - 1, false)

                true
            }
            Instruction.JSRR -> {

                registers[6] = if (registers[6].magic) registers[6] else InternalValue(registers[6].value - 1, false)

                true
            }
            Instruction.LD -> {
                arg0 as Register
                arg1 as Value

                val pc = registers[Register.PC.ordinal]
                if (pc.magic) {
                    registers[arg0.ordinal] = UNKNOWN
                } else {
                    val address = pc.value + 1 + arg1.value
                    registers[arg0.ordinal] = mem.resolveValue(address)
                }

                true
            }
            Instruction.LDI -> {
                arg0 as Register
                arg1 as Value

                val pc = registers[Register.PC.ordinal]
                if (pc.magic) {
                    registers[arg0.ordinal] = UNKNOWN
                } else {
                    val address = pc.value + 1 + arg1.value

                    val indirectAddress = mem.resolveValue(address)
                    if (indirectAddress.magic) {
                        registers[arg0.ordinal] = UNKNOWN
                    } else {
                        registers[arg0.ordinal] = mem.resolveValue(indirectAddress.value)
                    }
                }

                true
            }
            Instruction.LDR -> {
                arg0 as Register
                arg1 as Register
                arg2 as Value

                if (registers[arg1.ordinal].magic) {
                    false
                } else {
                    val index = registers[arg1.ordinal].value + arg2.value
                    registers[arg0.ordinal] = stack[index] ?: UNKNOWN
                    true
                }
            }
            Instruction.LEA -> {
                arg0 as Register
                arg1 as Value

                val pc = registers[Register.PC.ordinal]
                if (pc.magic) {
                    registers[arg0.ordinal] = UNKNOWN
                } else {
                    val address = pc.value + 1 + arg1.value
                    registers[arg0.ordinal] = InternalValue(address, false)
                }

                true
            }
            Instruction.NOT -> {
                arg0 as Register
                arg1 as Register

                if (registers[arg1.ordinal].magic) {
                    registers[arg0.ordinal] = registers[arg1.ordinal]
                } else {
                    val newValue = registers[arg1.ordinal].value.inv()

                    registers[arg0.ordinal] = InternalValue(newValue, false)
                }

                true
            }
            Instruction.RET -> false  // handled externally
            Instruction.RTI -> false  // handled externally
            Instruction.ST -> true // TODO
            Instruction.STI -> true // TODO
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
            Instruction.TRAP -> false  // handled externally
        }
    }

    fun execute(mem: Memory?, inst: FullInstruction): Boolean {
        return execute(mem ?: DEFAULT_MEMORY, inst.instruction, inst.arg0, inst.arg1, inst.arg2)
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

    fun verifyFunctionEpilogue(): List<String> {
        val pristineRegs = prototypeRegs()
        pristineRegs[7] = SAVED_PC
        pristineRegs[6] = INITIAL_SP
        pristineRegs[5] = INITIAL_BP

        val errors = ArrayList<String>()

        for (i in arrayOf(0, 1, 2, 3, 4, 5, 7)) {
            if (registers[i] != pristineRegs[i]) {
                errors.add("${pristineRegs[i].name} not preserved")
            }
        }

        if (registers[6].magic || registers[6].value != INITIAL_SP.value - 1) {
            errors.add("SP should be one less than INITIAL_SP")
        }

        return errors
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