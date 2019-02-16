package cn.codetector.lc3.lc3intellij.lang;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
%%

%{
public LC3ASMLexer() {
    this((java.io.Reader) null);
    init();
  }
%}

%public
%class LC3ASMLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType

%state IN_ARGUMENT
%state IN_SEPERATOR
%state IN_COMMENT

LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
WhiteSpace     = {LineTerminator} | [ \t\f]
Comment        = ";" {InputCharacter}* {LineTerminator}?
CommentContent = ( [^*] | \*+ [^/*] )*

%%

<YYINITIAL> "JMP" {System.out.println("JMP!");}