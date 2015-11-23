
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.FileInputStream;
import java.io.InputStream;
// import java.util.*;

public class TestTypeCheckVisitor {

    public static void main(String[] args) throws Exception {
        String inputFile = null;
        if ( args.length>0 ) inputFile = args[0];
        else inputFile = "SumtorialMicro.java";
        System.out.println(inputFile);
        InputStream is = System.in;
        if ( inputFile!=null ) {
            is = new FileInputStream(inputFile);
        }
        ANTLRInputStream input = new ANTLRInputStream(is);
        MiniJavaLexer lexer = new MiniJavaLexer(input); 
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MiniJavaParser parser = new MiniJavaParser(tokens);
        parser.setBuildParseTree(true);      // tell ANTLR to build a parse tree
        ParseTree tree = parser.prog(); // parse
        int nerrors=parser.getNumberOfSyntaxErrors();
        // show tree in text form
        System.out.println(tree.toStringTree(parser));

        if (nerrors==0){
          SymTableVisitor symTableVisitor = new SymTableVisitor();
          symTableVisitor.visit(tree);
          symTableVisitor.symTab.printSymbolTable();
          
          if (!symTableVisitor.error.anyErrors){
            TypeCheckVisitor typeCheckVisitor = new TypeCheckVisitor();
            typeCheckVisitor.setSymbolTable(symTableVisitor.symTab);
            typeCheckVisitor.visit(tree);
          }
        }
    }
}
