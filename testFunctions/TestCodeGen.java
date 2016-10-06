import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.io.*;

public class TestCodeGen {

    public static void main(String[] args) throws Exception {
        String inputFile = null;
        if ( args.length>0 ) inputFile = args[0];
        else inputFile = "sumtorialMini.java";
        System.out.println("intput file: "+inputFile);
        InputStream is = System.in;
        if ( inputFile!=null ) {
            is = new FileInputStream(inputFile);
        }
        ANTLRInputStream input = new ANTLRInputStream(is);
        MiniJavaLexer lexer = new MiniJavaLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MiniJavaParser parser = new MiniJavaParser(tokens);
        parser.setBuildParseTree(true);      // tell:ANTLR to build a parse tree
        ParseTree tree = parser.prog(); // parse
        int nerrors=parser.getNumberOfSyntaxErrors();
        // show tree in text form
        // System.out.println(indentString(tree.toStringTree(parser)));

        if (nerrors==0){
          SymTableVisitor symTableVisitor = new SymTableVisitor();
          symTableVisitor.visit(tree);
          //symTableVisitor.symTab.printSymbolTable();

          if (!symTableVisitor.error.anyErrors){
            TypeCheckVisitor typeCheckVisitor = new TypeCheckVisitor();
            typeCheckVisitor.setSymbolTable(symTableVisitor.symTab);
            typeCheckVisitor.visit(tree);

            if (!typeCheckVisitor.error.anyErrors){
              CodeGenVisitor codeGenVisitor = new CodeGenVisitor();
              codeGenVisitor.setSymbolTable(symTableVisitor.symTab);
              String output = codeGenVisitor.visit(tree);
              // System.out.println("generated code:\n"+output);
              // output code to file
              File outputFile = new File("code.ll");
              FileWriter fWriter = new FileWriter(outputFile, false); // false to overwrite.
              fWriter.write(output);
              fWriter.close();
              System.out.println("ended");
            }
          }
        }
    }
    public static String indentString(String s){
      return s;
    }
}
