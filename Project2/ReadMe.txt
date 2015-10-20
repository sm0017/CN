Name: Smita Sukhadeve
Assignment: Project 2 - Simple HTTP Server
Course: 540
Language Used: Java

Compilation: require JDK 1.7/1.8
Steps to compile and execute the program:
-Change the configuration parameter as required. Currently, a port is set to 5555, and you might want to change the user.home

If running on Windows/linux:
javac SimpleServerThread.java
javac SimpleHttpServer.java
java SimpleHttpServer
or
run  ./run-me.sh  (linux only)

Synopsis:

The submitted project consists of two programs.

1.SimpleHttpServer.java:
It contains the main method. It accepts the configuration parameter, i.e., Port number, document root name and creates
the server port. Once the server accepts the connection request from the client, it create the new
thread to handle different clients.

2. SimpleServerThread :
run()  method handles each new client connection  The method reads the first line of entered request. If the method is
'GET' then, it serves the requested files from the document root folder. It serves the error401.html file from the document
root if the requested file not found. Similarly, if a client asks for unsupported HTTP method, and then the error501.html file
is served.
writeResponseHeader(): writes the HTTP response header.
checkMimeType(): check mime type based on file extension.

3. error404.html: Get served when HTTP status code is 404
4. error501: HTML file for HTTP status code 501

5. configPropRoot.properties : Configure the parameters i.e port number and document root.

According to me, this project made me understand how HTTP protocol works in detail and Multithreading concept as well.
I missed to write the last blank line while writing the response header. Because of which, the server wasn't reading any file.
Later, I realize my mistake. Another thing, I struggled with is how to decide the MIME type because wasn't sure which file extension to check apart from .jpeg, mp3, text, html.
For standard media type, I decided to check the file extension and for the unknown binary content type used the Java's native library function getContentType(filename) method.



