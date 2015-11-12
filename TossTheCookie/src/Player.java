import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by smitatm on 10/27/15.
 */

public class Player {

    String playerName;
    //  int cookies;
    PlayerPosition position;
    ArrayList<PlayerCookie> cookies = new ArrayList<>();

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

    public void setPosition(PlayerPosition position) {
        this.position = position;
    }


    public boolean movePlayer(String direction, HashMap<PlayerPosition, Player> playerMap, int gameMap[][], int dimension, Player player) {

        int currentRow = player.getPosition().getRow();
        int currentColumn = player.getPosition().getColumn();
        boolean value = false;

        if (direction.equals("UP") || direction.equals("U")) {
            if (currentRow - 1 < 0 && gameMap[dimension - 1][currentColumn] != 5) {
                synchronized (gameMap) {
                    gameMap[dimension - 1][currentColumn] = 5;
                    synchronized (playerMap) {
                        playerMap.remove(player.position);
                        player.position.setRow(dimension - 1);
                        playerMap.put(position, player);
                    }
                    value = true;
                }
            } else if (currentRow - 1 > 0 && gameMap[currentRow - 1][currentColumn] != 5) {
                synchronized (gameMap) {
                    gameMap[currentRow - 1][currentColumn] = 5;
                    synchronized (playerMap) {
                        playerMap.remove(player.position);
                        player.position.setRow(currentRow - 1);
                        playerMap.put(position, player);
                    }
                    value = true;
                }

            }


        } else if (direction.equals("DOWN") || direction.equals("D")) {
            if (currentRow + 1 > dimension - 1 && gameMap[0][currentColumn] != 5) {
                synchronized (gameMap) {
                    gameMap[0][currentColumn] = 5;
                    currentRow = 0;
                    value = true;
                }
            } else if (currentRow + 1 < dimension - 1 && gameMap[currentRow + 1][currentColumn] != 5) {
                synchronized (gameMap) {
                    gameMap[currentRow + 1][currentColumn] = 5;
                    synchronized (playerMap) {
                        playerMap.remove(player.position);
                        player.position.setRow(currentRow + 1);
                        playerMap.put(position, player);
                    }
                    value = true;
                }

            }

        } else if (direction.equals("LEFT") || direction.equals("L")) {
            if (currentColumn - 1 < 0 && gameMap[currentRow][dimension - 1] != 5) {
                synchronized (gameMap) {
                    gameMap[currentRow][dimension - 1] = 5;
                    synchronized (playerMap) {
                        playerMap.remove(player.position);
                        player.position.setColumn(dimension - 1);
                        playerMap.put(position, player);
                    }
                    value = true;
                }
            } else if (currentColumn - 1 > 0 && gameMap[currentRow][currentColumn - 1] != 5) {
                synchronized (gameMap) {
                    gameMap[currentRow][currentColumn - 1] = 5;
                    synchronized (playerMap) {
                        playerMap.remove(player.position);
                        player.position.setColumn(currentColumn - 1);
                        playerMap.put(position, player);
                    }
                    value = true;
                }

            }
        } else if (direction.equals("RIGHT") || direction.equals("R")) {

            if (currentColumn + 1 > dimension - 1 && gameMap[currentRow][0] != 5) {
                synchronized (gameMap) {
                    gameMap[currentRow][0] = 5;
                    synchronized (playerMap) {
                        playerMap.remove(player.position);
                        player.position.setColumn(0);
                        playerMap.put(position, player);
                    }

                    value = true;
                }
            } else if (currentColumn + 1 < dimension - 1 && gameMap[currentRow][currentColumn + 1] != 5) {
                synchronized (gameMap) {
                    gameMap[currentRow][currentColumn + 1] = 5;
                    synchronized (playerMap) {
                        playerMap.remove(player.position);
                        player.position.setColumn(currentColumn + 1);
                        playerMap.put(position, player);
                    }
                    value = true;
                }

            }

        } else {
            value = false;
        }

        return value;
    }

