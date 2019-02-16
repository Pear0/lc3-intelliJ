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
%eof{ return;
%eof}

LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
WhiteSpace     = {LineTerminator} | [ \t\f]
Comment        = ";" {InputCharacter}* {LineTerminator}?
CommentContent = ( [^*] | \*+ [^/*] )*

%state IN_ARGUMENT
%state IN_SEPERATOR
%state IN_COMMENT

%%
<YYINITIAL> {Comment} {yybegin(YYINITIAL); return LC3Types.COMMENT; }
[^] { return TokenType.BAD_CHARACTER; }