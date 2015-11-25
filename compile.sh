java -jar /usr/local/lib/antlr-4.5.1-complete.jar -no-listener -visitor MiniJava.g4
javac *Msg.java
javac MiniJava*.java
javac Sym*.java
javac Type*.java
#javac Test*Visitor.java
javac *CodeGen*.java 
