import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.Random;

/**
 * Container for the set of required methods to handle Player's move and throw request
 */
public class PlayerService {

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
        int initialCookie=0;
        InputStream configFile =null;
        try{
        configFile = new FileInputStream("configPortRoot.properties");
        Properties properties = new Properties();
        properties.load(configFile);

        initialCookie = Integer.parseInt(properties.getProperty("initialCookieCnt"));
        }catch (IOException e){

        }finally {
            GameService.close(configFile);
        }
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

    // The player can only only move to the position where gameMap[row[column] = 0 and
    // The player can not move when the gameMam[row][column] = 5 which indicate the place is already  occupied by another player
    // The player can't move when the gameMap[row[[column]=1 which indicates the wall!
    // After each move the Player message '104 playerID , row, column, cookie' is sent to all Players
    //Player can only move Up , down, left and right. Other values will throw invalid Command to the particular player
    public void handlePlayerMove(String requestParam2, GameResource gameResource, Player player, PlayerSessionHandler sessionHandler) throws IOException {
        switch (requestParam2) {
            case "up":
            case "u":
                moveUp(player, gameResource, sessionHandler);
                GameService.brodCastMessage(playerPositionUpdate(player), gameResource);
                break;
            case "down":
            case "d":
                moveDown(player, gameResource,sessionHandler);
                GameService.brodCastMessage(playerPositionUpdate(player), gameResource);
                break;
            case "left":
            case "l":
                moveLeft(player, gameResource,sessionHandler);
                GameService.brodCastMessage(playerPositionUpdate(player), gameResource);
                break;
            case "right":
            case "r":
                moveRight(player, gameResource,sessionHandler);
                GameService.brodCastMessage(playerPositionUpdate(player), gameResource);
                break;
            default:
                String message = 300 + " Invalid command";
                GameService.writeResponse(sessionHandler.getWriter(), message);
                break;
        }
    }

   /* Logic: Moving player will only the change the current row as Player is moving vertically. The position on Y axis
   i.e. the column will remain same. But The condition where player is on the first row (i.e 0th row of map) is checked
   and game map is wrapped in this situation and player moves to the last row.
   game[row][column]==5 indicate occupies position
   gameMap[row][column]=1 indicate the wall
   CurrentRow -1 : moving up : value of row decreases as GameMap starts from 0.
   */

    private void moveUp(Player player, GameResource gameResource, PlayerSessionHandler sessionHandler) {
        int currentRow = player.getPosition().getRow();
        int currentColumn = player.getPosition().getColumn();
        int newAssignedRow,newAssignedColumn;
        PrintWriter playerWriter = sessionHandler.writer;

        if (currentRow - 1 < 0 && gameResource.gameMap[gameResource.maxRow - 1][currentColumn] != 5 &&
                gameResource.gameMap[gameResource.maxRow - 1][currentColumn] != 1) {
                    newAssignedRow = gameResource.maxRow - 1;
                    newAssignedColumn = currentColumn;
                    updatePlayerState(player, gameResource, newAssignedRow, newAssignedColumn);
                    moveStatusOk(playerWriter);

        } else if (currentRow - 1 > 0 && gameResource.gameMap[currentRow - 1][currentColumn] != 5 &&
                gameResource.gameMap[currentRow - 1][currentColumn] != 1) {
                int newPositionStatus = gameResource.gameMap[currentRow - 1][currentColumn] ;
                newAssignedRow = currentRow - 1;
                newAssignedColumn = currentColumn;
                updatePlayerState(player, gameResource, newAssignedRow, newAssignedColumn);
                moveStatusOk(playerWriter);
        }else {
            moveStatusNotOk(playerWriter);
        }

    }
    /* Logic: Moving down will only the change the current row as Player is moving vertically. The position on Y axis
     i.e. the column will remain same. But The condition where player is on the first row (i.e 0th row of map) is checked
     and game map is wrapped in this situation and player moves to the last row.
     game[row][column]==5 indicate occupies position
     gameMap[row][column]=1 indicate the wall
     CurrentRow + 1 : moving down : value of row increases as GameMap starts from 0 .
     */
    private void moveDown(Player player, GameResource gameResource, PlayerSessionHandler sessionHandler) {
        int currentRow = player.getPosition().getRow();
        int currentColumn = player.getPosition().getColumn();
        int newAssignedRow, newAssignedColumn;
        PrintWriter playerWriter = sessionHandler.writer;
        if (currentRow + 1 > gameResource.maxRow - 1 && gameResource.gameMap[0][currentColumn] != 5 &&
                gameResource.gameMap[0][currentColumn] != 1) {
            newAssignedRow = 0;
            newAssignedColumn = currentColumn;
            updatePlayerState(player, gameResource, newAssignedRow, newAssignedColumn);
            moveStatusOk(playerWriter);
        } else if (currentRow + 1 < gameResource.maxRow - 1 && gameResource.gameMap[currentRow + 1][currentColumn] != 5 &&
                gameResource.gameMap[currentRow + 1][currentColumn] != 1) {

            newAssignedRow = currentRow + 1;
            newAssignedColumn = currentColumn;
            updatePlayerState(player, gameResource, newAssignedRow, newAssignedColumn);
            moveStatusOk(playerWriter);
        }else {
            moveStatusNotOk(playerWriter);
        }
    }
    /* Logic: Moving Left will only the change the current column and will decrease its value by 1 as the column starts
    from 0. The condition where the player is on first column is takenn care by wrapping the map left to right and moving player
    to the last column
    */
    private void moveLeft(Player player, GameResource gameResource, PlayerSessionHandler sessionHandler) {
        int currentRow = player.getPosition().getRow();
        int currentColumn = player.getPosition().getColumn();
        int newAssignedRow, newAssignedColumn;
        PrintWriter playerWriter = sessionHandler.writer;

        if (currentColumn - 1 < 0 && gameResource.gameMap[currentRow][gameResource.maxColumn - 1] != 5 &&
                gameResource.gameMap[currentRow][gameResource.maxColumn - 1] != 1) {

               newAssignedRow = currentRow;
               newAssignedColumn = gameResource.maxColumn - 1;
               updatePlayerState(player, gameResource, newAssignedRow, newAssignedColumn);
               moveStatusOk(playerWriter);


        } else if (currentColumn - 1 > 0 && gameResource.gameMap[currentRow][currentColumn - 1] != 5 &&
                   gameResource.gameMap[currentRow][currentColumn - 1] != 1) {

                newAssignedRow = currentRow;
                newAssignedColumn = currentColumn - 1;
                updatePlayerState(player, gameResource, newAssignedRow, newAssignedColumn);
                moveStatusOk(playerWriter);

        }else {
            moveStatusNotOk(playerWriter);
        }
    }

