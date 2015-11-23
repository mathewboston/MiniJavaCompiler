class Sumtorial{
    public static void main(String[] a){
     Sum f;
     f = new Sum();
	System.out.println(f.ComputeSum(10));
    }
} 

class Sum {

    public int ComputeSum(int num){
	int num_aux ;
	if (num < 1)
	    num_aux = 1 ;
	else 
	    num_aux = num + (this.ComputeSum(num-1)) ;
	return num_aux ;
    }

}
