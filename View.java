import java.io.Serializable;
import java.util.Scanner;

/**
 * This class is used to display
 * information about the game to the
 * player and to acquire user input.
 *
 */
@SuppressWarnings("serial")
public class View implements Serializable{
	
	// The Scanner object. It is used to get input
	// from the player
	private Scanner scan = new Scanner( System.in );
	
	// The names of different classes of boats
	private final String[] boatNames = { "Patrol", "Destroyer", "Battleship", "Carrier" };
	
	/**
	 * The purpose of this method is to get 
	 * and check the user's input for valid 
	 * types (int for positions, and String
	 * for direction).
	 * @param   playerName   the name of the player
	 * @param   index        the type of ship under
	 *                       consideration
	 * @return  Object[]     An array of type Object
	 *                       to hold the two coordinate
	 *                       integers and the String
	 *                       indicating direction
	 */
	public Object[] getPositions( String playerName, int index ) {
		
		// The Object array. The array is of type Object
		// to hold the row and column integer coordinates
		// and the String indicating direction
		Object[] inputs = new Object[3];
		
		// So long as the input is not valid, 
		// this will remain false
		boolean valid = false;
		
		// Position in the Object array where we
		// are placing valid inputs
		int spot = 0;
		
		// Ask user for starting location and direction of
		// a ship
		System.out.println( playerName + ", choose a starting row and column position for the " +
				boatNames[index] + " length ship and in which direction it shall point." );
	
		// Continue asking until the user gives an input
		// of the correct types ( integer, integer, and String).
		while( !valid ) {
			
			// Check the user's row and column inputs
			if( spot != 2 && scan.hasNextInt() ) {
				inputs[spot] = scan.nextInt();
				spot++;
			}
			
			// Check the user's direction input
			else if ( spot == 2 && scan.hasNext() ) {
				inputs[spot] = scan.next();
				valid = true;
			}
			
			// If any one input is wrong, tell the user so
			else {
				System.out.println( "Invalid input" );
				spot = 0;
				scan.next();
			}
		}
		return inputs;
	}
	
	/**
	 * This method gets user input for their desired target
	 * @param    playerName   The player whose turn it is
	 * @return   int[]        The coordinates the player
	 *                        is attacking.
	 */
	public int[] getInput( String playerName ) {
		
		// The coordinates the player is targeting
		int[] target = new int[2];
		
		// Ask the player for a target
		System.out.println( playerName + ", choose a position to target! (ex. row column)" );
		
		// So long as the input is not valid,
		// this will be false
		boolean valid = false;
		
		// Position in the target array
		int index = 0;
		
		// So long as the input is not valid,
		// keep asking
		while ( !valid ) {
			
			// Check the input for row
			if( index == 0 && scan.hasNextInt() ) {
				target[index] = scan.nextInt();
				index++;
			}
			
			// Check the input for column
			else if( index == 1 && scan.hasNextInt() ) {
				target[index] = scan.nextInt();
				valid = true;
			}
			
			// If either input is not an int, try again.
			else {
				System.out.println( "Not a valid input! Must be a pair of numbers!" );
				index = 0;
			}
		}
		return target;
	}
	
	/**
	 * A simple method for printing to the console.
	 * @param   message   the string to be printed
	 */
	public void message( String message ) {
		System.out.println( message );
	}
}