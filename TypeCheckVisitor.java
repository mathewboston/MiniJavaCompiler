/***
 * Excerpted from "The Definitive ANTLR 4 Reference",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/tpantlr2 for more book information.
***/
import java.util.ArrayList;

public class TypeCheckVisitor extends MiniJavaBaseVisitor<Void> {
  SymbolTable symTab;
  DebugMsg debug = new DebugMsg(true);
  ErrorMsg error = new ErrorMsg(true);
  
  public void setSymbolTable(SymbolTable st){
    symTab = st;
  }
 /*
  mainClass
    : 'class' ID '{' 'public' 'static' 'void' 'main'
         '(' 'String' '[' ']' ID ')' '{' varDecl* statement* '}' '}'
    ;
  */ 
  @Override
  public Void visitMainClass(MiniJavaParser.MainClassContext ctx) {
    System.out.println("\nstarting type checking");
    symTab.setCurrentClass(ctx.ID(0).getText());
    visitChildren(ctx);
    return null;
  }
  /*
   classDecl
   : 'class' ID '{' fieldDecl* methodDecl* '}' 
   ;
   */
  @Override
  public Void visitClassDecl(MiniJavaParser.ClassDeclContext ctx) {
    symTab.setCurrentClass(ctx.ID().getText());
    visitChildren(ctx);
    return null;    
  }
  /*
   methodDecl
   : 'public' type ID '(' formalList ')' methodBody  
   ;
   */
  @Override
  public Void visitMethodDecl(MiniJavaParser.MethodDeclContext ctx) {
    visitChildren(ctx);
    if (!ctx.type(0).t.equals(ctx.methodBody().t))
      error.report("return expression type ("+ctx.methodBody().t+") does not match method return type ("+ctx.type(0).t+")");    
    return null;
  }  
  /*
   methodBody returns [String t]
     : '{' varDecl* statement* 'return' expr ';' '}'
     ;
   */  
  @Override public Void visitMethodBody(MiniJavaParser.MethodBodyContext ctx) { 
    visitChildren(ctx); 
    ctx.t = ctx.expr().t;
    return null;
  }
 /*
  |   'if' '(' expr ')' statement 'else' statement #ifStat
  */
  @Override
  public Void visitIfStat(MiniJavaParser.IfStatContext ctx) {
    visitChildren(ctx);
    if (!"boolean".equals(ctx.expr().t))
      error.report("condition in if statement must be boolean");
    return null;
  }
 /*
  | 'System.out.println' '(' expr ')' ';'        #printStat
  */
  @Override public Void visitPrintStat(MiniJavaParser.PrintStatContext ctx) {
    visitChildren(ctx); 
    if (!"int".equals(ctx.expr().t))
      error.report("System.out.println expects an integer parameter");
    return null;
  }
  /*
   | ID '=' expr ';'                              #assignStat  
   */
  @Override public Void visitAssignStat(MiniJavaParser.AssignStatContext ctx) { 
    visitChildren(ctx); 
    SymbolAttributes idSymbol=symTab.get(ctx.ID().getText());
    if (idSymbol==null)
      error.report("undefined symbol: "+ctx.ID().getText());
    else if (idSymbol.kind == SymbolTable.CLASS)
      error.report("Identifier cannot be a class");
    else if (idSymbol.kind == SymbolTable.METHOD)
      error.report("Identifier cannot be a Method");
    else if (!idSymbol.type.equals(ctx.expr().t))
      error.report(ctx,"Assignment types don't match: "+idSymbol.type+" and "+ctx.expr().t);
    return null;
  }  
  /* expr returns [String t]
      : atom                     #atomExpr
      | expr '.' ID '(' expr ')' #methodCallExpr 
      | expr op=('+'|'-') expr   #plusMinusExpr
      | expr '<' expr            #lessThanExpr
      ;
   */
  @Override public Void visitMethodCallExpr(MiniJavaParser.MethodCallExprContext ctx) {
    visitChildren(ctx);
    SymbolAttributes methodSymbol=symTab.get(ctx.ID().getText());
    if (methodSymbol == null)
      error.report("symbol not found: "+ctx.ID().getText());
    else if (methodSymbol.kind != SymbolTable.METHOD)
      error.report("symbol is not a method");
    // need to check parameter types
    if (methodSymbol == null)
      ctx.t = "";
    else 
      ctx.t=methodSymbol.type;
    return null;
  }
  @Override public Void visitLessThanExpr(MiniJavaParser.LessThanExprContext ctx) {
    visitChildren(ctx);
    if ("int".equals(ctx.expr(0).t) && "int".equals(ctx.expr(1).t))
      ctx.t="boolean"; 
    return null;
  } 
  @Override public Void visitPlusMinusExpr(MiniJavaParser.PlusMinusExprContext ctx) { 
    visitChildren(ctx);
    // although we don't need it here, we can check if it's a '+':
    // if (ctx.op.getType()==MiniJavaParser.ADD) ...
    if (!("int".equals(ctx.expr(0).t) && "int".equals(ctx.expr(1).t)))
      error.report("+ and - operands must be int type");
    ctx.t="int"; 
    return null;
  } 
  @Override public Void visitAtomExpr(MiniJavaParser.AtomExprContext ctx) { 
    visitChildren(ctx);
    ctx.t=ctx.atom().t;
    return null;
  }    
  /*
atom returns [String t]
    : INT              #intExpr
    | ID               #idExpr
    | 'this'           #thisExpr
    | 'new' ID '(' ')' #newExpr
    | '(' expr ')'     #parenthesizedExpr
    ;
   */
  @Override public Void visitIntExpr(MiniJavaParser.IntExprContext ctx) { 
    ctx.t="int";
    return null;
  }   
  @Override public Void visitIdExpr(MiniJavaParser.IdExprContext ctx) { 
    SymbolAttributes idSymbol=symTab.get(ctx.ID().getText());
    if (idSymbol==null){
      error.report("undefined symbol: "+ctx.ID().getText());
      ctx.t = "";
    }
    else
      ctx.t = idSymbol.type;
    return null;
  }
  @Override public Void visitThisExpr(MiniJavaParser.ThisExprContext ctx) { 
    ctx.t = symTab.getCurrentClass();
    return null;
  }
 /*
  | 'new' ID '(' ')' #newExpr
  */
  @Override public Void visitNewExpr(MiniJavaParser.NewExprContext ctx) { 
    SymbolAttributes idSymbol=symTab.get(ctx.ID().getText());
    ctx.t = "";
    if (idSymbol==null)
      error.report("undefined symbol: "+ctx.ID().getText());
    else if (idSymbol.kind != SymbolTable.CLASS)
      error.report("Class name expected");
    else
      ctx.t = idSymbol.symbolId;
    return null;
  }  
  @Override public Void visitParenthesizedExpr(MiniJavaParser.ParenthesizedExprContext ctx) {
    visitChildren(ctx);
    ctx.t = ctx.expr().t;
    return null;
  }

}
