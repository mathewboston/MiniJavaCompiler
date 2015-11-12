import org.antlr.v4.runtime.*;
public class ErrorMsg 
{
 boolean anyErrors, reporting;

 void report(String msg) {
  anyErrors = true;
  if (reporting)
   System.out.println(msg);
 }
 void report(ParserRuleContext ctx, String msg) {
   anyErrors = true;
   if (reporting)
     System.out.println("Line "+ctx.start.getLine()+": "+msg);
 }
 ErrorMsg(){
  anyErrors=false;
  reporting=true;
 }
 ErrorMsg(boolean setReporting){
  anyErrors=false;
  reporting=setReporting;
 }
}
