
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

    private static final Logger logger = Logger.getLogger("SimpleHttpServer");


    public static void main(String args[]) throws IOException
    {
        Properties properties = new Properties();
        InputStream configFile;
        File documentRoot =null;
        int portNumber=0;

//Read the Port Number and Document Root from configuration file
        try {
            configFile = new FileInputStream("configPortRoot.properties");
            properties.load(configFile);
            portNumber = Integer.parseInt(properties.getProperty("portNumber"));
            documentRoot=  new File(properties.getProperty("Document_root"));

            } catch (RuntimeException e){  e.getCause();}



//Create new ServerSocket on specified port
        try (ServerSocket serverSocket = new ServerSocket(portNumber))
        {
            while(true)
            {
                logger.info("Server is Listening on the port " +portNumber);
                Socket clientSocket = serverSocket.accept();
                logger.info("Connection has been established");
                new SimpleServerThread(clientSocket, documentRoot).start();
            }
        } catch(IOException e){
            System.err.println("Can not create Server socket on the specified port " +portNumber);
            System.exit(-1);
        }

    }

}

