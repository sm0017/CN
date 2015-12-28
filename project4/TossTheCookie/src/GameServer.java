import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;

/**
 *Main Class: Accept the player requests and create new thread for each player
 * The main method reads the configurable parameters from the property file and
 * wraps essential values in the gameResource instance so that we can use it in the PlayerSession
 */
public class GameServer {

    private static final Logger logger = Logger.getLogger("CookieServer");
    private static ActivePlayerSession playerSessions = new ActivePlayerSession();
    private static HashMap<PlayerPosition, Player> playerMap = new HashMap<>();

    static int randomNumber=0;
    private static final GameResource gameResource= new GameResource();
    static int[][] gameMap;

    public static void main(String args[]) throws IOException {
        Properties properties = new Properties();
        InputStream configFile;
        int portNumber = 0, maxRow = 0, maxColumn = 0;

        try {
            configFile = new FileInputStream("configPortRoot.properties");
            properties.load(configFile);
            portNumber = Integer.parseInt(properties.getProperty("portNumber"));
            maxRow = Integer.parseInt(properties.getProperty("maxRow"));
            maxColumn = Integer.parseInt(properties.getProperty("maxColumn"));
            gameMap = new int[maxRow][maxColumn];
            GameService gm = new GameService();
            gm.generateGameMap(maxRow,maxColumn, gameMap);
        } catch (RuntimeException e) {
            e.getCause();
        }

        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            while (true) {
                randomNumber=randomNumber+2;
                gameResource.setPlayerMap(playerMap);
                gameResource.setGameMap(gameMap);
                gameResource.setActivePlayerSession(playerSessions);
                gameResource.setMaxRow(maxRow);
                gameResource.setMaxColumn(maxColumn);
                gameResource.setRandomNumber(randomNumber);
                gameResource.setListeningPort(portNumber);
                PlayerSessionHandler playerSession = new PlayerSessionHandler(serverSocket.accept(), gameResource);
                playerSessions.addSession(playerSession);
                playerSession.start();
               if (playerSession.writer.checkError()){
                   if (playerSession.isAlive()){
                       playerSession.interrupt();
                       System.out.println("Player Disconnected");
                   }
               }

            }
        } catch (IOException e) {
            logger.info("I/O error " + e.getMessage());
        }
    }
}










