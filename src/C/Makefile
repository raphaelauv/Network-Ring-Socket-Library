all:main

main: test.c protocole.o hashmap.o Thread.o
	gcc -pthread -Wall hashmap.o  protocole.o Thread.o test.c  -o main
Thread.o: Thread.c Thread.h
	gcc -c Thread.h Thread.c 
protocole.o:protocole.c protocole.h
	gcc -c protocole.h protocole.c
hashmap.o:
	gcc -c hashmap.c hashmap.h
clean :
	rm *.o
	rm *.gch
