grammar SimpleLang;

prog: dec+ EOF;

dec: type IDFR '(' vardec ')' body;

vardec: (type IDFR (',' type IDFR)*)?;

body: '{' (type IDFR ':=' exp ';')* ene '}';

block: '{' ene '}';

ene: exp (';' exp)*;

exp
    : INTLIT    #AnInt
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
    | IDFR      #Identifier
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
;
BOOLOP
    : '&'
    | '|'
    | '^'
;
INTTYPE: 'int';
BOOLTYPE: 'bool';
UNITTYPE: 'unit';
BOOLLIT: 'true'|'false';
IDFR: [a-z]([a-z]|[A-Z]|[0-9]|'_')*;
INTLIT: '0'|[1-9][0-9]*;
WS     : [ \n\r\t]+ -> skip ;