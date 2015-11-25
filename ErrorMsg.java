import org.antlr.v4.runtime.*;
public class ErrorMsg
{
 boolean anyErrors, reporting;
 int errorCount;

 void report(String msg) {
  anyErrors = true;
  errorCount++;
  if (reporting)
   System.out.println(msg);
 }
 void report(ParserRuleContext ctx, String msg) {
   anyErrors = true;
   errorCount++;
   if (reporting)
     System.out.println("Line "+ctx.start.getLine()+": "+msg);
 }
 ErrorMsg(){
  errorCount = 0;
  anyErrors=false;
  reporting=true;
 }
 ErrorMsg(boolean setReporting){
  errorCount = 0;
  anyErrors=false;
  reporting=setReporting;
 }
}
