import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;

/**
 * Created by smitatm on 11/11/15.
 */
public class GameService {

    Socket clientSocket;

    public GameService(Socket clientSocket) {
        this.clientSocket = clientSocket;
        OutputStream outputStream = null;
        try {
            outputStream = clientSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Writer writer = new OutputStreamWriter(outputStream);
    }

    public GameService() {

    }

    public static void writeResponse(Writer writer, int responseCode, String message) throws IOException {

        try {
            writer.write(responseCode + message + "\r\n");
            writer.flush();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public static void sendInitialMap(int dimension,int gameMap[][],Socket socket) {
        GameService game = new GameService();
        OutputStream outputStream =null;
        try {
           outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Writer writer = new OutputStreamWriter(outputStream);
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                int value = gameMap[i][j];
                String message = +i +", "+j+", "+ value +"\r\n ";
                try {
                    game.writeResponse(writer, 200, message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static void sendMapUpdate(int gameMap[][],Socket socket, Player player) {

        GameService game = new GameService();
        OutputStream outputStream =null;
        try {
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Writer writer = new OutputStreamWriter(outputStream);
        int x= player.position.getRow();
        int y=  player.position.getColumn();


        for (int i=x-5; i < gameMap.length; i++) {
            for (int j = y-5; j < gameMap.length; j++) {
                int value = gameMap[i][j];
                String message = +i +", "+j+", "+ value +"\r\n ";
                try {
                    game.writeResponse(writer, 102, message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
