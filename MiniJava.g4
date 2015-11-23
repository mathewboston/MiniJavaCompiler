/*
Mathew Boston Minijava
Rev 4.0
 */

grammar MiniJava;

prog : mainClass classDecl*;

mainClass : 'class' ID '{' 'public' 'static' 'void' 'main' '(' 'String'
            '[' ']' ID ')' '{' varDecl* statement* '}' '}' ;

classDecl : 'class' ID '{' fieldDecl* methodDecl* '}' ;

fieldDecl : type ID ';' ;

varDecl : type ID ';' ;

methodDecl : 'public' type ID '('  (type ID | type ID (',' (type ID))+)? ')' methodBody ;

methodBody returns [String t] : '{' varDecl* statement* 'return' expr ';' '}';

type returns [String t] : 'int' '[' ']' 	#intArrayType
     | 'boolean'				#booleanType
     | 'int' 					#intType
     | ID 					#classType;

statement : '{' statement* '}' 					#blockStat
          | 'if' '(' expr ')' statement 'else' statement 	#ifStat
          | 'while' '(' expr ')' statement 			#whileStat
          | 'System.out.println' '(' expr ')' ';' 		#printStat
          | ID '=' expr ';' 					#assignStat
          | ID '[' expr ']' '=' expr ';' 			#assignArrayStat;

expr returns [String t] : expr '[' expr']' 	   	#arrayExpr
     | expr '.' ID '(' ( expr ( ',' expr )* )? ')' 	#methodCallExpr
     | ( '+' | '-' ) expr 				#uniExpr
     | '!' expr 					#notExpr
     | expr '*' expr 					#multExpr
     | expr ('+'|'-') expr 				#plusMinusExpr
     | expr '<' expr 					#lessThanExpr
     | expr '&&' expr 					#andExpr
     | atom 						#atomExpr;

atom returns [String t] : INT		#intExpr
     | ID 				#idExpr
     | 'new' ID '(' ')' 		#newExpr
     | '(' expr ')'			#parenthesizedExpr
     | atom '.' 'length'		#lengthExpr
     | 'new' 'int' '['atom']' 		#newArrayExpr
     | 'true' 				#trueExpr
     | 'false'				#falseExpr
     | 'this'				#thisExpr;

ID :   [a-zA-Z_][a-zA-Z0-9_]* ;
INT     :   '0'..'9'+ ;
WS      :   [ \t\r\n]+ -> skip ;
COMMENT :   '/*' .*? '*/' -> skip ;
LINE_COMMENT  :   '//' .*? '\r'? '\n' -> skip ;
