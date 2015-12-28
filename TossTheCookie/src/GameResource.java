import java.util.ArrayList;
import java.util.HashMap;

/**
 *Wrapper for all Global Resources
 */

public class GameResource {
    HashMap<PlayerPosition, Player> playerMap;
    int[][] gameMap;
    ActivePlayerSession activePlayerSessions;
    int maxRow, maxColumn;
    int randomNumber;
    int listeningPort;
    ArrayList<Player> magicPlayers;

    public ArrayList<Player> getMagicPlayers() {
        return magicPlayers;
    }

    public void setMagicPlayers(ArrayList<Player> magicPlayers) {
        this.magicPlayers = magicPlayers;
    }

    public void setPlayerMap(HashMap<PlayerPosition, Player> playerMap) {
        this.playerMap = playerMap;
    }

    public void setGameMap(int[][] gameMap) {
        this.gameMap = gameMap;
    }

    public void setActivePlayerSession(ActivePlayerSession activePlayerSessions) {
        this.activePlayerSessions = activePlayerSessions;
    }

    public int getListeningPort() {
        return listeningPort;
    }

    public ActivePlayerSession getActivePlayerSessions() {
        return activePlayerSessions;
    }

    public void setListeningPort(int listeningPort) {
        this.listeningPort = listeningPort;
    }

    public void setMaxRow(int maxRow) {
        this.maxRow = maxRow;
    }

    public void setMaxColumn(int maxColumn) {
        this.maxColumn = maxColumn;
    }

    public void setRandomNumber(int randomNumber) {
        this.randomNumber = randomNumber;
    }
}
