# IntelliJ LC-3 Plugin

## Install

This plugin is still in development. The latest version can be found in [GitHub Releases](https://github.com/Pear0/lc3-intelliJ/releases).


## Features

### Probably Dead Code Analysis 

![Dead Code Analysis](images/dead-code.png)

### Calling Convention Checking (see below)

![Calling Convention](images/calling-convention.png)

### Stack / Register Tracing (see below)

![Tracing Video](images/tracing.mp4)

### Syntax Highlighting

![Address Gutter](images/syntax-highlighting.png)

### Memory Address Gutter

![Address Gutter](images/memory-addresses.png)

### Label Completion

![Label Completion](images/label-completion.png)

### Range Checks

![Range Checks](images/range-checks.png)


## Code Tracing Analysis

A pragma can be used to enable symbolic execution analysis for a function.
This is necessary for calling convention verification and the tracing sidebar 
to work. 

![Function Pragma](images/pragma-function.png)

The symbolic execution analysis is detailed more [here](Flow%20Analysis.md).


## Credits

The idea and original plugin are thanks to [Codetector](https://github.com/Codetector1374).

Contributors:
* [Will Gulian](https://github.com/Pear0)
* [Codetector](https://github.com/Codetector1374)
