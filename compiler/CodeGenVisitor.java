/***
 * Excerpted from "The Definitive ANTLR 4 Reference",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material,
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose.
 * Visit http://www.pragmaticprogrammer.com/titles/tpantlr2 for more book information.
***/
import java.util.ArrayList;
import java.util.HashMap;

public class CodeGenVisitor extends MiniJavaBaseVisitor<String> {
  SymbolTable symTab; // the symbol table built by the symbol table visitor
  int reg = 0;        // a counter set at the last allocated llvm register

  // a hashmap from operator to the llvm instruction for it
  HashMap <String, String> opToInstr = new HashMap<String, String>();
  // a hashmap from minijava types to llvm types
  HashMap <String, String> llvmType = new HashMap<String, String>();
  final int intSizeBytes = 4; // how many bytes are needed to represent a java int in llvm
  // strings for llvm types corresponding to minijava types
  final String intStr = "i32";
  final String booleanStr = "i1";
  final String intArrayStr = "i32*";

  public void init(){
    // initialize the opToInstr hashmap
    opToInstr.put("+","add");
    opToInstr.put("-","sub");
    opToInstr.put("*","mul");
    opToInstr.put("<","icmp slt");
    // initialize the llvmType hashmap
    llvmType.put("int",intStr);
    llvmType.put("boolean", booleanStr);
    llvmType.put("int[]", intArrayStr);

    DebugMsg debug = new DebugMsg(true); // used to print debugging messages not needed in the final version
    ErrorMsg error = new ErrorMsg(true); // used to print error messages that will stay in the final version
  }

  public String getLlvmType(String type){
    String t = llvmType.get(type);
    if (t == null)
      t = "%$"+type+"*"; // llvm types used for user defined types, i.e. class names
    return t;
  }
  String currentClass;

  public void setSymbolTable(SymbolTable st){
    symTab = st;
    // symTab.printSymbolTable();
  }
  // getreg() returns the next unallocated register number. (Used for labels as well.)
  public int getreg(){
    return ++reg;
  }