    public boolean throwCookie(Player player, int gameMap[][], String direction, int dimension, HashMap playerMap, PlayerCookie cookie) {
        int currentRow = player.getPosition().getRow();
        int currentColumn = player.getPosition().getColumn();
        boolean value = false;

        if (direction.equals("UP") || direction.equals("U")) {
            if (currentRow - 1 < 0 && gameMap[dimension - 1][currentColumn] == 5) {
                PlayerPosition hitPosition = new PlayerPosition();
                hitPosition.setRow(dimension - 1);
                hitPosition.setColumn(currentColumn);
                Player playerHit = (Player) playerMap.get(hitPosition);
                synchronized (player) {
                    player.getCookies().remove(cookie);
                }
                synchronized (playerHit) {
                    playerHit.getCookies().add(cookie);
                }
            } else if (currentRow - 1 > 0 && gameMap[currentRow - 1][currentColumn] == 5) {
                PlayerPosition hitPosition = new PlayerPosition();
                hitPosition.setRow(currentRow - 1);
                hitPosition.setColumn(currentColumn);
                Player playerHit = (Player) playerMap.get(hitPosition);
                synchronized (player) {
                    player.getCookies().remove(cookie);
                }
                synchronized (playerHit) {
                    playerHit.getCookies().add(cookie);
                }

            }
        } else if (direction.equals("DOWN") || direction.equals("D")) {
            if (currentRow + 1 > dimension - 1 && gameMap[0][currentColumn] == 5) {
                PlayerPosition hitPosition = new PlayerPosition();
                hitPosition.setRow(0);
                hitPosition.setColumn(currentColumn);
                Player playerHit = (Player) playerMap.get(hitPosition);
                synchronized (player) {
                    player.getCookies().remove(cookie);
                }
                synchronized (playerHit) {
                    playerHit.getCookies().add(cookie);
                }
            } else if (currentRow + 1 < dimension - 1 && gameMap[currentRow + 1][currentColumn] == 5) {
                PlayerPosition hitPosition = new PlayerPosition();
                hitPosition.setRow(currentRow + 1);
                hitPosition.setColumn(currentColumn);
                Player playerHit = (Player) playerMap.get(hitPosition);
                synchronized (player) {
                    player.getCookies().remove(cookie);
                }
                synchronized (playerHit) {
                    playerHit.getCookies().add(cookie);
                }
            }
        } else if (direction.equals("LEFT") || direction.equals("L")) {
            if (currentColumn - 1 < 0 && gameMap[currentRow][dimension - 1] == 5) {
                PlayerPosition hitPosition = new PlayerPosition();
                hitPosition.setRow(currentRow);
                hitPosition.setColumn(dimension - 1);
                Player playerHit = (Player) playerMap.get(hitPosition);
                synchronized (player) {
                    player.getCookies().remove(cookie);
                }
                synchronized (playerHit) {
                    playerHit.getCookies().add(cookie);
                }

            } else if (currentColumn - 1 > 0 && gameMap[currentRow][currentColumn - 1] == 5) {
                PlayerPosition hitPosition = new PlayerPosition();
                hitPosition.setRow(currentRow);
                hitPosition.setColumn(currentColumn - 1);
                Player playerHit = (Player) playerMap.get(hitPosition);
                synchronized (player) {
                    player.getCookies().remove(cookie);
                }
                synchronized (playerHit) {
                    playerHit.getCookies().add(cookie);
                }

            }
        } else if (direction.equals("RIGHT") || direction.equals("R")) {

            if (currentColumn + 1 > dimension - 1 && gameMap[currentRow][0] == 5) {
                PlayerPosition hitPosition = new PlayerPosition();
                hitPosition.setRow(currentRow);
                hitPosition.setColumn(0);
                Player playerHit = (Player) playerMap.get(hitPosition);
                synchronized (player) {
                    player.getCookies().remove(cookie);
                }
                synchronized (playerHit) {
                    playerHit.getCookies().add(cookie);
                }

            } else if (currentColumn + 1 < dimension - 1 && gameMap[currentRow][currentColumn + 1] == 5) {
                PlayerPosition hitPosition = new PlayerPosition();
                hitPosition.setRow(currentRow);
                hitPosition.setColumn(currentColumn + 1);
                Player playerHit = (Player) playerMap.get(hitPosition);
                synchronized (player) {
                    player.getCookies().remove(cookie);
                }
                synchronized (playerHit) {
                    playerHit.getCookies().add(cookie);
                }
            }

        } else {
            value = false;
        }
        return value;
    }


    public static class PlayerPosition {
        int row;
        int column;

        public int getRow() {
            return row;
        }

        public void setRow(int row) {
            this.row = row;
        }

        public int getColumn() {
            return column;
        }

        public void setColumn(int column) {
            this.column = column;
        }
    }

    public static class PlayerCookie {
        int cookieSpeed;

        public int getCookieSpeed() {
            return cookieSpeed;
        }

        public void setCookieSpeed(int cookieSpeed) {
            this.cookieSpeed = cookieSpeed;
        }
    }
}