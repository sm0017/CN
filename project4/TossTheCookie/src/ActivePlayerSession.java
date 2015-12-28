import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * This class is to maintain the list of all active player sessions is used while broadcasting
 * the messages to all the players.
 *
 */
public class ActivePlayerSession {

    private static final Logger logger = Logger.getLogger(GameServer.class.getName());

    private Collection<PlayerSessionHandler> sessionList = new ArrayList<PlayerSessionHandler>();

    public synchronized void addSession(PlayerSessionHandler s) {
        sessionList.add(s);
    }

    public Iterator<PlayerSessionHandler> iterator() {
        return sessionList.iterator();
    }

    public synchronized void removeSession(PlayerSessionHandler session) {
        logger.info("Some player left the game" + session);
        sessionList.remove(session);
    }

}


