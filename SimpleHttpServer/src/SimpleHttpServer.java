
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Created by smitatm on 9/22/15.
 */
public class SimpleHttpServer
{

    private static final Logger logger = Logger.getLogger(SimpleHttpServer.class.getName());


    public static void main(String args[]) throws IOException
    {
        Properties properties = new Properties();
        InputStream configFile= null;
        File documentRoot =null;
        String documentRootName;
        int portNumber=0;
//
        try {
            configFile = new FileInputStream("configPortRoot.properties");
            properties.load(configFile);
            portNumber = Integer.parseInt(properties.getProperty("portNumber"));
            documentRoot=  new File(properties.getProperty("Document_root"));

        // portNumber = Integer.parseInt(args[0]);
          //  documentRoot = new File(args[1]);
            } catch (RuntimeException e) {
            e.getCause();
        }
//
        try (ServerSocket serverSocket = new ServerSocket(portNumber))
        {
            while(true)
            {
                Socket clientSocket = serverSocket.accept();
                new SimpleServerThread(clientSocket, documentRoot).start();
            }
        } catch(IOException e){
            System.err.println("can not listen on port" +portNumber);
            System.exit(-1);
        }

    }

}

