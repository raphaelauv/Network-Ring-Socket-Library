# Network-Ring-Socket-Library

Socket library who implement a RING protocol

Socket are connected in RING ,at any moment a new socket can rejoin the ring by connecting to any socket already present

A socket can connect to two different ring and become a duplicating socket


<img src="https://user-images.githubusercontent.com/10202690/32146518-238aa99c-bcd9-11e7-9896-8f2f4d12a7e0.jpg" alt="alt text" width="700" height="whatever">


With 4 exemple of use :
#### TRANS : application of file transfert 
#### DIFF : chat room messenger
#### SECURISE : crypted RSA conversation
#### ENTITY : do nothing but ensures the transmission of messages on the ring

## Compilation
	use makeFile

## Execution

	java -cp ./Java application.Diff 4545 7878 9898 -v

	java -cp ./Java application.Trans 4545 7878 9898 -v
	
	java -cp ./Java application.Securise 4545 7878 9898 -v

	java -cp ./Java Entity 4545 7878 9898 -v


	src/C :
	./main port_tcp port_udp addr_multi port_multi
