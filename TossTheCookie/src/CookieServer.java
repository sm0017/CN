import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;

public class CookieServer {
    private static final Logger logger = Logger.getLogger("CookieServer");
    public static HashMap<Player.PlayerPosition, Player> playerMap = new HashMap<>();
    public static int[][] gameMap;

    public static void main(String args[]) throws IOException {
        Properties properties = new Properties();
        InputStream configFile;
        int portNumber = 0, mapSize = 0;
        gameMap = new int[mapSize][mapSize];
        try {
            configFile = new FileInputStream("configPortRoot.properties");
            properties.load(configFile);
            portNumber = Integer.parseInt(properties.getProperty("portNumber"));
            mapSize = Integer.parseInt(properties.getProperty("mapSize"));
            generateGameMap(mapSize, gameMap);
        } catch (RuntimeException e) {
            e.getCause();
        }

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            while (true) {
                logger.info("Server is Listening on the port " + portNumber);
                Socket clientSocket = serverSocket.accept();
                OutputStream outputStream = clientSocket.getOutputStream();
                Writer writer = new OutputStreamWriter(outputStream);
                GameService gameService = new GameService();
                if (clientSocket != null) {
                    String mapDimension=+mapSize+", "+mapSize;
                    gameService.writeResponse(writer,200,mapDimension);
                    new ServiceThread(clientSocket, playerMap, gameMap, mapSize).start();
                } else {
                    logger.info("error in connection");
                }
            }
        } catch (IOException e) {
            System.err.println("Can not create Server socket on the specified port " + portNumber);
            System.exit(-1);
        }
    }
    //gameMap[][]=0,PlayerCanMove || gameMap[][]=1, playerCan'tMove || gameMap[][]=5, afterPlayerMoved
    public static void generateGameMap(int mapSize, int map[][]) {
        for (int i = 0; i < mapSize; i++) {
            for (int j = 0; j < mapSize; j++) {
                if (i % 6 == 0 && j % 6 == 0 || i % 20 == 0 && j % 20 == 0) {
                    map[i][j] = 0;
                } else {
                    map[i][j] = 1;
                }

            }
        }
    }
}