  // defaultResult returns the default value returned by visitor methods.
  @Override
  public String defaultResult(){
    return "";
  }
  // aggregateResult aggregates the results of visiting multiple children of a node.
  @Override
  public String aggregateResult(String aggregate, String nextResult) {
    return aggregate+nextResult;
  }
/*
grammar MiniJava;

prog : mainClass classDecl*
 ;
 */
   @Override
   public String visitProg(MiniJavaParser.ProgContext ctx) {
     init();
     String code = "declare i32 @printf(i8*, ...)\n"+
       "declare noalias i8* @malloc(i64) nounwind\n"+
       "@.str = internal constant [4 x i8] c\"%d\\0A\\00\"\n";
     for (MiniJavaParser.ClassDeclContext c : ctx.classDecl()){
       code += visit(c);
     }
     code += visit(ctx.mainClass());
     return code;
   }
   /*

mainClass
: 'class' ID '{' 'public' 'static' 'void' 'main'
      '(' 'String' '[' ']' ID ')' '{' varDecl* statement* '}' '}'
; */
   @Override
   public String visitMainClass(MiniJavaParser.MainClassContext ctx) {
     currentClass = ctx.ID(0).getText();
     String code = "define i32 @main(i32 %argc, i8** %argv) {\n"+
       "    %main_1 = alloca i32\n"+
       "    %main_2 = alloca i32\n"+
       "    %main_3 = alloca i8**\n"+
       "    store i32 0, i32* %main_1\n"+
       "    store i32 %argc, i32* %main_2\n"+
       "    store i8** %argv, i8*** %main_3\n";
     for (MiniJavaParser.VarDeclContext c : ctx.varDecl())
       code += visit(c);
     for (MiniJavaParser.StatementContext c : ctx.statement())
       code += visit(c);
     code += "    ret i32 0\n}";
     return code;
   }
  /*
   classDecl
   : 'class' ID '{' fieldDecl* methodDecl* '}'
   ;
   */
   @Override
   public String visitClassDecl(MiniJavaParser.ClassDeclContext ctx) {
     currentClass = ctx.ID().getText();
     String fieldTypes = "";
     int n = ctx.fieldDecl().size();
     if (n>0)
       fieldTypes += visit(ctx.fieldDecl(0));
     for (int i=1; i<n; i++)
       fieldTypes += ", "+visit(ctx.fieldDecl(i));
     n = ctx.methodDecl().size();
     String methods = "";
     for (int i=0; i<n; i++)
       methods += visit(ctx.methodDecl(i));
     return "%$"+currentClass+" = type { "+fieldTypes+" }\n"+methods;
   }
  /*
   fieldDecl : type ID ';'
   ;
   */
   @Override
   public String visitFieldDecl(MiniJavaParser.FieldDeclContext ctx) {
     return getLlvmType(ctx.type().t); // needed for the class type definition line
   }
  /*
    varDecl : type ID ';'
   ;
   */
   @Override public String visitVarDecl(MiniJavaParser.VarDeclContext ctx) {
     return "    %"+ctx.ID().getText()+" = alloca "+getLlvmType(ctx.type().t)+"\n";
   }
   /*
    methodDecl
    : 'public' type ID '(' formalParam ')' methodBody
    ;
    */
   @Override public String visitMethodDecl(MiniJavaParser.MethodDeclContext ctx) {
     /* example for a method within class C
      * public int m (n){
      *   ...
      *   return 0
      * }
      define i32 @C$m(%$C* %this_arg, i32 %n_arg) {
      ; init arg(s): n
        %this = alloca %$C*
        store %$C* %this_arg, %$C** %this
        %n = alloca i32
        store i32 %n_arg, i32* %n
        ...
        ret i32 0
      }

      For Minijava, we don't include the "this" parameter
      */
     String defineLine = "define "+getLlvmType(ctx.type().t)+" @"+currentClass;
     defineLine += "$"+ctx.ID().getText()+"(";
     MiniJavaParser.FormalParamContext fparam = ctx.formalParam();
     String param = fparam.ID().getText();
     defineLine += getLlvmType(fparam.type().t)+" %"+param+"_arg) {\n";
     String initComment = "; init arg(s): "+param+"\n";

     String id = "%"+param;
     String type = getLlvmType(fparam.type().t);
     String initParams = "    "+id+" = alloca "+type+"\n";
     initParams += "    store "+type+" "+id+"_arg, "+type+"* "+id+"\n";

     String body = visit(ctx.methodBody());

     String returnStat = "    ret "+getLlvmType(ctx.type().getText())+" %r"+reg+"\n";

     return defineLine + initComment + initParams + body + returnStat + "}\n";
   }
   /*
    methodBody returns [String t]
    : '{' varDecl* statement* 'return' expr ';' '}'
    ;
    */
   // no need to override the visitMethodBody method
   /*
    formalParam
    : type ID
    ;
    */
   // no need to override the visitFormalParam method
   /*
    statement
    :  '{' statement* '}'                    #blockStat
    */
   // no need to everride the visitStatement method
   /*
    | 'if' '(' expr ')' statement 'else' statement #ifStat
   */
  @Override
  public String visitIfStat(MiniJavaParser.IfStatContext ctx) {
    String code = "; if statement\n";
    code += visit(ctx.expr());
    int r1=reg; // register where the expression result is stored
    int labelNumber = getreg();
    code += "    br i1 %r"+r1+", label %true"+labelNumber+", label %false"+labelNumber+"\n";
    code += "true"+labelNumber+":\n";
    code += visit(ctx.statement(0));
    code += "    br label %end"+labelNumber+"\n";
    code += "false"+labelNumber+":\n";
    code += visit(ctx.statement(1));
    code += "    br label %end"+labelNumber+"\n";
    code += "end"+labelNumber+":\n";
    return code;
  }

  /*
   | 'System.out.println' '(' expr ')' ';'        #printStat
   */
  @Override public String visitPrintStat(MiniJavaParser.PrintStatContext ctx) {
    String code = visit(ctx.expr());
    int r1 = reg;
    int r2 = getreg();
    code += "    %r"+r2+" = call i32 (i8*, ...)* @printf(i8* getelementptr inbounds ([4 x i8]* @.str, i32 0, i32 0), i32 %r"+r1+")\n";
    return code;
  }

