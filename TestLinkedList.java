class TestLinkedList{
	public static void main(String[] a){
		TestList head;
		boolean aux;
		head=new TestList();
		aux=head.Init();
		head=head.Insert(4);
		head=head.Insert(5);
		head=head.Insert(6);
		System.out.println(head.GetElem());
		System.out.println(head.GetNext().GetElem());
		System.out.println(head.GetNext().GetNext().GetElem());
		if(aux){}else{}
	}
}

class TestList{
	//int elem ;
	//TestList next ;
	boolean end ;
	//int elem ;

	// Initialize the node list as the last node
	public boolean Init(){
		TestList next ;
		int elem ;
		//TestList next ;
		end = true ;
		return true ;
	}

	// Initialize the values of a new node
	public boolean InitNew(int v_elem, TestList v_next, boolean v_end){
		end = v_end ;
		elem = v_elem ;
		next = v_next ;
		return true ;
	}

	// Insert a new node at the beginning of the list
	public TestList Insert(int new_elem){
		boolean ret_val ;
		TestList aux03 ;
		TestList aux02 ;
		aux03 = this ;
		aux02 = new TestList();
		ret_val = aux02.InitNew(new_elem,aux03,false);
		if (!ret_val) System.out.println(0); else{}
		return aux02 ;
	}

	public boolean GetEnd(){
		return end ;
	}

	public int GetElem(){
		return elem ;
	}

	public TestList GetNext(){
		return next ;
	}

	// Print the linked list
	public boolean Print(){
		TestList aux01 ;
		boolean var_end ;
		int  var_elem ;

		aux01 = this ;
		var_end = end ;
		var_elem = elem ;
		while (!var_end){
			System.out.println(var_elem);
			aux01 = aux01.GetNext() ;
			var_end = aux01.GetEnd();
			var_elem = aux01.GetElem();
		}

		return true ;
	}
}