    /* Logic: Moving right will only the change the current column and will increment its value by 1 as the column starts
      from 0. The condition where the player is on last column is taken care by wrapping the map and moving player
      to the first column
      */
    private void moveRight(Player player, GameResource gameResource, PlayerSessionHandler sessionHandler) {
        int currentRow = player.getPosition().getRow();
        int currentColumn = player.getPosition().getColumn();
        int newAssignedRow, newAssignedColumn;
        PrintWriter playerWriter = sessionHandler.writer;
        if (currentColumn + 1 > gameResource.maxColumn - 1 && gameResource.gameMap[currentRow][0] != 5 &&
                gameResource.gameMap[currentRow][0] != 1) {

            newAssignedRow = currentRow;
            newAssignedColumn = 0;
            updatePlayerState(player, gameResource, newAssignedRow, newAssignedColumn);
            moveStatusOk(playerWriter);

        } else if (currentColumn + 1 < gameResource.maxColumn - 1 && gameResource.gameMap[currentRow][currentColumn + 1] != 5 &&
                gameResource.gameMap[currentRow][currentColumn + 1] != 1) {

            newAssignedRow = currentRow;
            newAssignedColumn = currentColumn + 1;
            updatePlayerState(player, gameResource, newAssignedRow, newAssignedColumn);
            moveStatusOk(playerWriter);
        }else {
            moveStatusNotOk(playerWriter);
        }
    }

    /* A player can throw cookie Up, down, Left and Write
    Server checks which location player is throwing the cookie so that we can tell other players about the cookie throw
    and if they can move from that location : Message code 103
    Secondly, if the cookie hits wall then nothing will happen. Update player about this with -1 indicator in 'message with
    status code 105.
    Only one cookie will be thrown at a time and has speed =1 block
    Following messages will be sent to all player during the cookie throw
    103 cookieID, xRow, Xcolumn
    105 cookieID xRow, Xcolumn , playerID/-1
    104 playerid, xRow, Xcolumn, cookieCount : to update player about currents status

    */

    public void handlePlayerThrow(String requestParam2, GameResource gameResource, Player player, PlayerSessionHandler sessionHandler) {
        PlayerCookie cookie = player.getCookies().get(0);
        PrintWriter writer = sessionHandler.writer;
        switch (requestParam2) {
            case "up":
            case "u":
                throwCookieUp(player, gameResource, cookie, writer);
                break;
            case "down":
            case "d":
                throwCookieDown(player, gameResource, cookie,writer);
                break;
            case "left":
            case "l":
                throwCookieLeft(player, gameResource, cookie,writer);
                break;
            case "right":
            case "r":
                throwCookieRight(player, gameResource, cookie,writer);
                break;
            default:
                GameService.invalidCommand(sessionHandler.writer);
                break;
        }

    }

