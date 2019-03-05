package cn.codetector.lc3.lc3intellij.lang;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import cn.codetector.lc3.lc3intellij.psi.LC3Types;
%%

%public
%class LC3ASMLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%caseless
%eof{ return;
%eof}

LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
WhiteSpace     = [ \t\f]*
WhiteSpaceReq  = [ \t\f]+
Comment        = ";" {InputCharacter}*?
CommentContent = ( [^*] | \*+ [^/*] )*
LABEL = [a-zA-Z]([a-zA-Z0-9]{0,19})

SIMPLE_SYMBOL={VALID_CHAR}({VALID_CHAR}|[\d\!])*
VALID_CHAR=[a-zA-Z_\U0000A0-\U10ffff]

STRING_UNICODE=\\((u{HEXDIGIT}{4})|(x{HEXDIGIT}{2}))
CHAR_LITERAL='([^\\\'\x00-\x1F\x7F]|\\[^\x00-\x1F\x7F]+)'
STRING_ESCAPE=\\[^]

STRING=\"[^\"]*\"

DIGIT=[\d_]
NUM_PART=\d({DIGIT}*\d)?

NUM_SUFFIX=-?{DIGIT}+
P_SUFFIX=[pP]{NUM_SUFFIX}
E_SUFFIX=[eE]{NUM_SUFFIX}
F_SUFFIX=[fF]{NUM_SUFFIX}
HEXDIGIT=[a-fA-F0-9]
HEX_NUM=[xX]{HEXDIGIT}+({P_SUFFIX}|{E_SUFFIX}|{F_SUFFIX})?
OCT_NUM=[oO][0-7]+
BIN_NUM=[bB][01]+
DEC_NUM=[#]?[-]?{NUM_PART}
INTEGER={HEX_NUM}|{OCT_NUM}|{BIN_NUM}|{DEC_NUM}

REGISTER=[Rr][0-7]

%state IN_DIRECTIVE
%state IN_INSTRUCTION
%state IN_INSTRUCTION_WAIT_ARG
%%


<YYINITIAL> "." {yybegin(IN_DIRECTIVE); return LC3Types.DIRECTIVE_MARKER;}
<IN_DIRECTIVE> "orig" {yybegin(YYINITIAL); return LC3Types.ORIG_SYM;}
<IN_DIRECTIVE> "fill" {yybegin(YYINITIAL); return LC3Types.FILL_SYM;}
<IN_DIRECTIVE> "blkw" {yybegin(YYINITIAL); return LC3Types.BLKW_SYM;}
<IN_DIRECTIVE> "stringz" {yybegin(YYINITIAL); return LC3Types.STRINGZ_SYM;}
<IN_DIRECTIVE> "end" {yybegin(YYINITIAL); return LC3Types.END_SYM;}
<YYINITIAL> {INTEGER} {yybegin(YYINITIAL); return LC3Types.INTEGER; }
<YYINITIAL> {STRING} {yybegin(YYINITIAL); return LC3Types.STRING;}

<YYINITIAL> "add" {yybegin(IN_INSTRUCTION); return LC3Types.ADD_OP;}
<YYINITIAL> "and" {yybegin(IN_INSTRUCTION); return LC3Types.AND_OP;}
<YYINITIAL> "br"[nzpNZP]?[zpZP]?[pP]? {yybegin(IN_INSTRUCTION); return LC3Types.BR_OP;}
<YYINITIAL> "jmp" {yybegin(IN_INSTRUCTION); return LC3Types.JMP_OP;}
<YYINITIAL> "jsr" {yybegin(IN_INSTRUCTION); return LC3Types.JSR_OP;}
<YYINITIAL> "jsrr" {yybegin(IN_INSTRUCTION); return LC3Types.JSRR_OP;}
<YYINITIAL> "ld" {yybegin(IN_INSTRUCTION); return LC3Types.LD_OP;}
<YYINITIAL> "ldi" {yybegin(IN_INSTRUCTION); return LC3Types.LDI_OP;}
<YYINITIAL> "ldr" {yybegin(IN_INSTRUCTION); return LC3Types.LDR_OP;}
<YYINITIAL> "lea" {yybegin(IN_INSTRUCTION); return LC3Types.LEA_OP;}
<YYINITIAL> "not" {yybegin(IN_INSTRUCTION); return LC3Types.NOT_OP;}
<YYINITIAL> "ret" {yybegin(IN_INSTRUCTION); return LC3Types.RET_OP;}
<YYINITIAL> "rti" {yybegin(IN_INSTRUCTION); return LC3Types.RTI_OP;}
<YYINITIAL> "st" {yybegin(IN_INSTRUCTION); return LC3Types.ST_OP;}
<YYINITIAL> "sti" {yybegin(IN_INSTRUCTION); return LC3Types.STI_OP;}
<YYINITIAL> "str" {yybegin(IN_INSTRUCTION); return LC3Types.STR_OP;}
<YYINITIAL> "getc" {yybegin(IN_INSTRUCTION); return LC3Types.GETC_OP;}
<YYINITIAL> "out" {yybegin(IN_INSTRUCTION); return LC3Types.OUT_OP;}
<YYINITIAL> "puts" {yybegin(IN_INSTRUCTION); return LC3Types.PUTS_OP;}
<YYINITIAL> "in" {yybegin(IN_INSTRUCTION); return LC3Types.IN_OP;}
<YYINITIAL> "halt" {yybegin(IN_INSTRUCTION); return LC3Types.HALT_OP;}
<IN_INSTRUCTION> {WhiteSpace} "," {WhiteSpace} {yybegin(IN_INSTRUCTION); return LC3Types.ARG_ARG_SEPARATOR;}
<IN_INSTRUCTION> {REGISTER} {yybegin(IN_INSTRUCTION); return LC3Types.REGISTER;}
<IN_INSTRUCTION> {INTEGER} {yybegin(IN_INSTRUCTION); return LC3Types.INTEGER; }
<IN_INSTRUCTION> {LABEL} {yybegin(IN_INSTRUCTION); return LC3Types.LABEL;}


{LineTerminator} {yybegin(YYINITIAL); return LC3Types.CRLF; }
{WhiteSpace} {return TokenType.WHITE_SPACE;}
{Comment} {yybegin(YYINITIAL); return LC3Types.COMMENT;}
<YYINITIAL> {LABEL} {yybegin(YYINITIAL); return LC3Types.LABEL;}
[^] { return TokenType.BAD_CHARACTER; }