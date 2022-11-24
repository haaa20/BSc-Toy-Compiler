grammar SimpleLang;

prog: dec+ EOF;

dec: type IDFR '(' vardec ')' body;

vardec: (type IDFR (',' type IDFR)*)?;

body: '{' (type IDFR ':=' exp ';')* ene '}';

block: '{' ene '}';

ene: exp (';' exp)*;

exp
    : IDFR      #Identifier
    | INTLIT    #AnInt
    | BOOLLIT   #ABool
    | IDFR ':=' exp #Assignment
    | '(' exp binop exp ')' #Operation
    | IDFR '(' args ')' #FuncCall
    | block #CodeBlock
    | 'if' exp 'then' block 'else' block #Conditional
    | 'while' exp 'do' block #WhileLoop
    | 'repeat' block 'until' exp    #UntilLoop
    | 'print' exp   #Print
    | 'newline' #NewLine // not sure about these chief...
    | 'space'   #Space
    | 'skip'    #Skip
;

args: (exp (',' exp)*)?;

type: INTTYPE| BOOLTYPE| UNITTYPE;

binop: COMPOP| INTOP| BOOLOP;

COMPOP
    : '=='
    | '<'
    | '>'
    | '<='
    | '>='
;
INTOP
    : '+'
    | '-'
    | '*'
    | '/'
    | '^'
;
BOOLOP
    : '&'
    | '|'
;
INTTYPE: 'int';
BOOLTYPE: 'bool';
UNITTYPE: 'unit';
IDFR: [a-z]+([a-z]|[A-Z]|'_')*;
INTLIT: '0'|[1-9][0-9]*;
BOOLLIT: 'true'|'false';
WS     : [ \n\r\t]+ -> skip ;