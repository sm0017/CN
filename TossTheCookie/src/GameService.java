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
                }
                // This indicate the magic location. for example if maxRow=10 and maxcolumn =10 then i=5 and j=5
                // map[5][5]= magic location = 8
                else if(i==maxRow/2 && j==maxColumn/2){
                    map[i][j]=8;
                }
                else {
                    map[i][j] = 0;
                }
            }
        }
    }

    // The gameMap is sent when player connect to the server. The map is sent with the status code 102
    public static void sendInitialMap(int maxRow,int maxColumn,int gameMap[][],Writer writer)
    {
        for (int i = 0; i < maxRow; i++) {
            StringBuilder message= new StringBuilder(maxRow+20);
            message.append("102 ");
            for (int j = 0; j < maxColumn; j++)
            {
                if(j==maxColumn-1){
                    int value = gameMap[i][j];
                    message.append(+i +", "+j+", "+ value);
                }else {
                    int value = gameMap[i][j];
                    message.append(+i +", "+j+", "+ value+", ");
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

    public static void brodCastMessage(String message,GameResource gameResource) {
        synchronized (gameResource.activePlayerSessions) {
            Iterator<PlayerSessionHandler> active = gameResource.activePlayerSessions.iterator();
            while (active.hasNext()) {
                PlayerSessionHandler session = active.next();
                if (!session.isAlive()) {
                    active.remove();
                    session.interrupt();
                } else {
                    try {
                        writeResponse(session.writer,message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

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
                        writeResponse(session.writer, PlayerService.playerUpdateOnMove(p));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void invalidCommand(PrintWriter writer){
        String message = 400 + " Invalid Command";
        try {
            GameService.writeResponse(writer, message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
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


    public  static  void existingPlayerUpdate(GameResource gameResource, PlayerSessionHandler sessionHandler) {
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
                            GameService.writeResponse(sessionHandler.writer, PlayerService.playerUpdateOnMove(p));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    public  static  boolean  checkIfMagicMove(Player player, String request, int maxRow, int maxColumn){
        boolean magicMove= false;
        int currentRow = player.getPosition().row;
        int currentColumn = player.getPosition().column;
        if (request.equals("u")){
          if (currentRow!=0 && currentRow -1 == maxRow/2 && currentColumn== maxColumn/2){
              magicMove= true;
          }

        }else if (request.equals("d") ){
            if (currentRow!=0 && currentRow +1 == maxRow/2 && currentColumn== maxColumn/2){
                magicMove= true;
            }

        }else  if (request.equals("l")){
            if (currentColumn!=0 && currentRow == maxRow/2 && currentColumn-1== maxColumn/2){
                magicMove= true;
            }
        }   else if (request.equals("r")){
            if (currentColumn!=0 && currentRow == maxRow/2 && currentColumn+1== maxColumn/2){
                magicMove= true;
            }
        }
   return magicMove;
    }
}
