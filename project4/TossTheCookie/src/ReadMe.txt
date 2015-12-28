Name: Smita Sukhadeve
Assignment: Project 3 - Game Server Part 1
Course: 540
Language Used: Java

Compilation: require JDK 1.7/1.8
Steps to compile and execute the program:
-You can configure following parameter in the in property file: Values reflect current setting.
portNumber= 5555
maxRow=10
maxColumn=10
remoteServerAddress = localhost
remotePort=6666
initialCookieCnt=3

you might want to change portNumber while running the remote server instance and also change remotePort accordingly.

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
Modification for 2: The new attribute movedToMagicPlace is assigned to a player to identify whether or not player moved to the magic place.

4.PlayerSessionHandler
GameServer creates the PlayerSessionHandler thread, after accepting the connection request from the player/client.
This class calls the required method from PlayerService and GameService to handle the player requests such as login, move,
throw, msg, quit, etc. It parses the request and call appropriate method based on the request type.
Modification: Now PlayerSessionHandler also handles the movement of player on the remote magic server. It creates the new Thread MagicLocation to handle magicPlayer game.

5.PlayerService
This class contains methods for handling player requests, updating the 'GameMap' and 'PlayerMap' based on the Players
A move, throw.
Modification:  Methods to handle new features and to solve the part 1 missing functionalities has been added.

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

11. MagicLocation
This class handles the proxy interaction between the player and remote server.  The various methods are added to maintain communication between player and the remote server.

For this assignment, I focused resolving previously missed functionalities and taking care of new features.

Took care of following comments from Project 3:
Map update gets sent before login, should be after login: Now the map is only sent after player logged in.
New players don’t get information about existing players after they login
Little to no code documentation

Currently, My GameServer supports the basic requests from the Player:
1.login/l
2.move/m
3.throw/t
4.quit/q
5.msg all/playerID/map
8.A Periodic update for the move and throw and fixed existing problems.
9.removes player after winning the game. Winner player can enter the ‘q’ and end the session.
10. Handle remote connection and player movement to remote server. But I struggled with handling the interaction between the remote server and player.

Server GameLogic:
1. Player connects to the server and server sends the initial map details with status codes:
200: map size info
2: login playerName: First, server checks if the request is valid or not and then check whether the playerName exists
or not.
After successful login player could see:
102 map
104 currentPlayer
104 existing players if any
100 welcome message

3.Move: for every move, checks valid move based on the if the place is preoccupied/blocker and broadCast the 200 ok /not ok message along with 104 player updates.

4.Throw: '103 message' is sent to all that indicates the thrown cookie. After throw processed,
105 message was reflecting cookie hit/miss condition as described in the specification. Status 104 to all player to reflect the correct cookie count after throw happened.
5.The player can ask for periodic map /player updates using the 'msg' command and quit the game.
msg all/player: give the players details
msg map: send the map



