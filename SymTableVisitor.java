/***
* Excerpted from "The Definitive ANTLR 4 Reference",
* published by The Pragmatic Bookshelf.
* Copyrights apply to this code. It may not be used to create training material,
* courses, books, articles, and the like. Contact us if you are in doubt.
* We make no guarantees that this code is fit for any purpose.
* Visit http://www.pragmaticprogrammer.com/titles/tpantlr2 for more book information.
***/
import java.util.ArrayList;

public class SymTableVisitor extends MiniJavaBaseVisitor<Void> {
	SymbolTable symTab = new SymbolTable("Classes","");
	DebugMsg debug = new DebugMsg(true);
	ErrorMsg error = new ErrorMsg(true);
	int classID;
	int methodID;

	/* mainClass: 'class' ID '{' 'public' 'static' 'void' 'main'
	'(' 'String' '[' ']' ID ')' '{' varDecl* statement* '}' '}' */
	@Override
	public Void visitMainClass(MiniJavaParser.MainClassContext ctx) {
		// ctx.ID(0).getText() returns the actual first ID as a String
		// ctx.ID(1).getText() returns the actual second ID as a String
		if (!symTab.addClass(ctx.ID(0).getText()))//add class to main symtable
		error.report("could not add class "+ctx.ID(0).getText());
		else{
			classID = symTab.addSymbolTable(ctx.ID(0).getText(),"class"); //new symtable for class
			symTab.getSymbolTable(classID).addMethod("main", "String"); //add main method to symtable
			methodID = symTab.getSymbolTable(classID).addSymbolTable("main","method"); //new symtable for main method
		}
		if (!symTab.getSymbolTable(classID).getSymbolTable(methodID).addParam(ctx.ID(1).getText(), "String")) //add params to symtable for method
		error.report("could not add parameter "+ctx.ID(1).getText());
		visitChildren(ctx);
		return null;
	}
	/* classDecl: 'class' ID '{' varDecl* methodDecl* '}' */
	@Override
	public Void visitClassDecl(MiniJavaParser.ClassDeclContext ctx) {
		// ctx.ID().getText() returns the actual ID as a String
		if (!symTab.addClass(ctx.ID().getText()))//add class to main symtable
		error.report("could not add class "+ctx.ID().getText());
		else
		classID = symTab.addSymbolTable(ctx.ID().getText(), "class"); //new symtable for class
		visitChildren(ctx);
		return null;
	}
	/* varDecl: type ID ';' */
	@Override
	public Void visitVarDecl(MiniJavaParser.VarDeclContext ctx) {
		// ctx.ID().getText() returns the actual ID as a String
		// ctx.type().t is the String returned by parsing type
		visit(ctx.type());
		if(symTab.getSymbolTable(classID).getSymbolTable(methodID).inSymbolTable(ctx.ID().getText())){
			error.report("\nERROR : local already defined within scope : "+ctx.ID().getText());
			return null;
		}
		if(symTab.getSymbolTable(classID).inSymbolTable(ctx.ID().getText())){
		error.report("\nERROR : local already defined within scope : "+ctx.ID().getText());
			return null;
		}
		if (!symTab.getSymbolTable(classID).getSymbolTable(methodID).addLocal(ctx.ID().getText(), ctx.type().t)) //add variables to symtable for method
		error.report("could not add local var "+ctx.ID().getText());
		return null;
	}
	/*'public' type ID '('  (type ID | type ID ',' (type ID)+)? ')'
	'{' varDecl* statement* 'return' expr ';' '}';*/
	@Override
	public Void visitMethodDecl(MiniJavaParser.MethodDeclContext ctx) {
		// ctx.ID().getText() returns the actual ID as a String
		// ctx.type().t is the String returned by parsing type
		visit(ctx.type(0));
		if (!symTab.getSymbolTable(classID).addMethod(ctx.ID(0).getText(), ctx.type(0).t))
	error.report("could not add method "+ctx.ID(0).getText());
		else
		methodID = symTab.getSymbolTable(classID).addSymbolTable(ctx.ID(0).getText(),"method");  //new symtable for method
		int pos = 1;
		while(ctx.type(pos) != null){
			visit(ctx.type(pos));
			if(symTab.getSymbolTable(classID).getSymbolTable(methodID).inSymbolTable(ctx.ID(pos).getText())){
				error.report("\nERROR : param already defined within scope : "+ctx.ID(pos).getText());
				visitChildren(ctx);
				return null;
			}
			if (!symTab.getSymbolTable(classID).getSymbolTable(methodID).addParam(ctx.ID(pos).getText(), ctx.type(pos).t))  //add params to symtable for method
				error.report("could not add parameter "+ctx.ID(pos).getText());
				pos++;
		}
		visitChildren(ctx);
		return null;
	}
	/*type ID ';'*/
	@Override
	public Void visitFieldDecl (MiniJavaParser.FieldDeclContext ctx) {
		// ctx.ID().getText() returns the actual ID as a String
		// ctx.type().t is the String returned by parsing type
		visit(ctx.type());
		if(symTab.getSymbolTable(classID).inSymbolTable(ctx.ID().getText())){
			error.report("\nERROR : field already defined within scope : "+ctx.ID().getText());
			return null;
		}
		if (!symTab.getSymbolTable(classID).addField(ctx.ID().getText(), ctx.type().t))  //add fields to symtable for class
		error.report("could not add field "+ctx.ID().getText());
		return null;
	}
	/* 'int' '[' ']' #intArrayType
	| 'boolean' #booleanType
	| 'int' #intType
	| ID #classType;
	*/
	@Override
	public Void visitIntType(MiniJavaParser.IntTypeContext ctx) {
		// ctx.t is the String returned to return from parsing type
		ctx.t="int";
		return null;
	}
	@Override
	public Void visitBooleanType(MiniJavaParser.BooleanTypeContext ctx) {
		// ctx.t is the String returned to return from parsing type
		ctx.t="boolean";
		return null;
	}
	@Override
	public Void visitIntArrayType(MiniJavaParser.IntArrayTypeContext ctx) {
		// ctx.t is the String returned to return from parsing type
		ctx.t="int []";
		return null;
	}
	@Override
	public Void visitClassType(MiniJavaParser.ClassTypeContext ctx) {
		// ctx.t is the String returned to return from parsing type
		ctx.t=ctx.ID().getText();
		return null;
	}
}
