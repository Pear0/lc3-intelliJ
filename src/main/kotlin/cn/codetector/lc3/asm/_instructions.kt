package cn.codetector.lc3.asm

private fun rangeBits(bitSize: Int) = (-(2 shl (bitSize - 1)))..((2 shl (bitSize - 1)) - 1)

enum class Immediate(val range: IntRange) {
    IMM5(rangeBits(5)),
    OFFSET6(rangeBits(6)),
    OFFSET9(rangeBits(9)),
    OFFSET11(rangeBits(11)),
    TRAPVECT8(0..255);
}


sealed class Instruction {
// https://justinmeiners.github.io/lc3-vm/supplies/lc3-isa.pdf

    object ADD : Instruction() {
        override val immediate: Immediate? = Immediate.IMM5
    }

    object AND : Instruction() {
        override val immediate: Immediate? = Immediate.IMM5
    }

    object BR : Instruction() {
        override val immediate: Immediate? = Immediate.OFFSET9
    }

    object JMP : Instruction()

    object JSR : Instruction() {
        override val immediate: Immediate? = Immediate.OFFSET11
    }

    object JSRR : Instruction()

    object LD : Instruction() {
        override val immediate: Immediate? = Immediate.OFFSET9
    }

    object LDI : Instruction() {
        override val immediate: Immediate? = Immediate.OFFSET9
    }

    object LDR : Instruction() {
        override val immediate: Immediate? = Immediate.OFFSET6
    }

    object LEA : Instruction() {
        override val immediate: Immediate? = Immediate.OFFSET9
    }

    object NOT : Instruction()

    object RET : Instruction()

    object RTI : Instruction()

    object ST : Instruction() {
        override val immediate: Immediate? = Immediate.OFFSET9
    }

    object STI : Instruction() {
        override val immediate: Immediate? = Immediate.OFFSET9
    }

    object STR : Instruction() {
        override val immediate: Immediate? = Immediate.OFFSET6
    }

    object TRAP : Instruction() {
        override val immediate: Immediate? = Immediate.TRAPVECT8
    }

    open val immediate: Immediate? = null
}
