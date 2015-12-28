import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Iterator;

/**
 * Wrapper for Global methods
 */
public class GameService {
    public GameService() {

    }

    //gameMap[][]=0 ==> PlayerCanMove || gameMap[][]=1 ==> playerCan'tMove || gameMap[][]=5 ==> afterPlayerMoved
    public static void generateGameMap(int maxRow, int maxColumn, int map[][]) {
        for (int i = 0; i < maxRow; i++) {
            for (int j = 0; j < maxColumn; j++) {
                if (i % 6 == 0 && j % 6 == 0 || i % 20 == 0 && j % 20 == 0) {
                    map[i][j] = 1;
                } else if (i == maxRow / 2 && j == maxColumn / 2) {
                    map[i][j] = 8;
                } else {
                    map[i][j] = 0;
                }
            }
        }
    }

    // The gameMap is sent when player login
    // The message is sent with  status code 102, x,y, value .....EOL
    public static void sendInitialMap(int maxRow, int maxColumn, int gameMap[][], Writer writer) {
        for (int i = 0; i < maxRow; i++) {
            StringBuilder message = new StringBuilder(maxRow + 20);
            message.append("102 ");
            for (int j = 0; j < maxColumn; j++) {
                if (j == maxColumn - 1) {
                    int value = gameMap[i][j];
                    message.append(+i + ", " + j + ", " + value);
                } else {
                    int value = gameMap[i][j];
                    message.append(+i + ", " + j + ", " + value + ", ");
                }
            }
            try {
                writeResponse(writer, message.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // generic method for writing different message based on the player request
    public static void writeResponse(Writer writer, String message) throws IOException {
        writer.write(message + "\r\n");
        writer.flush();
    }


    // The method is broadcasting the messages to all the player

    public static void brodCastMessage(String message, GameResource gameResource) {
        synchronized (gameResource.activePlayerSessions) {
            Iterator<PlayerSessionHandler> active = gameResource.activePlayerSessions.iterator();
            while (active.hasNext()) {
                PlayerSessionHandler session = active.next();
                if (!session.isAlive()) {
                    active.remove();
                    session.interrupt();
                } else {
                    try {
                        writeResponse(session.writer, message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // The method writes the message to all players when cookie is thrown
    public static void updateOnCookieThrow(GameResource gameResource) {
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
                        writeResponse(session.writer, PlayerService.playerPositionUpdate(p));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    //Message for invalid command entered by the player
    public static void invalidCommand(PrintWriter writer) {
        String message = 300 + " Invalid Command";
        try {
            GameService.writeResponse(writer, message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // To close IOs
    public static void close(Closeable... closeables) {
        for (Closeable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (IOException e) {

                }
            }
        }
    }

    // This method is to update the player about the existing players when they login
    public static void existingPlayerUpdate(GameResource gameResource, PlayerSessionHandler sessionHandler) {
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
                        if (!p.playerName.equals(sessionHandler.getPlayer())) {
                            GameService.writeResponse(sessionHandler.writer, PlayerService.playerPositionUpdate(p));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }


    // This method check if the player move will lead him to the magic location
    //The magic location is fixed and only one position which is middle position in the
    // The game board
    public static boolean checkIfMagicMove(Player player, String request1, String request2, int maxRow, int maxColumn) {
        boolean magicMove = false;
        int currentRow = player.getPosition().row;
        int currentColumn = player.getPosition().column;
        if (request1.equals("move") || request1.equals("m")) {
            if (request2.equals("u") || request2.equals("up")) {
                if (currentRow != 0 && currentRow - 1 == maxRow / 2 && currentColumn == maxColumn / 2) {
                    magicMove = true;
                }

            } else if (request2.equals("d") || request2.equals("down")) {
                if (currentRow != 0 && currentRow + 1 == maxRow / 2 && currentColumn == maxColumn / 2) {
                    magicMove = true;
                }

            } else if ((request2.equals("l")) || request2.equals("left")) {
                if (currentColumn != 0 && currentRow == maxRow / 2 && currentColumn - 1 == maxColumn / 2) {
                    magicMove = true;
                }
            } else if ((request2.equals("r")) || request2.equals("right")) {
                if (currentColumn != 0 && currentRow == maxRow / 2 && currentColumn + 1 == maxColumn / 2) {
                    magicMove = true;
                }
            }
        } else {
            magicMove = false;
        }
        return magicMove;
    }

    public static int checkValidInSize(String playerInput) {
        int validInput = 0;
        if (null == playerInput) {
            validInput = 0;

        } else {
            String requestType[] = playerInput.split("\\s");
            if (requestType.length == 0 || requestType.length > 2) {
                validInput = 0;
            } else if (requestType.length == 2) {
                validInput = 2;
            } else if (requestType.length == 1) {
                validInput = 1;
            }

        }
        return validInput;
    }

}
