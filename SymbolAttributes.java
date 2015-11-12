import java.util.HashMap;

public class SymbolAttributes{
public String symbolId; // the symbol to which the attributes relate
public byte kind;  // could be FIELD, METHOD, PARAM, LOCAL or CLASS
public String type; //  for when we have a type associated with the symbol

   SymbolAttributes(String theId, byte theKind, String theType){
     // when the symbol is associated with a specific type
     symbolId = theId;
     kind = theKind;
     type = theType;
   }
   SymbolAttributes(String theId, byte theKind){
     symbolId = theId;
     kind = theKind;
     type = "";
   }
   String makeString(){
     // builds a string with the symbol and its attributes
     String ret_string;
     ret_string="ID "+symbolId;
     ret_string+=", kind "+SymbolTable.kindString[kind];
     if (type.length()>0)
       ret_string+=", type "+type;
     return ret_string;
   }
   void printSymbolAttributes(){
     System.out.println(makeString());
   }
}
