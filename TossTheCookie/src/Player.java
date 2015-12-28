import java.util.ArrayList;

/**
 * Represent the Player
 */

public class Player {
    String playerName;
    PlayerPosition position;
    ArrayList<PlayerCookie> cookies = new ArrayList<>();
    boolean movedToMagicPlace;

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public ArrayList<PlayerCookie> getCookies() {
        return cookies;
    }

    public void setCookies(ArrayList<PlayerCookie> cookies) {
        this.cookies = cookies;
    }

    public PlayerPosition getPosition() {
        return position;
    }

    public boolean isMovedToMagicPlace() {
        return movedToMagicPlace;
    }

    public void setMovedToMagicPlace(boolean movedToMagicPlace) {
        this.movedToMagicPlace = movedToMagicPlace;
    }

    public void setPosition(PlayerPosition position) {
        this.position = position;
    }

}
