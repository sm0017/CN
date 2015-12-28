/**
 * Represents the Player Session. Handles player's request
 * This class allow player to connect to the server
 * Handles login
 * handle magicLocation
 * Handle players request
 * When player connect to server : send 200 maxRow MaxColumn
 * after successful login:104 player messages
 * 100 welcome message
 *
 */

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.Properties;

public class PlayerSessionHandler extends Thread {

    BufferedReader reader;
    PrintWriter writer;
    GameResource gameResource;
    ActivePlayerSession activePlayerSession;
    private Socket clientSocket;
    private Player player;
    MagicLocation magicLocation;
    static boolean isMagicMove = false;

    public PlayerSessionHandler(Socket socket, GameResource gameResource) {
        this.clientSocket = socket;
        this.gameResource = gameResource;
        try {

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            String message = 200 + " " + gameResource.maxRow + ", " + gameResource.maxColumn + "\r\n";
            GameService.writeResponse(writer, message);
            boolean isLoginFailed = true;

        // When player connect to the server, read player input
            while (isLoginFailed) {

                String request = reader.readLine();
                if (!request.equals(null)) {

                    String requestType[] = request.split("\\s");
                    //length 2 indicates valid input. All the input of sizes =2 except quit
                    //Player session will start only after the successful login.

                    if (requestType.length == 2 && "login".equals(requestType[0]) || "l".equals(requestType[0])) {
                        boolean exist = PlayerService.checkPlayerExist(requestType[1], gameResource);
                        if (exist) {
                            message = 201 + " Player exists!";
                            try {
                                GameService.writeResponse(writer, message);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        } else {

                            GameService.sendInitialMap(gameResource.maxRow, gameResource.maxColumn, gameResource.gameMap, writer);
                            Player player = PlayerService.handleLogin(requestType[1], gameResource, writer);
                            this.setPlayer(player);
                            message = 104 + " " + player.playerName + ", " + player.position.row + ", " + player.position.column
                                    + ", " + player.cookies.size();
                            try {
                                GameService.writeResponse(writer, message);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                            message = PlayerService.playerPositionUpdate(player);
                            GameService.brodCastMessage(message, gameResource);
                            GameService.existingPlayerUpdate(gameResource, this);
                            String successLoginMsg = 100 + " " + this.getPlayer().playerName + "! ready to Toss The Cookie!";
                            GameService.writeResponse(writer, successLoginMsg);
                            isLoginFailed = false; //break the loop and start the new thread
                        }

                    } else {
                        GameService.invalidCommand(writer);
                    }

                }
            }
        } catch (IOException e) {
            String message = 400 + "Server Error";
            try {
                GameService.writeResponse(writer, message);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public BufferedReader getReader() {
        return reader;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public ActivePlayerSession getActivePlayerSession() {
        return activePlayerSession;
    }

    // This loop run indefinitely to read the players input
    // Calls different method handle login, Magic location movement, player move and throw request
    public void run() {

        while (true) {

            try {
                Player player = this.getPlayer();
                String request = null;
                try {
                    request = reader.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                PlayerService playerService = new PlayerService();
                int inputSize= GameService.checkValidInSize(request);
                String requestParam1, requestParam2 ;
                // check if the player entered some input command
                if (inputSize==2)
                {
                    String requestType[] = request.split("\\s");
                        requestParam1 = requestType[0];
                        requestParam2 = requestType[1];

                    /*check if the player is moving to magic location: the intial value of isMovedToMagicPlace=false
                      and it will be only true when the player moves to magic location*/

                    if (!player.isMovedToMagicPlace() && null!=requestParam2) {
                        isMagicMove = GameService.checkIfMagicMove(player, requestParam1, requestParam2, gameResource.maxRow,
                                      gameResource.maxColumn);
                    }

                    // if yes handle the login to magic remove server and setMovedFlag to true
                    if (isMagicMove) {
                        InputStream configFile = new FileInputStream("configPortRoot.properties");
                        Properties properties = new Properties();
                        properties.load(configFile);
                        String serverAddress = properties.getProperty("remoteServerAddress");
                        int remotePort = Integer.parseInt(properties.getProperty("remotePort"));

                        try {
                            Socket remoteSocket = new Socket(serverAddress, remotePort);
                            if (!remoteSocket.equals(null)) {
                                player.setMovedToMagicPlace(true);
                                new MagicLocation(request, player, gameResource, remoteSocket, this).start();
                            }else {
                                player.setMovedToMagicPlace(false);
                            }
                        } catch (IOException e) {
                        }
                    }

                    // if player is not moved to magic place handle all request locally
                    if (!player.isMovedToMagicPlace()) {
                        if (requestType.length == 2) {

                            switch (requestParam1) {
                                case "move":
                                case "m":
                                    playerService.handlePlayerMove(requestParam2, gameResource, player, this);
                                    break;

                                case "throw":
                                case "t":
                                    playerService.handlePlayerThrow(requestParam2, gameResource, player, this);
                                    break;
                                case "MSG":
                                case "msg":
                                    playerService.handleMsgRequest(requestParam2, gameResource, this);
                                    break;
                                default:
                                    GameService.invalidCommand(writer);
                                    break;
                            }
                        }
                    }else if (player.isMovedToMagicPlace()) {
                        magicLocation.handleRemoteRequest();
                    }
                }else if (inputSize==1 && request.startsWith("q")||request.startsWith("quit")
                        || request.startsWith("y")||request.startsWith("yes")){

                    playerService.handleQuit(gameResource, this.getPlayer(), this);
                    try {
                        clientSocket.close();
                    } catch (IOException e) {
                    } finally {
                        GameService.close(clientSocket);
                    }
                    break;
                }else {
                    GameService.invalidCommand(writer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}




