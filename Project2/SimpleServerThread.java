import java.io.*;
import java.net.Socket;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Date;

/**
 * Created by smitatm on 9/22/15.
 */
public class SimpleServerThread extends Thread {

    private Socket clientSocket = null;
    private File documentRoot = null;

    private static String HTTP_CODE_404 ="HTTP/1.0 404 File Not Found";
    private static String HTTP_CODE_501 ="HTTP/1.0 501 Not Implemented";
    String error501File = "error501.html";
    String error404File = "error404.html";

    public SimpleServerThread(Socket socket, File documentRoot)
    {

        this.clientSocket = socket;
        this.documentRoot = documentRoot;
    }

    public void run() {
        try {

            //get the output stream to write the response
            OutputStream outputStream = new BufferedOutputStream(clientSocket.getOutputStream());
            Writer writer = new OutputStreamWriter(outputStream);

            //get the Input stream to read the request
            InputStream inputStream = new BufferedInputStream(clientSocket.getInputStream());
            Reader reader = new InputStreamReader(inputStream);

            StringBuilder requestLine = new StringBuilder(100);

            while (true)
            {

            //Read the request and build the String
                int requestParse = reader.read();
                if (requestParse == '\r' || requestParse == '\n') break; //only reads First line of the request
                requestLine.ensureCapacity(100);
                requestLine.append((char) requestParse);
            }
            String request = requestLine.toString();

            //use whitespace regex to split the String
            String[] splittedRequest = request.split("\\s+");

            String httpMethod = splittedRequest[0];
            //check if its GET request
            if (httpMethod.equals("GET"))
            {
                String filePath = splittedRequest[1];

                System.out.print(documentRoot + "\n");
                System.out.print(filePath + "\n");

                File file = new File(documentRoot, filePath.substring(1, filePath.length()));
                String[] filepath = file.getCanonicalPath().split("\\.");
                String fileExtension = filepath[1];
                String contentType = checkMimeType(fileExtension, file);


                if (file.canRead() && file.getCanonicalPath().startsWith(documentRoot.getCanonicalPath()))
                {
                    byte[] fileContent = Files.readAllBytes(file.toPath());
                    int length = fileContent.length;
                    writeResponseHeader(writer, "HTTP/1.0 200 OK", contentType, length);
                    outputStream.write(fileContent);
                    outputStream.flush();
                }
                else
                {
                    //serves error file from documentRoot folder
                    File errorFile = new File (documentRoot +"/" +error404File);
                    byte[] errorFileContent = Files.readAllBytes(errorFile.toPath());
                    writeResponseHeader(writer, HTTP_CODE_404, "text/html", errorFileContent.length);
                    outputStream.write(errorFileContent);
                    outputStream.flush();
                }


            } else
            {
                // method does not equal "GET" serve the error file from document root folder
                File errorFile = new File (documentRoot +"/" +error501File);
                byte[] error501Content = Files.readAllBytes(errorFile.toPath());
                writeResponseHeader(writer, HTTP_CODE_501, "text/html", error501Content.length);
                outputStream.write(error501Content);
                outputStream.flush();

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
//Write the Response Header
    public void writeResponseHeader(Writer writer, String httpResponseCode, String mimeType, int length) throws IOException
    {
        writer.write(httpResponseCode + "\r\n");
        Date currentDate = new Date();
        writer.write("Date: " + currentDate + "\r\n");
        writer.write("Server: SimpleHttpServer 1.0" +"\r\n");
        writer.write("Content-length: " + length + "\r\n");
        writer.write("Content-type: " + mimeType +"\r\n");
        writer.write("\r\n");
        writer.flush();
    }
// checks the MimeType depending on the File extension

    public String checkMimeType(String fileExtension, File file)
    {
        String contentType = null;

          if( fileExtension.equals("jpg")|| fileExtension.equals("jpeg")|| fileExtension.equals("jpe"))
          {contentType="image/jpg";
          }
          else if (fileExtension.equals("png")){  contentType="image/png";}
          else if (fileExtension.equals("gif")){contentType="image/gif";}
          else if( fileExtension.equals("html")||fileExtension.equals("text")) {
                                     contentType="text/html";}
          else if (fileExtension.equals("mp3")){
                   contentType="audio/mpeg3"; }
          else {
              //handles unknown MimeTypes
              try {
                  contentType = URLConnection.getFileNameMap().getContentTypeFor(file.getCanonicalPath());
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }


        return contentType;
    }

}

