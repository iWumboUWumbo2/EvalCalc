JFLAGS = -g
JC = javac
RM=del
CLS=cls

.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	Calculator.java \
	Expression.java 

default: classes

classes: $(CLASSES:.java=.class)

run:
	$(CLS)
	java Calculator

clean:
	$(RM) *.class
