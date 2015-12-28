
Name: Smita Sukhadeve
Assignment: Project 3 - Game Server Part 1
Course: 540
Language Used: Java

Compilation: require JDK 1.7/1.8
Steps to compile and execute the program:
-Please change the configuration parameter as required. Currently, the port is set to 5555, row and column set to 128.

If running on linux:
cd project3/TossTheCookie/src and
run  ./run-me.sh

Or Import project in IntelJ/Eclipse IDE, compile the module and run the GameServer. Please, change the property file
path if required.

Synopsis:
The submitted project consists of following programs:

1.Player
2.PlayerCookie
3.PlayerPosition

Above are POJO classes that define the Player attributes: playerName, their position on the map, etc.

4.PlayerSessionHandler
GameServer creates the PlayerSessionHandler thread, after accepting the connection request from the player/client.
This class calls the required method from PlayerService and GameService to handle the player requests such as login, move,
throw, msg, quit, etc. It parses the request and call appropriate method based on the request type.

5.PlayerService
This class contains methods for handling player requests, updating the 'GameMap' and 'PlayerMap' based on the Players
move, throw. It constitutes the important methods such as:

6.GameServer:
This is the main class that takes care of creating the listening port, accepting the Player's request and creating
the player thread.

7.GameService:
This class takes care of wrapping the server behavior for creating the 'GameMap', sending the initial map, updating the
player etc. It's container for all the global methods.

8.GameResource:
This is the wrapper for all static resources shared across the multiple Player threads.
gameMap: It's two-dimensional array. As of now I am only using 0's and 1's.
O: represent the position where player can move
1: represent the position where player can't move
5: represent the occupied position
playerMap: It is Map of PlayerPosition and the Player to maintains the current state of the players.

9.ActivePlayerSession
This class maintains the list of all active Sessions and their behaviors.

10. configPropRoot.properties: Configure the parameters, i.e., port number and max rows/column for gameMap.

For this assignment, I mostly concentrated on implementing the protocol correctly, handling concurrency, etc. The
implemented GameLogic is fairly simple. But, I struggled while handling the multiple threads and also with the game
mechanics.  I initially thought of sending the map as 'Fog of war' but later decided to send entire map initially as
that was getting complicated.

Currently, My GameServer supports the basic requests from the Player:
1.login/l
2.move/m
3.throw/t
4.quit/q
5.msg all/player/map
8.Periodic update for the move and throw

Server GameLogic:
1. Player connects to the server and server sends the initial map details with status codes:
200: map size info
102: Initial map
2: login playerName: First, server checks if the request is valid or not and then check whether the playerName exists
or not.
400 invalid Message/login
100: welcome with player name shows successful login
102 message shows assigned position
3.Move: for every move, checks valid move based on the if the place is preoccupied/blocker and broadCast the 102 message
to all.
4.Throw: '103 message' is sent to  all that indicates the thrown cookie. If if hits any player, the update with code 102
sent to the player who threw cookie and player who hit by the cookie with new cookie count.
5.The player can ask for periodic map /player updates using the 'msg' command and quit the game. All are informed
about this using status code '201'.


