import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MagicLocation extends Thread {
    BufferedReader remoteMagicReader;
    PrintWriter remoteMagicWriter;
    Socket remoteMagicSocket;
    Player player;
    GameResource gameResource;
    PlayerSessionHandler magicPlayerSession;
    String request;
    PrintWriter localMessageWriter;
    BufferedReader localMessageReader;

    public MagicLocation(String request, Player player, GameResource gameResource, Socket socket, PlayerSessionHandler sessionHandler) {
        this.player = player;
        this.gameResource = gameResource;
        this.remoteMagicSocket = socket;
        this.magicPlayerSession = sessionHandler;
        this.request = request;
        try {

            // Create input and output stream to start the communication between Local server and remote server
            remoteMagicReader = new BufferedReader(new InputStreamReader(remoteMagicSocket.getInputStream()));
            remoteMagicWriter = new PrintWriter(remoteMagicSocket.getOutputStream(), true);
            boolean remoteLogin = true;
            // Handle remote login of the player
            while (remoteLogin) {
                String remoteResponse = remoteMagicReader.readLine();
                localMessageWriter = sessionHandler.getWriter();
                localMessageReader = sessionHandler.getReader();
                String magicMessage = null;
                if (null != remoteResponse) {
                    if (remoteResponse.startsWith("200")) {
                        magicMessage = 301 + " " + player.playerName + " moved to the magic world!";
                        GameService.brodCastMessage(magicMessage, gameResource);
                        // remove the player which move to magic location from activePlayer Session list
                        // gameResource.activePlayerSessions.removeSession(sessionHandler);
                        // Local server sends login request on behalf of the player which moved magic location and wait for
                        // remote server response
                        String magicLogin = "login " + gameResource.listeningPort + ":" + player.playerName;
                        handleMagicRequest(remoteMagicWriter, magicLogin);
                    }
                    // Get the remote server map and communicate with magic player
                    else if (remoteResponse.startsWith("102")) {
                        GameService.writeResponse(localMessageWriter, remoteResponse);
                    } else if (remoteResponse.startsWith("104")) {
                        handleProxy(remoteResponse, localMessageWriter);
                    } else if (remoteResponse.startsWith("100")) {
                        //Player successfully logged  in
                        handleProxy(remoteResponse, localMessageWriter);
                        remoteLogin = false;  // This break the loop and start the thread
                    }
                    //login error
                    else if (remoteResponse.startsWith("400") || remoteResponse.startsWith("300")) {
                        GameService.writeResponse(localMessageWriter, remoteResponse);
                    } else if (remoteResponse.startsWith("500")) {
                        GameService.writeResponse(localMessageWriter, remoteResponse);
                    }
                }
            }
        } catch (IOException e) {
            // some Error
        }
    }

    // Strip the player name from the player object
    //send login playerName to the other server and wait for its response
    // playerName always send as port:playerName to avoid name conflict and
    // Server has to take care between the mapping realPlayerName to port:playerName to correctly identify the client
    private void handleMagicRequest(PrintWriter magicWriter, String magicPlayerRequest) {
        try {
            GameService.writeResponse(magicWriter, magicPlayerRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleMagicResponse(PrintWriter magicMessageWriter, String magicResponse) {
        if (null == magicResponse) {
            if (magicResponse.startsWith("102")) {
                try {
                    GameService.writeResponse(magicMessageWriter, magicResponse);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (magicResponse.startsWith("104")) {
                handleProxy(magicResponse, magicMessageWriter);
            }
            //Errors
            else if (magicResponse.startsWith("400") || magicResponse.startsWith("300") ||
                    magicResponse.startsWith("500")) {
                try {
                    GameService.writeResponse(magicMessageWriter, magicResponse);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (magicResponse.startsWith("103") || magicResponse.startsWith("105")) {
                try {
                    GameService.writeResponse(magicMessageWriter, magicResponse);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void handleProxy(String magicResponse, PrintWriter magicMessageWriter) {

        String[] magicResponseArray = magicResponse.split("\\s");
        String realPlayerName;
        String[] port1Player2 = magicResponseArray[1].split(":");
        if (port1Player2.length == 2) {
            realPlayerName = port1Player2[1];
        } else {
            realPlayerName = port1Player2[0];
        }
        String proxyMsg = buildString(realPlayerName, magicResponseArray);
        try {
            GameService.writeResponse(magicMessageWriter, proxyMsg);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    public static String buildString(String realPlayerName, String[] magicResponseArray) {
        StringBuffer msg = new StringBuffer(200);
        String proxyMessage = null;
        for (int i = 0; i < magicResponseArray.length; i++) {
            if (i == 1) {
                msg.append(realPlayerName);
                msg.append(" ");
            } else if (i == magicResponseArray.length - 1) {
                msg.append(magicResponseArray[i]);
            } else if (i < magicResponseArray.length) {
                msg.append(magicResponseArray[i]);
                msg.append(" ");
            }
        }
        proxyMessage = msg.toString();
        return proxyMessage;
    }

    public void run() {
        handleRemoteRequest();
    }

    public void handleRemoteRequest() {
        try {
            while (true) {
                String remoteResponse = null;
                String magicPlayerRequest = null;
                magicPlayerRequest = localMessageReader.readLine();
                // This method pass the player message to the remote server
                handleMagicRequest(remoteMagicWriter, magicPlayerRequest);


                while (true) {
                    // Read the remote response on player input
                    try {
                        remoteResponse = remoteMagicReader.readLine();
                    } catch (IOException en) {
                        en.getCause();
                    }
                    // Local server receives the input from the the remote server and communicate it with the player
                    if (!remoteResponse.equals(null)) {
                        handleMagicResponse(localMessageWriter, remoteResponse);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

