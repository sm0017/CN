import java.io.*;
import java.net.Socket;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

/**
 * Created by smitatm on 9/22/15.
 */
public class SimpleServerThread extends Thread {

    private Socket clientSocket = null;
    private File documentRoot = null;
    private static String HTTP_CODE_401 ="HTTP/1.0 404 File Not Found";
    private static String HTTP_CODE_501 ="HTTP/1.0 501 Not Implemented";


    public SimpleServerThread(Socket socket, File documentRoot)
    {

        this.clientSocket = socket;
        this.documentRoot = documentRoot;
    }

    public void run() {
        try {

            //get the output stream to write the response
            OutputStream out = new BufferedOutputStream(clientSocket.getOutputStream());
            Writer writer = new OutputStreamWriter(out);

            //get the Input stream to read the request
            Reader reader = new InputStreamReader(new BufferedInputStream(clientSocket.getInputStream()));

            StringBuilder requestLine = new StringBuilder();

            while (true)
            {

                //Read the request and build the string
                int requestParse = reader.read();
                if (requestParse == '\r' || requestParse == '\n') break; //denotes end of request
                requestLine.append((char) requestParse);

            }
            String request = requestLine.toString();
            System.out.print(request);
          //use whitespace regex to split the String
            String[] splittedRequest = request.split("\\s+");

            String httpMethod = splittedRequest[0];
            //check if its GET request
            if (httpMethod.equals("GET"))
            {
                String filePath = splittedRequest[1];
                File file = new File(documentRoot, filePath.substring(1, filePath.length()));
                String contentType =
                        URLConnection.getFileNameMap().getContentTypeFor(filePath);
                if (file.canRead() && file.getCanonicalPath().startsWith(documentRoot.getCanonicalPath())) {
                    byte[] fileContent = Files.readAllBytes(file.toPath());
                    writeHeader(writer, "HTTP/1.0 200 OK", contentType, fileContent.length);
                    out.write(fileContent);
                    out.flush();
                } else {

                    String body = new StringBuilder("<HTML>\r\n")
                            .append("<HEAD><TITLE>File Not Found</TITLE>\r\n")
                            .append("</HEAD>\r\n")
                            .append("<BODY>")
                            .append("<H1>HTTP Error 404: File Not Found</H1>\r\n")
                            .append("</BODY></HTML>\r\n").toString();
                    writeHeader(writer, HTTP_CODE_401, "text/html; charset=utf-8", body.length());
                    writer.write(body);
                    writer.flush();

                }
            } else
            { // method does not equal "GET"
                String body = new StringBuilder("<HTML>\r\n")
                        .append("<HEAD><TITLE>Not Implemented</TITLE>\r\n")
                        .append("</HEAD>\r\n")
                        .append("<BODY>")
                        .append("<H1>HTTP Error 501: Not Implemented</H1>\r\n")
                        .append("</BODY></HTML>\r\n").toString();

                writeHeader(writer, HTTP_CODE_501, "text/html; charset=utf-8", body.length());


            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void writeHeader(Writer writer, String httpResponseCode, String contentType, int length) throws IOException
    {
        writer.write(httpResponseCode + "\r\n");
        Date now = new Date();
        writer.write("Date: " + now + "\r\n");
        writer.write("Server: SimpleHttpServer 2.0\r\n");
        writer.write("Content-type: " + contentType + "\r\n\r\n");
        writer.write("Content-length: " + length + "\r\n");
        writer.flush();
    }

}