  /*
   | ID '=' expr ';'                              #assignStat
   */
  @Override public String visitAssignStat(MiniJavaParser.AssignStatContext ctx) {
    String code = visit(ctx.expr());
    int r=reg;
    String id = ctx.ID().getText();
    SymbolAttributes symID = symTab.get(id);
    if (symID.kind == SymbolTable.FIELD){
      // to be implemented as field. Here implemented as local
      String type = getLlvmType(symID.type);
      code += "    store "+type+" %r"+r+", "+type+"* %"+id+"\n";
    }
    else {
      String type = getLlvmType(symID.type);
      code += "    store "+type+" %r"+r+", "+type+"* %"+id+"\n";
    }
    return code;
  }

  /*
   expr returns [String t]
   : atom                     #atomExpr
   */
  // no need to override the visitAtomExpr method
  /*
   | expr '.' ID '(' expr ')' #methodCallExpr
   */
  @Override
  public String visitMethodCallExpr(MiniJavaParser.MethodCallExprContext ctx) {
    // will need to include code to evaluate expr(0) to generate an object.
    String classType = ctx.expr(0).t;
    String code = visit(ctx.expr(1));
    int r = reg;
    String methodId = ctx.ID().getText();
    SymbolAttributes methodSymbol = symTab.get(methodId);
    String returnType = getLlvmType(methodSymbol.type);
    String paramType = getLlvmType(ctx.expr(1).t);
    //String params = "    %r"+getreg()+" = call "+returnType+" @"+classType+"$"+classSymbol.symbolId;
    String param = "("+paramType+" %r"+r+")";
    code += "    %r"+getreg()+" = call "+returnType+" @"+classType+"$"+methodId+param+"\n";
    return code;
  }
  /*
    | expr '<' expr            #lessThanExpr
   */
 @Override public String visitLessThanExpr(MiniJavaParser.LessThanExprContext ctx) {
   String code = visit(ctx.expr(0));
   int r1 = reg;
   code += visit(ctx.expr(1));
   int r2 = reg;
   code += "    %r"+getreg()+" = "+opToInstr.get("<")+" "+intStr+" %r"+r1+", %r"+r2+"\n";
   return code;
 }
  /*
   | expr op=('+'|'-') expr
   */
 @Override public String visitPlusMinusExpr(MiniJavaParser.PlusMinusExprContext ctx) {
   String code = visit(ctx.expr(0));
   int r1 = reg;
   code += visit(ctx.expr(1));
   int r2 = reg;
   code += "    %r"+getreg()+" = "+opToInstr.get(ctx.op.getText())+" "+intStr+" %r"+r1+", %r"+r2+"\n";
   return code;
 }
 /*
  atom returns [String t]
  : INT              #intExpr
  */
  @Override
  public String visitIntExpr(MiniJavaParser.IntExprContext ctx) {
    return "    %r"+getreg()+" = add nsw "+intStr+" "+ctx.INT().getText()+", 0\n";
  }
  /*
   | ID               #idExpr
   */
  @Override
  public String visitIdExpr(MiniJavaParser.IdExprContext ctx) {
    String code;
    SymbolAttributes symID = symTab.get(ctx.ID().getText());
    if (symID.kind == SymbolTable.FIELD){
      // need to implement loading a field.
      // Here just treating as a local.
      code = "    %r"+getreg()+" = load "+getLlvmType(symID.type)+"* %"+ctx.ID().getText()+"\n";
    }
    else {
      code = "    %r"+getreg()+" = load "+getLlvmType(symID.type)+"* %"+ctx.ID().getText()+"\n";
    }
    return code;
  }
  /*
   | 'this'           #thisExpr
   */
  // no need to override the visitThisExpr method
  /*
   *     | 'new' ID '(' ')' #newExpr
   */
  @Override
  public String visitNewExpr(MiniJavaParser.NewExprContext ctx) {
    String id = ctx.ID().getText();
    int r1 = getreg();
    int r2 = getreg();
    String type = getLlvmType(id);
    String code = "    %r"+r1+"= call i8* @malloc(i64 8)\n";
    code += "    %r"+r2+" = bitcast i8* %r"+r1+" to "+type+"\n";
    return code;
  }
  /*
    | '(' expr ')'     #parenthesizedExpr
    ;
   */
  // no need to override the visitParenthesizedExpr method
}
