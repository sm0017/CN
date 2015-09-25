import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/*
 *  Smita Sukhadeve
 *  Project1 :NumberGuessing Server
 *
 */

public class NumberGuessingServer
{
   public static void main(String[] args){

       //Initialize all variables
       ServerSocket serverSocket = null;
       Socket clientSocket = null;
       int randomNumber;
       int guess;
       int noOfGuesses = 0;

       try{

           //register service at port 5555
           serverSocket = new ServerSocket(5555);
           System.out.println("Server is listening on localhost at Port 5555");


           while (true) {


               boolean correct = false;
               String result;
               String message;

               try {
                   //Listen and accept the client request

                   clientSocket = serverSocket.accept();
               }catch (IOException e) {
                   e.printStackTrace();
               }
               GameLogic gameLogic = new GameLogic();

               //generate random number
               randomNumber = gameLogic.generateRandomNumber(1, 100);

               //create output stream to write client response

               PrintWriter writeClientResponse= new PrintWriter(clientSocket.getOutputStream(), true);


               writeClientResponse.println("+ Hello, I am thinking of a number between 1 and 100. Can you guess it?");
               writeClientResponse.println("X Enter 0 to exit the game.");

               System.out.println("Server generated number" + randomNumber);

               //to read input stream
               Scanner readClientGuess = new Scanner(clientSocket.getInputStream());


               do {
                   noOfGuesses++;
                   guess = readClientGuess.nextInt();


                   System.out.println("client entered the guess :" + guess);

                   result=gameLogic.checkGuess(guess, randomNumber);
                   message=gameLogic.displayMessage(result,noOfGuesses);
                   writeClientResponse.println(message);
                    if (result=="*"||result=="X"){
                    break;
                    }

               } while (correct == false);


               clientSocket.close();

           }


       }
       catch(IOException e) {e.printStackTrace();}

   }





}


