# Flow Analysis

This page describes at a high level how this 
plugin analyzes code to symbolically trace execution.


## Setup

First, the full assembly file is analyzed to find all jump targets. This flow analysis 
does not currently support indirect jumps. Each jump target is used to split the assembly
into basic blocks. Basic blocks are a commonly used in compilers, and they represent a 
block of code that always runs together.

An important concept is that these basic blocks combined with jumps/branches form 
a directed graph. This fact is used in the next section.

## Function Analysis

For every function labeled with a `; pragma function prologue`, a virtual LC-3 is created
and initialized with symbolic values for the registers and stack. The basic blocks for 
this function are traversed in a depth first search until a basic block that ends with a 
`RET` is found.

Because this a directed graph and not a tree, sometimes a basic block will have multiple 
parents. When this happens, the virtual LC-3 states are merged and the graph is traversed
again. This is done in a way that reaches a steady state after a few iterations because 
LC-3 is a fairly simple ISA.

Because the code is not actually executed, it is guaranteed to converge at 
the cost of knowing exactly what the state of the LC-3 is. This is usually fine because
the most important registers are R5 and R6 because they are used to keep track of the 
stack. R5 and R6 are usually not modified very much in hand-written assembly and are 
not branch dependent.

Finally, at each `RET`, the LC-3 state is compared to the expected result following the 
LC-3 calling convention.
