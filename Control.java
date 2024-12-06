/**
 * This class contains the logic for
 * a game of battle ship using the 
 * MVC design
 *
 */
public class Control {
	
	// A View object for interacting
	// with the players
	View view;
	
	// A Model object for keeping track
	// of the state and basic rules
	Model model;
	
	// Player objects
	Player[] players = new Player[2];
	
	// Whose turn it is
	int turn = 0;
	int sign = 1;

	/**
	 * The constructor. It initializes the
	 * View, Model, and Player objects.
	 */
	public Control(){
		view = new View();
		model = new Model();
		
		// Player objects
		players[0] = new Player ( "player a", 'A', 'a' );
		players[1] = new Player ( "player b", 'B', 'b' );
	}
	
	/**
	 * This method sets up the model and the
	 * fleets for each player.
	 */
	public void controlSetUp(){
		model.modelSetUp( players[0].shipMark, players[1].shipMark);
		fleetSetUp(0);
		fleetSetUp(1);	
	}
	
	/** 
	 * This method creates a fleet 
	 * of ships for a game of
	 * battle ship according to user
	 * input. However, ships can not
	 * touch or overlap.
	 */
	private void fleetSetUp( int player ) {
		
		// The input row
		int row = 0;
		
		// The input column
		int column = 0;
		
		// The input direction
		String direction = "";
		
		// Index in the ships array where we
		// will place the new ship
		int index = 0;
		
		// Continue until the user has filled the 
		// array of ships
		while( index != 4 ) {
			
			// Array of Objects to hold the returned
			// integer coordinates and String direction
			Object[] inputs = view.getPositions( players[player].playerName, index );
			row = (Integer) inputs[0];
			column = (Integer) inputs[1];
			direction = (String) inputs[2];
			
			// If the ship can be built, increment index
			if( model.buildShip( player, row, column, direction ) ) {
				index++;
			}
		}		
	}
	
	/**
	 * The purpose of this method is to manage a game
	 * of battle ship.
	 */
	public void playGameWith(  ) {
		
		// Set first player
		Player current = players[0];
		
		// Set the player waiting for their turn.
		Player waiting = players[1];
		
		// Used for when the current player changes
		Player temp;
		
		// So long as either player has at least one ship still
		// afloat, the game continues
		while( model.checkStatus() ) {
			
			// Print the current player's ocean with their ships
			view.message( "Your ocean:" );
			view.message( model.printOcean( turn, true ) );
			
			// Print the waiting player's ocean without their
			// ships. This is so that the guessing player
			// can see their hits and misses.
			view.message( "Your guesses:" );
			view.message( model.printOcean( turn + sign, false ) );
			
			// Ask the current player where they want to attack
			int[] target = view.getInput( players[turn].playerName );
			
			// Pass the target to the waiting player's checkHit()
			// method. If it returns true, then it hit, otherwise
			// it is a miss.
			if( model.checkHit( turn + sign, target[0], target[1], players[turn].hitMark ) ) {
				view.message( "Hit!" );
			}
			else {
				view.message( "Miss!" );
			}
			
			// Switch current and waiting players
			temp = current;
			current = waiting;
			waiting = temp;
			
			// An arbitrary border between the two players' turns. 
		    view.message( "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~" );
			
		    // Update the turn
			turn += sign;
			sign *= -1;
		}
		
		// Find out who the winner is
		int winner = model.victory();
		view.message( players[winner].playerName + " has won!" );
	
	}
	
	/**
	 * The main method. It creates the control object,
	 * sets up a game of battle ship, and starts the game.
	 * @param   args   command line arguments ( not used )
	 */
	public static void main( String [] args ) {
		Control control = new Control();
		control.controlSetUp();
		control.playGameWith();
	}
}