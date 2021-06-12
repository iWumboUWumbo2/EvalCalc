JFLAGS = -g
JC = javac
RM=del

.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	Calculator.java \
	Expression.java 

default: classes

classes: $(CLASSES:.java=.class)

run:
	java Calculator

clean:
	$(RM) *.class