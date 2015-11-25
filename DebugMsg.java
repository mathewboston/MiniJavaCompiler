public class DebugMsg
//Dr Longpre
{
	boolean reporting;

	void report(String msg) {
		if (reporting)
			System.out.println(msg);
	}
	DebugMsg(){
		reporting=true;
	}
	DebugMsg(boolean setReporting){
		reporting=setReporting;
	}
}