    //When  a player 'A' throws a cookie up it will fall at location position[playerCurrentRow-1,currentColumn] on the
    //game map. If Cookie hits the player 'B', server adds that cookie to the inventory of Player 'B';
    // And decrement from cookie inventory of the player A'

    private void throwCookieUp(Player player, GameResource gameResource, PlayerCookie cookie, PrintWriter writer) {
        int currentRow = player.getPosition().getRow();
        int currentColumn = player.getPosition().getColumn();

        // Cookie hits player
        if (currentRow - 1 < 0 && gameResource.gameMap[gameResource.maxRow - 1][currentColumn] == 5) {

            PlayerPosition throwPosition = new PlayerPosition();
            throwPosition.setRow(gameResource.maxRow - 1);
            throwPosition.setColumn(currentColumn);
            GameService.brodCastMessage(cookieThrowMessage(cookie.getCookieID(), throwPosition), gameResource);

            if (gameResource.playerMap.containsKey(throwPosition)) {
                Player playerHit = gameResource.playerMap.get(throwPosition);
                int cookieHitID = cookie.getCookieID();
                updateCookieState(player, playerHit, cookie, gameResource);

                if (player.getCookies().size()==0){
                    handleWinnerCondition(gameResource,player);
                    removeWinner(gameResource, player);
                }else {
                    GameService.brodCastMessage(cookieHit(playerHit,cookieHitID), gameResource);
                }
            }

        } // Cookie hits wall or empty block  on map
        else if (currentRow - 1 < 0 && (gameResource.gameMap[gameResource.maxRow - 1][currentColumn] == 1 ||
                gameResource.gameMap[gameResource.maxRow - 1][currentColumn] == 0)) {

            PlayerPosition throwPosition = new PlayerPosition();
            throwPosition.setRow(gameResource.maxRow - 1);
            throwPosition.setColumn(currentColumn);
            GameService.brodCastMessage(cookieThrowMessage(cookie.getCookieID(), throwPosition), gameResource);
            GameService.brodCastMessage(cookieMiss(cookie.getCookieID(),throwPosition), gameResource);

        } // cookie hits player
        else if ((currentRow - 1 > 0 || currentRow - 1 == 0) && gameResource.gameMap[currentRow - 1][currentColumn] == 5) {
            PlayerPosition throwPosition = new PlayerPosition();
            throwPosition.setRow(currentRow - 1);
            throwPosition.setColumn(currentColumn);

            GameService.brodCastMessage(cookieThrowMessage(cookie.getCookieID(), throwPosition), gameResource);
            if (gameResource.playerMap.containsKey(throwPosition)) {
                Player playerHit = gameResource.playerMap.get(throwPosition);
                int cookieHitID = cookie.getCookieID();
                updateCookieState(player, playerHit, cookie, gameResource);
                if (player.getCookies().size()==0){
                    handleWinnerCondition(gameResource,player);
                    removeWinner(gameResource, player);
                }else {
                    GameService.brodCastMessage(cookieHit(playerHit,cookieHitID), gameResource);
                }
            }
        } //Cookie hits the wall or empty block
        else if ((currentRow - 1 > 0 || currentRow - 1 == 0) && (gameResource.gameMap[currentRow - 1][currentColumn] == 1
                || gameResource.gameMap[currentRow - 1][currentColumn] == 0)) {

            PlayerPosition throwPosition = new PlayerPosition();
            throwPosition.setRow(currentRow - 1);
            throwPosition.setColumn(currentColumn);
            GameService.brodCastMessage(cookieThrowMessage(cookie.getCookieID(), throwPosition), gameResource);
            GameService.brodCastMessage(cookieMiss(cookie.getCookieID(), throwPosition), gameResource);
        }
        GameService.updateOnCookieThrow(gameResource);
    }

    //When  a player 'A' throws a cookie up it will fall at location position[playerCurrentRow+1,currentColumn] on the
    //game map. If Cookie hits the player 'B', server adds that cookie to the inventory of Player 'B';
    // And decrement from cookie inventory of the player A'

