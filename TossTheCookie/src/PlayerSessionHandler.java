/**
 * Represents the Player Session. Handles player's request
 */

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlayerSessionHandler extends Thread {

    BufferedReader reader;
    PrintWriter writer;
    GameResource gameResource;
    ActivePlayerSession activePlayerSession;
    private Socket clientSocket;
    private Player player;
    private static Map<String, MagicLocation> magicMap;
    MagicLocation magicLocation;


    public PlayerSessionHandler(Socket socket, GameResource gameResource) {
        this.clientSocket = socket;
        this.gameResource = gameResource;
        try {
            magicMap = new HashMap<>();
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            String message = 200 + " " + gameResource.maxRow + ", " + gameResource.maxColumn + "\r\n";
            GameService.writeResponse(writer, message);
            boolean inValidLogin = true;
            while (inValidLogin) {

                String request = reader.readLine();

                // Check Player input
                if (!request.equals(null)) {

                    String requestType[] = request.split("\\s");
                    //length 2 indicates valid input. All the input of size two except quit
                    //First request should be login and then pnly player can play
                    if (requestType.length == 2 && "login".equals(requestType[0]) || "l".equals(requestType[0])) {
                        boolean exist = PlayerService.checkPlayerExist(requestType[1], gameResource);
                        if (exist) {
                            message = 400 + " Player exists!";
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
                            message = PlayerService.playerUpdateOnMove(player);
                            GameService.brodCastMessage(message, gameResource);
                            GameService.existingPlayerUpdate(gameResource, this);
                            String successLoginMsg = 100 + " " + this.getPlayer().playerName + "! ready to Toss The Cookie!";
                            GameService.writeResponse(writer, successLoginMsg);
                            inValidLogin = false; //break the loop and start the new thread
                        }

                    } else {
                        GameService.invalidCommand(writer);
                    }

                }
            }
        } catch (IOException e) {
            String message = 500 + "Server Error";
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

    public void setReader(BufferedReader reader) {
        this.reader = reader;
    }

    public PrintWriter getWriter() {
        return writer;

    }

    public static Map<String, MagicLocation> getMap() {
        return magicMap;
    }

    public void setWriter(PrintWriter writer) {
        this.writer = writer;
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

    public void run() {

        while (true) {
            try {
                Player player = this.getPlayer();
                String request = null;
                // Continuously read the player input
                try {
                    request = reader.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                PlayerService playerService = new PlayerService();
                String requestParam1=null, requestParam2 =null;
                // Check for the input command for null
                if (!request.equals(null)) {
                    boolean isMagicMove = false;
                    String requestType[] = request.split("\\s");
                  if (requestType.length==2){
                    requestParam1 = requestType[0];
                    requestParam2 = requestType[1];
                  }else if (requestType.length==1 && requestParam1.equals("q")){


                  }else {
                      GameService.invalidCommand(writer);
                  }

                    // check if the player moved to magic location
                    if (!player.isMovedToMagicPlace() && !requestParam2.equals(null)){
                        isMagicMove = GameService.checkIfMagicMove(player, requestParam2, gameResource.maxRow, gameResource.maxColumn);
                    }

                   // if yes handle the login to magic remove server and setMovedFlag to true
                    if (isMagicMove) {
                        String serverAddress = "localhost";
                        int remotePort = 6666;
                        try {
                            Socket remoteSocket = new Socket(serverAddress, remotePort);
                            if (!remoteSocket.equals(null)) {
                                magicLocation = new MagicLocation(request, player, gameResource, remoteSocket, this);
                                player.setMovedToMagicPlace(true);
                            }
                        } catch (IOException e) {
                        }
                    }

// if player is local then handle local play
                    if (!player.isMovedToMagicPlace()) {
                        if (requestType.length == 2) {

                            switch (requestParam1) {
                                case "move":
                                case "MOVE":
                                case "m":


                                    playerService.handlePlayerMove(requestParam2, gameResource, player, this);

                                    break;

                                case "throw":
                                case "THROW":
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
                        } else if ("q".equals(requestType[0]) || "quit".equals(requestType[0])) {
                            playerService.handleQuit(gameResource, this.getPlayer(), this);

                            try {
                                clientSocket.close();
                            } catch (IOException e) {
                            } finally {
                                GameService.close(clientSocket);
                            }
                            break;
                        } else {
                            //    GameService.invalidCommand(writer);
                        }
                    } else if (player.isMovedToMagicPlace()) {
                        magicLocation.handleRemoteRequest();
                    }
                }else { }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}




