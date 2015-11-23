#!/bin/bash
echo export CLASSPATH=".:/usr/local/lib/antlr-4.5.1-complete.jar:$CLASSPATH"
echo alias antlr4='java -Xmx500M -cp "/usr/local/lib/antlr-4.5.1-complete.jar:$CLASSPATH" org.antlr.v4.Tool'
echo alias grun='java org.antlr.v4.runtime.misc.TestRig'
