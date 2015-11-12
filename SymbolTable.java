import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;

public class SymbolTable {

	HashMap<String, SymbolAttributes> symbols; // the main data structure
	HashMap<String, SymbolTable> nextLevel; // internal leveling 
	public static final byte FIELD = 0, METHOD = 1, PARAM=2, LOCAL=3, CLASS=4;
	public static final String[] kindString={"field","method","param","local","class"};
	private String SymbolTableID;
	private String SymbolTableType;


	SymbolTable(String id, String type){
		nextLevel = new HashMap<String, SymbolTable>(); // create an empty hash table for other symbol tables
		symbols = new HashMap<String, SymbolAttributes>(); // creates an empty hash table
		SymbolTableID = id;
		SymbolTableType = type;
	}

	public boolean addClass(String id){
		return symbols.put(id, new SymbolAttributes(id, CLASS)) == null;
	}  
	public boolean addField(String id, String type){
		return symbols.put(id, new SymbolAttributes(id, FIELD, type)) == null;
	}
	public boolean addMethod(String id, String type){
		return symbols.put(id, new SymbolAttributes(id, METHOD, type)) == null;
	}
	public boolean addParam(String id, String type){
		return symbols.put(id, new SymbolAttributes(id, PARAM, type)) == null;
	}
	public boolean addLocal(String id, String type){
		return symbols.put(id, new SymbolAttributes(id, LOCAL, type)) == null;
	}
	public SymbolTable addSymbolTable(String id, String type){
		SymbolTable symTmp =  new SymbolTable(id,type);
		nextLevel.put(id,symTmp);
		return symTmp;
	}
	public SymbolAttributes get(String id){    
		return symbols.get(id);
	}
	public void setCurrentClass(String id){
		SymbolTableID=id;
	}
	public String getCurrentClass(){
		return SymbolTableID;
	}

	public boolean inSymbolTable(String id){
		Collection symbolsCol = symbols.entrySet();
		Iterator symbolsItr = symbolsCol.iterator();
		while(symbolsItr.hasNext()){
			Map.Entry symbolsEntry = (Map.Entry) symbolsItr.next();
			SymbolAttributes theAttribute = (SymbolAttributes) symbolsEntry.getValue();
			if(theAttribute.symbolId.equals(id)) return true;      
		}
		return false;
	}

	public void printSymbolTable(){
		System.out.print("\n");
		if(SymbolTableType.equals(kindString[4])) System.out.print("     ");
		if(SymbolTableType.equals(kindString[1])) System.out.print("              ");
		System.out.println("Contents of the symbol table : " + SymbolTableID);
		Collection symbolsCol = symbols.entrySet();
		Collection levelCol = nextLevel.entrySet();
		Iterator symbolsItr = symbolsCol.iterator();
		Iterator levelItr = levelCol.iterator();
		while(symbolsItr.hasNext()){
			Map.Entry symbolsEntry= (Map.Entry) symbolsItr.next();
			SymbolAttributes theAttribute = (SymbolAttributes) symbolsEntry.getValue();
			if(SymbolTableType.equals(kindString[4])) System.out.print("     ");
			if(SymbolTableType.equals(kindString[1])) System.out.print("              ");
			theAttribute.printSymbolAttributes();      
		}
		while(levelItr.hasNext()){
			Map.Entry levelEntry = (Map.Entry) levelItr.next();
			SymbolTable levelTable = (SymbolTable) levelEntry.getValue();
			levelTable.printSymbolTable();     
		}

	}
}