
all: 
	echo "make java or make c"

java :list compil


list :	
	find -name *.java > sources_J.makeJava
compil :
	javac  -classpath "${CLASSPATH}" @sources_J.makeJava
c :
	make -C C

clean:
	$(shell echo 'find  ./Java -type f -name \*.class -delete')  
	rm -f  sources_J.makeJava

	