    private void throwCookieDown(Player player, GameResource gameResource, PlayerCookie cookie, PrintWriter sessionHandler) {
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
                if (player.getCookies().size()==0){
                    handleWinnerCondition(gameResource,player);
                    removeWinner(gameResource, player);
                }else {
                    GameService.brodCastMessage(cookieHit(playerHit,cookieHitID), gameResource);
                }
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
                if (player.getCookies().size()==0){
                    handleWinnerCondition(gameResource,player);
                    removeWinner(gameResource, player);
                }else {
                    GameService.brodCastMessage(cookieHit(playerHit,cookieHitID), gameResource);
                }
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

    //When  a player 'A' throws a cookie up it will fall at location position[playerCurrentRow,currentColumn-1] on the
    //game map.'

    private void throwCookieLeft(Player player, GameResource gameResource, PlayerCookie cookie, PrintWriter writer) {
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
                if (player.getCookies().size()==0){
                    handleWinnerCondition(gameResource,player);
                    removeWinner(gameResource, player);
                }else {
                    GameService.brodCastMessage(cookieHit(playerHit,cookieHitID), gameResource);
                };
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
                if (player.getCookies().size()==0){
                    handleWinnerCondition(gameResource,player);
                    removeWinner(gameResource, player);
                }else {
                    GameService.brodCastMessage(cookieHit(playerHit,cookieHitID), gameResource);
                }
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

    //When  a player 'A' throws a cookie up it will fall at location position[playerCurrentRow,currentColumn+1] on the
    //game map.'
    private void throwCookieRight(Player player, GameResource gameResource, PlayerCookie cookie, PrintWriter writer) {
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
                if (player.getCookies().size()==0){
                    handleWinnerCondition(gameResource,player);
                    removeWinner(gameResource, player);
                }else {
                    GameService.brodCastMessage(cookieHit(playerHit,cookieHitID), gameResource);
                }
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
            if (player.getCookies().size()==0){
                    handleWinnerCondition(gameResource,player);
                    removeWinner(gameResource, player);
                }else {
                    GameService.brodCastMessage(cookieHit(playerHit,cookieHitID), gameResource);
                }
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

    //This method is to update the gameMap when player moves to the particular block.
    //When player moves, server makes the value of previous position =0 and new position acquired by player =5
    // This method also takes care updating the player position coordinates
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

    public static  void handleWinnerCondition(GameResource gameResource, Player p){
        String message = 104 +p.getPlayerName() +", -1, -1, -1";
        GameService.brodCastMessage(message,gameResource);
    }

    public static void removeWinner (GameResource gameResource, Player winner) {

        synchronized (gameResource.activePlayerSessions) {
            Iterator<PlayerSessionHandler> active = gameResource.activePlayerSessions.iterator();
            while (active.hasNext()) {
                PlayerSessionHandler session = active.next();
                if (!session.isAlive()) {
                    active.remove();
                    session.interrupt();
                } else {
                    Player p = session.getPlayer();
                    if (p.playerName == winner.playerName) {

                        synchronized (gameResource.gameMap) {
                            synchronized (gameResource.playerMap) {
                                gameResource.gameMap[p.position.row][p.position.column] = 0;
                                gameResource.playerMap.remove(p.position);
                                gameResource.activePlayerSessions.removeSession(session);
                            }
                        }
                        break;
                    }
                }
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
        return 103 + " " +"C" +cookieId + ", " + position.getRow() + ", " + position.getColumn();
    }

    //Status code :101 Winner Message
    private static String winnerMessage(Player player) {
        return 101 + " " + player.getPlayerName() + ", won the Game!";
    }

    //Status code:103 : Player update on move
    public static String playerPositionUpdate(Player player) {
        String message;
        int currentRow = player.position.getRow();
        int currentColumn = player.position.getColumn();
        int cookie = player.getCookies().size();
        message = 104 + " " + player.getPlayerName() + ", " + currentRow + ", " + currentColumn + ", " + cookie;
        return message;
    }


    //Status code:105 : Cookie Hit message
    public static String cookieHit(Player player, int cookieId) {
        String message;
        int currentRow = player.position.getRow();
        int currentColumn = player.position.getColumn();
        int cookie = player.getCookies().size();
        message = 105 + " " +"C"+cookieId +  ", " + currentRow + ", " + currentColumn + ", " + player.getPlayerName() ;
        return message;
    }


    //Status code:105 : Cookie miss
    public static String cookieMiss(int cookieId, PlayerPosition position) {
        String message;
        int currentRow = position.getRow();
        int currentColumn = position.getColumn();
        message = 105 + " " +"C"+cookieId +  ", " + currentRow + ", " + currentColumn + ", " + -1 ;
        return message;
    }
    //Status code 200 : to convey player if the move was successful or  not
    public static void moveStatusOk(PrintWriter writer) {
        String message;
        message = 200 + "Moved!";

        try {
            GameService.writeResponse(writer,message);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    //Status code 200 : to convey player if the move was successful or  not
    public static void moveStatusNotOk(PrintWriter writer) {
        String message;
        message = 200 + "Not Moved!";

        try {
            GameService.writeResponse(writer,message);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                            GameService.writeResponse(sessionHandler.writer, PlayerService.playerPositionUpdate(p));
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
                                GameService.writeResponse(sessionHandler.writer, PlayerService.playerPositionUpdate(p));
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
                        String message = +201 + " " + player.playerName + " left the Game";
                        GameService.writeResponse(s.writer, message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        session.interrupt();
    }
}