import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * Container for the set of required methods to handle Player's move and throw request
 */
public class PlayerService {
    MagicLocation magicLocation;
    public static Player handleLogin(String requestParam2, GameResource gameResource, PrintWriter writer) {
        String playerName = requestParam2;
        Player player = new Player();
        PlayerPosition position = assignPosition(player, gameResource, writer);
        ArrayList<PlayerCookie> cookies = assignCookies(gameResource.randomNumber);
        player.setPlayerName(playerName);
        player.setPosition(position);
        player.setCookies(cookies);
        player.setMovedToMagicPlace(false);
        return player;
    }

    public static synchronized boolean checkPlayerExist(String playerName, GameResource gameResource) {
        boolean exist = false;
        if (gameResource.playerMap.isEmpty()) {
            exist = false;
        } else {
            for (Player player : gameResource.playerMap.values()) {
                if (player.getPlayerName().equals(playerName)) {
                    exist = true;
                    break;
                }
            }
        }
        return exist;
    }

    public static synchronized PlayerPosition assignPosition(Player player, GameResource gameResource, PrintWriter writer) {
        int playerCount = gameResource.playerMap.size();
        PlayerPosition position = null;
        if (playerCount == 0) {
            position = generatePosition(0, gameResource);
            gameResource.playerMap.put(position, player);
            gameResource.gameMap[position.getRow()][position.getColumn()] = 5;
        } else if (playerCount < gameResource.maxRow - 1) {
            position = generatePosition(playerCount, gameResource);
            gameResource.playerMap.put(position, player);
            gameResource.gameMap[position.getRow()][position.getColumn()] = 5;
        } else {
            String message = +201 + "only handles" + gameResource.maxRow + " player";
            try {
                GameService.writeResponse(writer, message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return position;
    }

    public static PlayerPosition generatePosition(int start, GameResource gameResource) {
        PlayerPosition position = new PlayerPosition();
        boolean unAssigned = true;
        while (unAssigned) {
            Random generateRandomNumber = new Random();
            int randomNumber = generateRandomNumber.nextInt(gameResource.maxColumn - start - 1);
            if (gameResource.gameMap[start][randomNumber] != 5 && gameResource.gameMap[start][randomNumber] != 1) {
                position.setRow(start);
                position.setColumn(randomNumber);
                unAssigned = false;
            }
        }
        return position;
    }

    private static ArrayList<PlayerCookie> assignCookies(int randomNumberStart) {
        int initialCookie = 2;
        ArrayList<PlayerCookie> cookiesInventory = new ArrayList<>();
        for (int i = 0; i < initialCookie; i++) {
            PlayerCookie cookie = new PlayerCookie();
            if (i % 5 == 0) {
                cookie.setCookieSpeed(5);
            } else {
                cookie.setCookieSpeed(1);
            }
            cookie.setCookieID(randomNumberStart++);
            cookiesInventory.add(cookie);
        }
        return cookiesInventory;
    }

    public void handlePlayerMove(String requestParam2, GameResource gameResource, Player player, PlayerSessionHandler sessionHandler) throws IOException {
        switch (requestParam2) {
            case "up":
            case "u":
                moveUp(player, gameResource, sessionHandler);
                GameService.brodCastMessage(playerUpdateOnMove(player), gameResource);
                break;
            case "down":
            case "d":
                moveDown(player, gameResource,sessionHandler);
                GameService.brodCastMessage(playerUpdateOnMove(player), gameResource);
                break;
            case "left":
            case "l":
                moveLeft(player, gameResource,sessionHandler);
                GameService.brodCastMessage(playerUpdateOnMove(player), gameResource);
                break;
            case "right":
            case "r":
                moveRight(player, gameResource,sessionHandler);
                GameService.brodCastMessage(playerUpdateOnMove(player), gameResource);
                break;
            default:
                String message = 400 + " Invalid command";
                GameService.writeResponse(sessionHandler.getWriter(), message);
                break;
        }
    }

    /*logic:Decrement current row by 1 and Check for currentRow equals first row
    if the currentRow is equal to first row i.e 0th row, currentRow-1 is less than 0 i.e negative so wrap the mam and mode
     player to the last row and column will remain same as player is moving verically

    */
    private void moveUp(Player player, GameResource gameResource, PlayerSessionHandler sessionHandler) {
        int currentRow = player.getPosition().getRow();
        int currentColumn = player.getPosition().getColumn();
        int newAssignedRow;
        int newAssignedColumn;

     //move successful when the player is not occupied or no barrier
        if (currentRow - 1 < 0 && gameResource.gameMap[gameResource.maxRow - 1][currentColumn] != 5 &&
                gameResource.gameMap[gameResource.maxRow - 1][currentColumn] != 1) {
            int newPositionStatus = gameResource.gameMap[gameResource.maxRow - 1][currentColumn];
                    newAssignedRow = gameResource.maxRow - 1;
            newAssignedColumn = currentColumn;
            updatePlayerState(player, gameResource, newAssignedRow, newAssignedColumn);
        } else if (currentRow - 1 > 0 && gameResource.gameMap[currentRow - 1][currentColumn] != 5 &&
                gameResource.gameMap[currentRow - 1][currentColumn] != 1) {
            int newPositionStatus = gameResource.gameMap[currentRow - 1][currentColumn] ;
            newAssignedRow = currentRow - 1;
            newAssignedColumn = currentColumn;
            updatePlayerState(player, gameResource, newAssignedRow, newAssignedColumn);
        }

    }

    // logic:Increment current row by 1 and Check for currentRow equals last row
    private void moveDown(Player player, GameResource gameResource, PlayerSessionHandler sessionHandler) {
        int currentRow = player.getPosition().getRow();
        int currentColumn = player.getPosition().getColumn();
        int newAssignedRow;
        int newAssignedColumn;
        if (currentRow + 1 > gameResource.maxRow - 1 && gameResource.gameMap[0][currentColumn] != 5 &&
                gameResource.gameMap[0][currentColumn] != 1) {
            int newPositionStatus = gameResource.gameMap[0][currentColumn];

            newAssignedRow = 0;
            newAssignedColumn = currentColumn;
            updatePlayerState(player, gameResource, newAssignedRow, newAssignedColumn);
        } else if (currentRow + 1 < gameResource.maxRow - 1 && gameResource.gameMap[currentRow + 1][currentColumn] != 5 &&
                gameResource.gameMap[currentRow + 1][currentColumn] != 1) {
            int newPositionStatus = gameResource.gameMap[currentRow + 1][currentColumn];

            newAssignedRow = currentRow + 1;
            newAssignedColumn = currentColumn;
            updatePlayerState(player, gameResource, newAssignedRow, newAssignedColumn);
        }
    }

    //logic:Decrement current column by 1 and Check for currentColumn equals first column
    private void moveLeft(Player player, GameResource gameResource, PlayerSessionHandler sessionHandler) {
        int currentRow = player.getPosition().getRow();
        int currentColumn = player.getPosition().getColumn();
        int newAssignedRow;
        int newAssignedColumn;

        if (currentColumn - 1 < 0 && gameResource.gameMap[currentRow][gameResource.maxColumn - 1] != 5 && gameResource.gameMap[currentRow][gameResource.maxColumn - 1] != 1) {
            int newPositionStatus = gameResource.gameMap[currentRow][gameResource.maxColumn - 1];

               newAssignedRow = currentRow;
               newAssignedColumn = gameResource.maxColumn - 1;
               updatePlayerState(player, gameResource, newAssignedRow, newAssignedColumn);


        } else if (currentColumn - 1 > 0 && gameResource.gameMap[currentRow][currentColumn - 1] != 5 && gameResource.gameMap[currentRow][gameResource.maxColumn - 1] != 1) {
            int newPositionStatus = gameResource.gameMap[currentRow][currentColumn - 1];

            newAssignedRow = currentRow;
            newAssignedColumn = currentColumn - 1;
            updatePlayerState(player, gameResource, newAssignedRow, newAssignedColumn);
        }
    }

    //logic:Increment current column by 1 and Check for current Column equals last column
    private void moveRight(Player player, GameResource gameResource, PlayerSessionHandler sessionHandler) {
        int currentRow = player.getPosition().getRow();
        int currentColumn = player.getPosition().getColumn();
        int newAssignedRow;
        int newAssignedColumn;
        if (currentColumn + 1 > gameResource.maxColumn - 1 && gameResource.gameMap[currentRow][0] != 5 &&
                gameResource.gameMap[currentRow][0] != 1) {
            int newPositionStatus = gameResource.gameMap[currentRow][0];

                newAssignedRow = currentRow;
            newAssignedColumn = 0;
            updatePlayerState(player, gameResource, newAssignedRow, newAssignedColumn);

        } else if (currentColumn + 1 < gameResource.maxColumn - 1 && gameResource.gameMap[currentRow][currentColumn + 1] != 5 &&
                gameResource.gameMap[currentRow][currentColumn + 1] != 1) {
            int newPositionStatus = gameResource.gameMap[currentRow][currentColumn + 1];

            newAssignedRow = currentRow;
            newAssignedColumn = currentColumn + 1;
            updatePlayerState(player, gameResource, newAssignedRow, newAssignedColumn);

        }
    }

    /*Before throw update all players and if cookie hits the player update cookie inventories and then both the players
    */
    public void handlePlayerThrow(String requestParam2, GameResource gameResource, Player player, PlayerSessionHandler sessionHandler) {
        PlayerCookie cookie = player.getCookies().get(0);
        switch (requestParam2) {
            case "up":
            case "u":
                throwCookieUp(player, gameResource, cookie);
                break;
            case "down":
            case "d":
                throwCookieDown(player, gameResource, cookie);
                break;
            case "left":
            case "l":
                throwCookieLeft(player, gameResource, cookie);
                break;
            case "right":
            case "r":
                throwCookieRight(player, gameResource, cookie);
                break;
            default:
                GameService.invalidCommand(sessionHandler.writer);
                break;
        }
    }

    //Throw cookie up :position[playerCurrentRow-1,currentColumn]
    private void throwCookieUp(Player player, GameResource gameResource, PlayerCookie cookie) {
        int currentRow = player.getPosition().getRow();
        int currentColumn = player.getPosition().getColumn();

        // Check if the player exist where cookie is thrown
        if (currentRow - 1 < 0 && gameResource.gameMap[gameResource.maxRow - 1][currentColumn] == 5) {

            //this is condition where cookie hits the player
            PlayerPosition throwPosition = new PlayerPosition();
            throwPosition.setRow(gameResource.maxRow - 1);
            throwPosition.setColumn(currentColumn);
            GameService.brodCastMessage(cookieThrowMessage(cookie.getCookieID(), throwPosition), gameResource);

            if (gameResource.playerMap.containsKey(throwPosition)) {
                Player playerHit = gameResource.playerMap.get(throwPosition);
                int cookieHitID = cookie.getCookieID();
                updateCookieState(player, playerHit, cookie, gameResource);
                GameService.brodCastMessage(cookieHit(playerHit,cookieHitID), gameResource);
            }

        } else if (currentRow - 1 < 0 && (gameResource.gameMap[gameResource.maxRow - 1][currentColumn] == 1 ||
                gameResource.gameMap[gameResource.maxRow - 1][currentColumn] == 0)) {
            PlayerPosition throwPosition = new PlayerPosition();
            throwPosition.setRow(gameResource.maxRow - 1);
            throwPosition.setColumn(currentColumn);
            GameService.brodCastMessage(cookieThrowMessage(cookie.getCookieID(), throwPosition), gameResource);
            GameService.brodCastMessage(cookieMiss(cookie.getCookieID(),throwPosition), gameResource);

        } else if ((currentRow - 1 > 0 || currentRow - 1 == 0) && gameResource.gameMap[currentRow - 1][currentColumn] == 5) {
            PlayerPosition throwPosition = new PlayerPosition();
            throwPosition.setRow(currentRow - 1);
            throwPosition.setColumn(currentColumn);
            GameService.brodCastMessage(cookieThrowMessage(cookie.getCookieID(), throwPosition), gameResource);
            if (gameResource.playerMap.containsKey(throwPosition)) {
                Player playerHit = gameResource.playerMap.get(throwPosition);
                int cookieHitID = cookie.getCookieID();
                updateCookieState(player, playerHit, cookie, gameResource);
                GameService.brodCastMessage(cookieHit(playerHit, cookieHitID), gameResource);
            }
        } else if ((currentRow - 1 > 0 || currentRow - 1 == 0) && (gameResource.gameMap[currentRow - 1][currentColumn] == 1
                || gameResource.gameMap[currentRow - 1][currentColumn] == 0)) {
            PlayerPosition throwPosition = new PlayerPosition();
            throwPosition.setRow(currentRow - 1);
            throwPosition.setColumn(currentColumn);
            GameService.brodCastMessage(cookieThrowMessage(cookie.getCookieID(), throwPosition), gameResource);
            GameService.brodCastMessage(cookieMiss(cookie.getCookieID(), throwPosition), gameResource);
        }
        GameService.updateOnCookieThrow(gameResource);
    }

    //Throw cookie Down:position[playerCurrentRow+1,currentColumn]
    private void throwCookieDown(Player player, GameResource gameResource, PlayerCookie cookie) {
        int currentRow = player.getPosition().getRow();
        int currentColumn = player.getPosition().getColumn();
        if (currentRow + 1 > gameResource.maxRow - 1 && gameResource.gameMap[0][currentColumn] == 5) {
            PlayerPosition throwPosition = new PlayerPosition();
            throwPosition.setRow(0);
            throwPosition.setColumn(currentColumn);
            GameService.brodCastMessage(cookieThrowMessage(cookie.getCookieID(), throwPosition), gameResource);
            if (gameResource.playerMap.containsKey(throwPosition)) {
                int cookieHitID = cookie.getCookieID();
                Player playerHit = gameResource.playerMap.get(throwPosition);
                updateCookieState(player, playerHit, cookie, gameResource);
                GameService.brodCastMessage(cookieHit(playerHit, cookieHitID), gameResource);
            }
        } else if (currentRow + 1 > gameResource.maxRow - 1 && (gameResource.gameMap[0][currentColumn] == 1 ||
                gameResource.gameMap[0][currentColumn] == 0)) {
            PlayerPosition throwPosition = new PlayerPosition();
            throwPosition.setRow(0);
            throwPosition.setColumn(currentColumn);
            GameService.brodCastMessage(cookieThrowMessage(cookie.getCookieID(), throwPosition), gameResource);
            GameService.brodCastMessage(cookieMiss(cookie.getCookieID(), throwPosition), gameResource);
        } else if ((currentRow + 1 < gameResource.maxRow - 1 || currentRow + 1 == gameResource.maxRow - 1) &&
                gameResource.gameMap[currentRow + 1][currentColumn] == 5) {
            PlayerPosition throwPosition = new PlayerPosition();
            throwPosition.setRow(currentRow + 1);
            throwPosition.setColumn(currentColumn);
            GameService.brodCastMessage(cookieThrowMessage(cookie.getCookieID(), throwPosition), gameResource);
            if (gameResource.playerMap.containsKey(throwPosition)) {
                Player playerHit = gameResource.playerMap.get(throwPosition);
                int cookieHitID = cookie.getCookieID();
                updateCookieState(player, playerHit, cookie, gameResource);
                GameService.brodCastMessage(cookieHit(playerHit, cookieHitID), gameResource);
            }
        } else if ((currentRow + 1 < gameResource.maxRow - 1 || currentRow + 1 == gameResource.maxRow - 1) &&
                (gameResource.gameMap[currentRow + 1][currentColumn] == 1 ||
                        gameResource.gameMap[currentRow + 1][currentColumn] == 0)) {
            PlayerPosition throwPosition = new PlayerPosition();
            throwPosition.setRow(currentRow + 1);
            throwPosition.setColumn(currentColumn);
            GameService.brodCastMessage(cookieThrowMessage(cookie.getCookieID(), throwPosition), gameResource);
            GameService.brodCastMessage(cookieMiss(cookie.getCookieID(), throwPosition), gameResource);
        }
        GameService.updateOnCookieThrow(gameResource);
    }

    //Throw cookie Left :position[playerCurrentRow,currentColumn-1]
    private void throwCookieLeft(Player player, GameResource gameResource, PlayerCookie cookie) {
        int currentRow = player.getPosition().getRow();
        int currentColumn = player.getPosition().getColumn();

        if (currentColumn - 1 < 0 && gameResource.gameMap[currentRow][gameResource.maxColumn - 1] == 5) {
            PlayerPosition throwPosition = new PlayerPosition();
            throwPosition.setRow(currentRow);
            throwPosition.setColumn(gameResource.maxColumn - 1);
            GameService.brodCastMessage(cookieThrowMessage(cookie.getCookieID(), throwPosition), gameResource);
            if (gameResource.playerMap.containsKey(throwPosition)) {
                Player playerHit = gameResource.playerMap.get(throwPosition);
                int cookieHitID = cookie.getCookieID();
                updateCookieState(player, playerHit, cookie, gameResource);
                GameService.brodCastMessage(cookieHit(playerHit, cookieHitID), gameResource);
            }
        } else if (currentColumn - 1 < 0 && (gameResource.gameMap[currentRow][gameResource.maxColumn - 1] == 1 ||
                gameResource.gameMap[currentRow][gameResource.maxColumn - 1] == 0)) {
            PlayerPosition throwPosition = new PlayerPosition();
            throwPosition.setRow(currentRow);
            throwPosition.setColumn(gameResource.maxColumn - 1);
            GameService.brodCastMessage(cookieThrowMessage(cookie.getCookieID(), throwPosition), gameResource);
            GameService.brodCastMessage(cookieMiss(cookie.getCookieID(), throwPosition), gameResource);
        } else if ((currentColumn - 1 > 0 || currentColumn - 1 == 0) &&
                gameResource.gameMap[currentRow][currentColumn - 1] == 5) {
            PlayerPosition throwPosition = new PlayerPosition();
            throwPosition.setRow(currentRow);
            throwPosition.setColumn(currentColumn - 1);
            GameService.brodCastMessage(cookieThrowMessage(cookie.getCookieID(), throwPosition), gameResource);
            if (gameResource.playerMap.containsKey(throwPosition)) {
                Player playerHit = gameResource.playerMap.get(throwPosition);
                int cookieHitID = cookie.getCookieID();
                updateCookieState(player, playerHit, cookie, gameResource);
                GameService.brodCastMessage(cookieHit(playerHit, cookieHitID), gameResource);
            }
        } else if ((currentColumn - 1 > 0 || currentColumn - 1 == 0) &&
                gameResource.gameMap[currentRow][currentColumn - 1] == 1 ||
                gameResource.gameMap[currentRow][currentColumn - 1] == 0) {
            PlayerPosition throwPosition = new PlayerPosition();
            throwPosition.setRow(currentRow);
            throwPosition.setColumn(currentColumn - 1);
            GameService.brodCastMessage(cookieThrowMessage(cookie.getCookieID(), throwPosition), gameResource);
            GameService.brodCastMessage(cookieMiss(cookie.getCookieID(), throwPosition), gameResource);
        }
        GameService.updateOnCookieThrow(gameResource);
    }


    //Throw cookie Right :position[playerCurrentRow,currentColumn+1]
    private void throwCookieRight(Player player, GameResource gameResource, PlayerCookie cookie) {
        int currentRow = player.getPosition().getRow();
        int currentColumn = player.getPosition().getColumn();

        if (currentColumn + 1 > gameResource.maxColumn - 1 && gameResource.gameMap[currentRow][0] == 5) {
            PlayerPosition throwPosition = new PlayerPosition();
            throwPosition.setRow(currentRow);
            throwPosition.setColumn(0);
            GameService.brodCastMessage(cookieThrowMessage(cookie.getCookieID(), throwPosition), gameResource);
            if (gameResource.playerMap.containsKey(throwPosition)) {
                Player playerHit = gameResource.playerMap.get(throwPosition);
                int cookieHitID = cookie.getCookieID();
                updateCookieState(player, playerHit, cookie, gameResource);
                GameService.brodCastMessage(cookieHit(playerHit, cookieHitID), gameResource);
            }
        } else if (currentColumn + 1 > gameResource.maxColumn - 1 && (gameResource.gameMap[currentRow][0] == 0 ||
                gameResource.gameMap[currentRow][0] == 1)) {
            PlayerPosition throwPosition = new PlayerPosition();
            throwPosition.setRow(currentRow);
            throwPosition.setColumn(0);
            GameService.brodCastMessage(cookieThrowMessage(cookie.getCookieID(), throwPosition), gameResource);
            GameService.brodCastMessage(cookieMiss(cookie.getCookieID(), throwPosition), gameResource);
        } else if ((currentColumn + 1 < gameResource.maxColumn - 1 || currentColumn + 1 == gameResource.maxColumn - 1)
                && gameResource.gameMap[currentRow][currentColumn + 1] == 5) {
            PlayerPosition throwPosition = new PlayerPosition();
            throwPosition.setRow(currentRow);
            throwPosition.setColumn(currentColumn + 1);
            GameService.brodCastMessage(cookieThrowMessage(cookie.getCookieID(), throwPosition), gameResource);
            if (gameResource.playerMap.containsKey(throwPosition)) {
                Player playerHit = gameResource.playerMap.get(throwPosition);
                int cookieHitID = cookie.getCookieID();
                updateCookieState(player, playerHit, cookie, gameResource);
                GameService.brodCastMessage(cookieHit(playerHit, cookieHitID), gameResource);
            }
        } else if ((currentColumn + 1 > gameResource.maxColumn - 1 || currentColumn + 1 == gameResource.maxColumn - 1) &&
                (gameResource.gameMap[currentRow][currentColumn + 1] == 1 ||
                        gameResource.gameMap[currentRow][currentColumn + 1] == 0)) {
            PlayerPosition throwPosition = new PlayerPosition();
            throwPosition.setRow(currentRow);
            throwPosition.setColumn(currentColumn + 1);
            GameService.brodCastMessage(cookieThrowMessage(cookie.getCookieID(), throwPosition), gameResource);
            GameService.brodCastMessage(cookieMiss(cookie.getCookieID(), throwPosition), gameResource);
        }
        GameService.updateOnCookieThrow(gameResource);
    }

    private static void updatePlayerState(Player player, GameResource gameResource, int newAssignedRow, int newAssignedColumn) {
        synchronized (gameResource.gameMap) {
            gameResource.gameMap[newAssignedRow][newAssignedColumn] = 5;
            synchronized (gameResource.playerMap) {
                gameResource.gameMap[player.position.row][player.position.column] = 0;
                gameResource.playerMap.remove(player.position);
                player.position.setRow(newAssignedRow);
                player.position.setColumn(newAssignedColumn);
                gameResource.playerMap.put(player.position, player);
            }
        }
    }

    private static void updateCookieState(Player player, Player playerHit, PlayerCookie cookie, GameResource gameResource) {
        player.getCookies().remove(cookie);
        if (player.cookies.isEmpty()) {
            GameService.brodCastMessage(winnerMessage(player), gameResource);
        }
        playerHit.getCookies().add(cookie);
    }

    //Status 103 : Cookie Throw Update
    private static String cookieThrowMessage(int cookieId, PlayerPosition position) {
        return 103 + " " + cookieId + ", " + position.getRow() + ", " + position.getColumn();
    }

    //Status code :101 Winner Message
    private static String winnerMessage(Player player) {
        return 101 + " " + player.getPlayerName() + ", is the Winner of the Game";
    }

    //Status code:103 : Player update on move
    public static String playerUpdateOnMove(Player player) {
        String message;
        int currentRow = player.position.getRow();
        int currentColumn = player.position.getColumn();
        int cookie = player.getCookies().size();
        message = 104 + " " + player.getPlayerName() + ", " + currentRow + ", " + currentColumn + ", " + cookie;
        return message;
    }


    //Status code:105 : Player update on move
    public static String cookieHit(Player player, int cookieId) {
        String message;
        int currentRow = player.position.getRow();
        int currentColumn = player.position.getColumn();
        int cookie = player.getCookies().size();
        message = 105 + " " +cookieId +  ", " + currentRow + ", " + currentColumn + ", " + player.getPlayerName() ;
        return message;
    }


    //Status code:105 : Player update on move
    public static String cookieMiss(int cookieId, PlayerPosition position) {
        String message;
        int currentRow = position.getRow();
        int currentColumn = position.getColumn();
        message = 105 + " " +cookieId +  ", " + currentRow + ", " + currentColumn + ", " + -1 ;
        return message;
    }

    // Player Update on for 'msg' request : Status code 104
    public void handleMsgRequest(String requestParam2, GameResource gameResource, PlayerSessionHandler sessionHandler) {
        if ("all".equals(requestParam2) || "a".equals(requestParam2)) {
            synchronized (gameResource.activePlayerSessions) {
                Iterator<PlayerSessionHandler> active = gameResource.activePlayerSessions.iterator();
                while (active.hasNext()) {
                    PlayerSessionHandler session = active.next();
                    if (!session.isAlive()) {
                        active.remove();
                        session.interrupt();
                    } else {
                        try {
                            Player p = session.getPlayer();
                            GameService.writeResponse(sessionHandler.writer, PlayerService.playerUpdateOnMove(p));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        } else if ("map".equals(requestParam2)) {
            GameService.sendInitialMap(gameResource.maxRow, gameResource.maxColumn, gameResource.gameMap, sessionHandler.writer);
        } else {
            String playerName = requestParam2;
            synchronized (gameResource.activePlayerSessions) {
                Iterator<PlayerSessionHandler> active = gameResource.activePlayerSessions.iterator();
                while (active.hasNext()) {
                    PlayerSessionHandler session = active.next();
                    if (!session.isAlive()) {
                        active.remove();
                        session.interrupt();
                    } else {
                        try {
                            Player p = session.getPlayer();
                            if (p.getPlayerName().equals(playerName)) {
                                GameService.writeResponse(sessionHandler.writer, PlayerService.playerUpdateOnMove(p));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public void handleQuit(GameResource gameResource, Player player, PlayerSessionHandler session) {

        synchronized (gameResource.gameMap) {
            synchronized (gameResource.playerMap) {
                gameResource.gameMap[player.position.row][player.position.column] = 0;
                gameResource.playerMap.remove(player.position);
                gameResource.activePlayerSessions.removeSession(session);
            }
        }

        synchronized (gameResource.activePlayerSessions) {
            Iterator<PlayerSessionHandler> active = gameResource.activePlayerSessions.iterator();
            while (active.hasNext()) {
                PlayerSessionHandler s = active.next();
                if (!session.isAlive()) {
                    active.remove();
                    session.interrupt();
                } else {
                    try {
                        Player p = session.getPlayer();
                        String message = +201 + " " + player.playerName + " is leaving the game";
                        GameService.writeResponse(s.writer, message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        session.interrupt();
    }

 /*   public void handleMagicLocation(Player player, GameResource gameResource, PlayerSessionHandler sessionHandler) {

       // List the port on which we running another server and create  new socket
        String serverAddress = "localhost";
        int magicPort = 6666;
        try{
            Socket magicSocket = new Socket(serverAddress, magicPort);
            if (magicSocket!=null){
                 magicLocation = new MagicLocation(player, gameResource, magicSocket, sessionHandler);

            }else {
                //handle in normal way
            }

         }catch (IOException e){

        }

    }
    */


}