
all: 
	echo "make java or make c"

java :list compil


list :	
	find -name *.java > sources_J.makeJava
compil :
	javac  -classpath "${CLASSPATH}" @sources_J.makeJava

clean:
	$(shell find . -type f -name \*.class -delete)


c :




