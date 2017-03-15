# CN
Computer Network assignments, HTTP server from scratch And Cookie Tossing Game (Multithreaded) using Socket/server 

Project -1 :

Number Guessing server using Java:
1.GameLogic.java: The program contains following methods:
checkGuess(): Compare entered guess and random number 
displayMessage(): Display messages based on the comparison
generateRandomNumber(): Generate random number between 1 and 100
2.NumberguessingServer :
The program handles the logic for socket creation and registering the service at port 5555 and main method. After accepting the connection, it writes output stream to the client, calls various methods in the GameLogic to evaluate the number entered by the client.

It was easier to create the socket, register service at particular port and communication between a server and the client using Java. Although, it was the simple program, give an idea about the how TCP/IP protocol works. I found it easier to use Scanner class to read entered guess(int) instead of the DataInputStream. The server wasn't reading entered integer correctly when I used DataInputStream.read() or readInt() method. 

Project 2 - Simple HTTP Server using TCP 



