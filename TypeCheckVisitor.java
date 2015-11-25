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
	public static final byte FIELD = 0, METHOD = 1, PARAM=2, LOCAL=3, CLASS=4;
	DebugMsg debug = new DebugMsg(true);
	ErrorMsg error = new ErrorMsg(true);
	int classID;
	int methodID;

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
		classID = 0;
		methodID = 0;
		visitChildren(ctx);
		System.out.println("type checking complete! "+error.errorCount+" error(s) \n");
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
		classID++;
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
		error.report(ctx,"return expression type ("+ctx.methodBody().t+") does not match method return type ("+ctx.type(0).t+")");
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
	'{' statement* '}' #blockStat
	*/
	@Override public Void visitBlockStat(MiniJavaParser.BlockStatContext ctx) {
		visitChildren(ctx);
		return null;
	}
	/*
	|   'if' '(' expr ')' statement 'else' statement #ifStat
	*/
	@Override
	public Void visitIfStat(MiniJavaParser.IfStatContext ctx) {
		visitChildren(ctx);
		if (!"boolean".equals(ctx.expr().t))
		error.report(ctx,"condition in if statement must be boolean");
		return null;
	}
	/*
	|   'while' '(' expr ')' statement #whileStat
	*/
	@Override
	public Void visitWhileStat(MiniJavaParser.WhileStatContext ctx) {
		visitChildren(ctx);
		if (!"boolean".equals(ctx.expr().t))
		error.report(ctx,"condition in if statement must be boolean");
		return null;
	}
	/*
	| 'System.out.println' '(' expr ')' ';'        #printStat
	*/
	@Override public Void visitPrintStat(MiniJavaParser.PrintStatContext ctx) {
		visitChildren(ctx);
		if (!"int".equals(ctx.expr().t))
		error.report(ctx,"System.out.println expects an integer parameter");
		return null;
	}
	/*
	| ID '=' expr ';'                              #assignStat
	*/
	@Override public Void visitAssignStat(MiniJavaParser.AssignStatContext ctx) {
		visitChildren(ctx);
		SymbolAttributes idSymbol=symTab.getSymbolTable(classID).getSymbolTable(methodID).get(ctx.ID().getText());
		if(idSymbol==null)
		idSymbol=symTab.getSymbolTable(classID).get(ctx.ID().getText());
		if(idSymbol==null){
			int n = 0;
			while(n<symTab.getSymbolTable(classID).getSymbolTableArrayLength()){
				idSymbol=symTab.getSymbolTable(classID).getSymbolTable(n).get(ctx.ID().getText());
				if(idSymbol != null) break;
				n++;
			}
		}
		if (idSymbol==null)
		error.report(ctx,"undefined symbol in assignment: "+ctx.ID().getText());
		else if (idSymbol.kind == SymbolTable.CLASS)
		error.report(ctx,"Identifier cannot be a class");
		else if (idSymbol.kind == SymbolTable.METHOD)
		error.report(ctx,"Identifier cannot be a Method");
		else if (!idSymbol.type.equals(ctx.expr().t))
		error.report(ctx,"Assignment types don't match: "+idSymbol.type+" and "+ctx.expr().t);
		return null;
	}

	/*
	|	ID '[' expr ']' '=' expr ';'                            #assignArrayStat

	Array is an int []. expr(1) must be of type int. expr(0) is array index, must
	be of type int. ID must be of type int []
	*/
	@Override public Void visitAssignArrayStat(MiniJavaParser.AssignArrayStatContext ctx) {
		visitChildren(ctx);
		SymbolAttributes idSymbol=symTab.getSymbolTable(classID).getSymbolTable(methodID).get(ctx.ID().getText());
		if(idSymbol==null)
		idSymbol=symTab.getSymbolTable(classID).get(ctx.ID().getText());
		if(idSymbol==null){
			int n = 0;
			while(n<symTab.getSymbolTable(classID).getSymbolTableArrayLength()){
				idSymbol=symTab.getSymbolTable(classID).getSymbolTable(n).get(ctx.ID().getText());
				if(idSymbol != null) break;
				n++;
			}
		}
		if (idSymbol==null)
		error.report(ctx,"undefined symbol in assignment: "+ctx.ID().getText());
		else if (idSymbol.kind == SymbolTable.CLASS)
		error.report(ctx,"Identifier cannot be a class");
		else if (idSymbol.kind == SymbolTable.METHOD)
		error.report(ctx,"Identifier cannot be a Method");
		else if (!"int []".equals(idSymbol.type))
		error.report(ctx,ctx.ID() + ": Symbol must be of type int []");
		else if (!"int".equals(ctx.expr(0).t))
		error.report(ctx,"Array index type don't match: int and "+ctx.expr(0).t);
		else if (!"int".equals(ctx.expr(1).t))
		error.report(ctx,"Assignment types don't match: int and "+ctx.expr(1).t);
		return null;
	}
	/*	expr returns [String t] : expr '[' expr ']'	   	          #arrayExpr
	| expr '.' ID '(' ( expr ( ',' expr )* )? ')' 	        #methodCallExpr
	| ( '+' | '-' ) expr 				                          #uniExpr
	| '!' expr 					                                  #notExpr
	| expr '*' expr 					                              #multExpr
	| expr ('+'|'-') expr 				                          #plusMinusExpr
	| expr '<' expr 					                              #lessThanExpr
	| expr '&&' expr 					                            #andExpr
	| atom 						                                    #atomExpr;
	*/
	@Override public Void visitMethodCallExpr(MiniJavaParser.MethodCallExprContext ctx) {
		visitChildren(ctx);
		int n = 0;
		SymbolAttributes methodSymbol = null;
		while(n<symTab.getSymbolTableArrayLength()){
			methodSymbol=symTab.getSymbolTable(n).get(ctx.ID().getText());
			if(methodSymbol != null) break;
			n++;
		}
		if (methodSymbol == null)
		error.report(ctx,"Method not found: "+ctx.ID().getText());
		else if (methodSymbol.kind != SymbolTable.METHOD)
		error.report(ctx,"symbol is not a method");
		if (methodSymbol == null)
		ctx.t = "";
		else
		ctx.t=methodSymbol.type;
		if(methodSymbol != null){ //check params and arguments match
			int i = 0;
			int argCount = 0;
			int argPos = 1;
			SymbolTable methodID = null;
			while(i<symTab.getSymbolTable(n).getSymbolTableArrayLength()){
				methodID = symTab.getSymbolTable(n).getSymbolTable(i);
				if(methodSymbol.symbolId.equals(methodID.getCurrentClass())) break;
				i++;
			}
			int paramCount = symTab.getSymbolTable(n).getSymbolTable(i).symbolTableTypeCounter(PARAM);
			ArrayList<SymbolAttributes> list = symTab.getSymbolTable(n).getSymbolTable(i).symbolTableType(PARAM);
			while(ctx.expr(argPos)!=null){
				boolean inParam = false;
				for(SymbolAttributes param: list){
					if(param.type.equals(ctx.expr(argPos).t))
					inParam = true;
				}
				if(!inParam)error.report(ctx, methodID.getCurrentClass()+" is not expecting paramater type "+ctx.expr(argPos).t);
				argPos++;
				argCount++;
			}
			if(argCount != paramCount)
			error.report(ctx, methodID.getCurrentClass()+" is expecting "+paramCount+" parameter(s) but sees "+argCount);
		}
		return null;
	}
	@Override public Void visitArrayExpr(MiniJavaParser.ArrayExprContext ctx) {
		visitChildren(ctx);
		if (!("int []".equals(ctx.expr(0).t)))
		error.report(ctx,"Array needs to be of type int");
		if (!( "int".equals(ctx.expr(1).t)))
		error.report(ctx,"Array index needs to be of type int");
		ctx.t="int";
		return null;
	}
	@Override public Void visitLessThanExpr(MiniJavaParser.LessThanExprContext ctx) {
		visitChildren(ctx);
		if (!("int".equals(ctx.expr(0).t) && "int".equals(ctx.expr(1).t)))
		error.report(ctx,"< needs to be of type int");
		ctx.t="boolean";
		return null;
	}
	@Override public Void visitAndExpr(MiniJavaParser.AndExprContext ctx) {
		visitChildren(ctx);
		if (!("boolean".equals(ctx.expr(0).t) && "boolean".equals(ctx.expr(1).t)))
		error.report(ctx,"&& needs to be of type boolean");
		ctx.t="boolean";
		return null;
	}
	@Override public Void visitMultExpr(MiniJavaParser.MultExprContext ctx) {
		visitChildren(ctx);
		if (!("int".equals(ctx.expr(0).t) && "int".equals(ctx.expr(1).t)))
		error.report(ctx,"* operands must be int type");
		ctx.t="int";
		return null;
	}
	@Override public Void visitPlusMinusExpr(MiniJavaParser.PlusMinusExprContext ctx) {
		visitChildren(ctx);
		if (!("int".equals(ctx.expr(0).t) && "int".equals(ctx.expr(1).t)))
		error.report(ctx,"+ and - operands must be int type");
		ctx.t="int";
		return null;
	}
	@Override public Void visitUniExpr(MiniJavaParser.UniExprContext ctx) {
		visitChildren(ctx);
		if (!("int".equals(ctx.expr().t)))
		error.report(ctx,"uni + and - operand must be int type");
		ctx.t="int";
		return null;
	}
	@Override public Void visitNotExpr(MiniJavaParser.NotExprContext ctx) {
		visitChildren(ctx);
		if (!("boolean".equals(ctx.expr().t)))
		error.report(ctx,"! operand must be boolean type");
		ctx.t="boolean";
		return null;
	}
	@Override public Void visitAtomExpr(MiniJavaParser.AtomExprContext ctx) {
		visitChildren(ctx);
		ctx.t=ctx.atom().t;
		return null;
	}
	/*
	atom returns [String t] : INT		                            #intExpr
	| ID 				                                          #idExpr
	| 'new' ID '(' ')' 		                                #newExpr
	| '(' expr ')'			                                    #parenthesizedExpr
	| atom '.' 'length'		                                #lengthExpr
	| 'new' 'int' '['atom']' 		                          #newArrayExpr
	| 'true' 				                                      #trueExpr
	| 'false'				                                      #falseExpr
	| 'this'			                                          #thisExpr;
	*/
	@Override public Void visitIntExpr(MiniJavaParser.IntExprContext ctx) {
		ctx.t="int";
		return null;
	}
	@Override public Void visitTrueExpr(MiniJavaParser.TrueExprContext ctx) {
		ctx.t="boolean";
		return null;
	}
	@Override public Void visitFalseExpr(MiniJavaParser.FalseExprContext ctx) {
		ctx.t="boolean";
		return null;
	}
	@Override public Void visitIdExpr(MiniJavaParser.IdExprContext ctx) {
		SymbolAttributes idSymbol=symTab.getSymbolTable(classID).getSymbolTable(methodID).get(ctx.ID().getText());
		if(idSymbol==null)
		idSymbol=symTab.getSymbolTable(classID).get(ctx.ID().getText());
		if(idSymbol==null){
			int n = 0;
			while(n<symTab.getSymbolTable(classID).getSymbolTableArrayLength()){
				idSymbol=symTab.getSymbolTable(classID).getSymbolTable(n).get(ctx.ID().getText());
				if(idSymbol != null) break;
				n++;
			}
		}
		if (idSymbol==null){
			error.report(ctx,"undefined symbol: "+ctx.ID().getText());
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
	@Override public Void visitNewExpr(MiniJavaParser.NewExprContext ctx) {
		SymbolAttributes idSymbol=symTab.get(ctx.ID().getText());
		ctx.t = "";
		if (idSymbol==null)
		error.report(ctx,"undefined symbol in new expression: "+ctx.ID().getText());
		else if (idSymbol.kind != SymbolTable.CLASS)
		error.report(ctx,"Class name expected in new expression");
		else
		ctx.t = idSymbol.symbolId;
		return null;
	}
	@Override public Void visitLengthExpr(MiniJavaParser.LengthExprContext ctx) {
		visitChildren(ctx);
		SymbolAttributes idSymbol=symTab.get(ctx.atom().t);
		ctx.t = "";
		if (idSymbol==null)
		error.report(ctx,"undefined symbol in .length expression: "+ctx.atom().t);
		else if (idSymbol.kind != SymbolTable.CLASS)
		error.report(ctx,"Class name expected in .length expression");
		else
		ctx.t = idSymbol.symbolId;
		return null;
	}
	@Override public Void visitNewArrayExpr(MiniJavaParser.NewArrayExprContext ctx) {
		visitChildren(ctx);
		if(!("int".equals(ctx.atom().t)))
		error.report(ctx,"Array index needs to be of type int");
		ctx.t = "int []";
		return null;
	}
	@Override public Void visitParenthesizedExpr(MiniJavaParser.ParenthesizedExprContext ctx) {
		visitChildren(ctx);
		ctx.t = ctx.expr().t;
		return null;
	}

}
