import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;

public class SymbolTable {

	HashMap<String, SymbolAttributes> symbols; // the main data structure
	ArrayList<SymbolTable>  nextLevel; // internal leveling 
	public static final byte FIELD = 0, METHOD = 1, PARAM=2, LOCAL=3, CLASS=4;
	public static final String[] kindString={"field","method","param","local","class"};
	private String SymbolTableID;
	private String SymbolTableType;
	private int methodIDCount; //issue ids to all methods for leveling
	private int classIDCount; //issue ids to all classes for leveling

	SymbolTable(String id , String type){
		//if(type == Class) 
		nextLevel = new ArrayList<SymbolTable>();
		symbols = new HashMap<String, SymbolAttributes>(); // creates an empty hash table
		SymbolTableID = id;
		SymbolTableType = type;
		methodIDCount = 0;
		classIDCount = 0;
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

	public int addSymbolTable(String id, String type){

		SymbolTable st = new SymbolTable(id, type);
		if(type.equals("class")) {
			nextLevel.add(classIDCount++,st);
			return classIDCount-1;
		}
		nextLevel.add(methodIDCount++,st);
		return methodIDCount-1;
	}

	public SymbolTable getSymbolTable(int id){
		return nextLevel.get(id);
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
		Iterator levelItr = nextLevel.iterator();
		Iterator symbolsItr = symbolsCol.iterator();
		while(symbolsItr.hasNext()){
			Map.Entry symbolsEntry= (Map.Entry) symbolsItr.next();
			SymbolAttributes theAttribute = (SymbolAttributes) symbolsEntry.getValue();
			if(SymbolTableType.equals(kindString[4])) System.out.print("     ");
			if(SymbolTableType.equals(kindString[1])) System.out.print("              ");
			theAttribute.printSymbolAttributes();      
		}
		while(levelItr.hasNext()){
			SymbolTable levelTable = (SymbolTable) levelItr.next();
			levelTable.printSymbolTable();     
		}

	}
}