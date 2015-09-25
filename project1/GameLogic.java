import java.util.Random;

/**
 * Created by smita
 */
public class GameLogic {
    int enteredGuess;
    int randomNumber;
    String result;

  //compare two Number
    public String checkGuess(int enteredGuess, int randomNumber) {

        if (enteredGuess < randomNumber && enteredGuess > 0) {
            result = "<";

        } else if (enteredGuess > randomNumber && enteredGuess < 100) {
            result = ">";

        } else if (enteredGuess < 0 || enteredGuess > 100) {
            result = "!";
        } else if (enteredGuess == randomNumber) {
            result = "*";

        } else if(enteredGuess==0){
            result = "X";
        }
        return result;
    }
//Display message
    public String displayMessage(String result, int count)
    {
        String message=null;
        switch (result)
        {


            case "<":
                message = "> My Number is Higher";
                break;
            case ">":
                message = "< My Number is Lower";
                break;
            case "!":
                message = "! Invalid input, please enter only numbers between 1 and 100?";
                break;
            case "*":
                message = "* That’s it. Good job. It took you " +count +" guesses. Thanks for playing";
                break;
            case "X":
                message = "X Better Luck next time!";
                break;

        }
        return  message;
    }
//Generate random Number
    public int generateRandomNumber(int start, int end)
    {
        int randomNumber;
        Random generateRandomNumber = new Random();
        randomNumber = generateRandomNumber.nextInt(end-start+1)+ start ;
        return randomNumber;
    }
}
