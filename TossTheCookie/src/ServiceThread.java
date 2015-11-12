/**
 * Created by smitatm on 11/7/15.
 */

import java.io.*;
import java.net.Socket;
import java.util.*;

public class ServiceThread extends Thread {

    public static HashMap<Player.PlayerPosition, Player> playerMap;
    int[][] gameMap;
    int dimension;
    private Socket clientSocket = null;

    public ServiceThread(Socket socket, HashMap playerMap, int gameMap[][], int dimension) {
        this.clientSocket = socket;
        this.playerMap = playerMap;
        this.gameMap = gameMap;
        this.dimension = dimension;
    }

    public static String[] parseRequest(Reader reader) throws IOException {
        StringBuilder requestLine = new StringBuilder(100);
        while (true) {
            int requestParse = reader.read();
            if (requestParse == '\r' || requestParse == '\n')
                break;
            requestLine.ensureCapacity(100);
            requestLine.append((char) requestParse);
        }
        String request = requestLine.toString();
        String[] splitRequest = request.split("\\s+");
        return splitRequest;
    }

    public void run() {
        try {

            InputStream inputStream = new BufferedInputStream(clientSocket.getInputStream());
            Reader reader = new InputStreamReader(inputStream);
            OutputStream outputStream = clientSocket.getOutputStream();
            Writer writer = new OutputStreamWriter(outputStream);

            String request[] = parseRequest(reader);

            Player player = new Player();
            GameService gameService = new GameService();

            if (request[0].equals("LOGIN")||request[0].equals("login")) {
                boolean checkExisting = true;
                String playerName = request[1];
                if (checkExisting) {
                    Iterator it = playerMap.entrySet().iterator();
                    while (it.hasNext()) {
                        Player validPlayer = (Player) it.next();
                        if (validPlayer.getPlayerName().equals(playerName)) {
                            player = validPlayer;
                            break;
                        }
                    }
                } else {
                    player = new Player();
                    Player.PlayerPosition position = assignPosition(gameMap, player, playerMap);
                    ArrayList<Player.PlayerCookie> cookies = assignCookies(player);
                    player.setPlayerName(playerName);
                    player.setPosition(position);
                    player.setCookies(cookies);
                }

                String messageOnLogin = playerName+ " Welcome to TossTheCookie game server";
                gameService.writeResponse(writer, 100, messageOnLogin);
                String initialPosition = "(" + player.position.getRow() + "," + player.position.getColumn() + "," + player.getCookies().size() + " )";
                gameService.writeResponse(writer, 104, initialPosition);
            } else {
                checkRequestAndRespond(request, clientSocket, dimension, gameMap, playerMap, player);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String checkRequestAndRespond(String[] request, Socket socket, int dimension, int gameMap[][],
                                         HashMap<Player.PlayerPosition, Player> playerMap, Player player) throws IOException {
        OutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
        Writer writer = new OutputStreamWriter(outputStream);
        GameService game = new GameService();
        String requestType = request[0];
        boolean moved = true;
        switch (requestType) {

            case "move":
            case "MOVE":
            case "M":
            case "m":
                String direction = request[1];
                player.movePlayer(direction, playerMap, gameMap, dimension, player);
                String messageOnMove = playerStatus(player);

                game.writeResponse(writer, 104, messageOnMove);

                break;

            case "throw":
            case "THROW":
            case "T":
            case "t":

                direction = request[1];
                ArrayList cookies = player.getCookies();
                if (!cookies.isEmpty()) {
                    Player.PlayerCookie cookie = (Player.PlayerCookie) cookies.get(0);
                    player.throwCookie(player, gameMap, direction, dimension, playerMap, cookie);
                    String messageOnThrow = playerStatus(player);
                   game.writeResponse(writer, 104, messageOnThrow);
                } else {
                    String messageOnWin = "winner" + player.getPlayerName();
                    game.writeResponse(writer, 102, messageOnWin);
                }

                break;

            case "MSG":
            case "msg":

                String allOrOne = request[1];
                playerUpdate(allOrOne, playerMap, writer);
                break;
            case "quit":
            case "QUIT":
            case "q":
            case "Q":
                synchronized (gameMap) {
                    int r = player.getPosition().getRow();
                    int c = player.getPosition().getColumn();
                    gameMap[r][c] = 1;
                    synchronized (playerMap) {
                        Player.PlayerPosition p = new Player.PlayerPosition();
                        p.setRow(r);
                        p.setColumn(c);
                        playerMap.remove(p);
                    }
                }
                break;

            default:
                String invalid = "bad Command: " +requestType;
                game.writeResponse(writer, 400, invalid);

        }
        return null;
    }

    private Player.PlayerPosition assignPosition(int gameMap[][], Player player, HashMap<Player.PlayerPosition, Player> playerMap) {

        Player.PlayerPosition position = generatePosition(1, 128);

        if (playerMap.get(position).equals(null) && position.getColumn() < 128 && position.getRow() < 128) {
            playerMap.put(position, player);
        } else {
            assignPosition(gameMap, player, playerMap);
        }
        return position;
    }

    private ArrayList<Player.PlayerCookie> assignCookies(Player player) {
        int initialCookie = 25;
        ArrayList cookiesInventory = new ArrayList();
        for (int i = 0; i < initialCookie; i++) {
            Player.PlayerCookie cookie = new Player.PlayerCookie();
            if (i % 5 == 0) {
                cookie.setCookieSpeed(5);
            } else {
                cookie.setCookieSpeed(1);
            }
            cookiesInventory.add(i, cookie);
        }
        return cookiesInventory;
    }


    public Player.PlayerPosition generatePosition(int start, int end) {
        int randomRow, randomColumn;
        Random generateRandomNumber = new Random();
        randomRow = generateRandomNumber.nextInt(end - start + 1) + start;
        randomColumn = generateRandomNumber.nextInt(start * 13 + 25) + start;
        Player.PlayerPosition position = new Player.PlayerPosition();
        position.setColumn(randomRow);
        position.setColumn(randomColumn);
        return position;

    }

    public String playerStatus(Player player) {
        String message;
        int currentRow = player.position.getRow();
        int currentColumn = player.position.getColumn();
        int cookie = player.getCookies().size();
        message = "(" + player.getPlayerName() + ", " + currentRow + ", " + currentColumn + ", " + cookie + ")";
        return message;
    }

    public void playerUpdate(String playerName, HashMap playerMap, Writer writer) throws IOException {
        String message = null;
        GameService gameService = new GameService();
        if (playerName.equals("all") || playerName.equals("a")) {
            Iterator it = playerMap.entrySet().iterator();
            while (it.hasNext()) {
                Player player = (Player) it.next();
                message = "(" + player.getPlayerName() + ", " + player.position.getRow() + ", " + player.position.getColumn() + ", " + player.getCookies() + ")";
                gameService.writeResponse(writer, 104, message);
                break;
            }

        } else {
            Iterator it = playerMap.entrySet().iterator();
            while (it.hasNext()) {
                Player player = (Player) it.next();
                if (player.getPlayerName().equals(playerName)) {
                    message = "(" + player.getPlayerName() + ", " + player.position.getRow() + ", " + player.position.getColumn() + ", " + player.getCookies() + ")";
                    gameService.writeResponse(writer, 104, message);
                    break;
                }

            }
        }
    }
}