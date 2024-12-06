/**
 * This class contains the state and basic
 * rules of a game of battle ship.
 */
public class Model {
	
	// The ocean objects, one for each player
	Ocean[] oceans = new Ocean[2];
	
	// The fleet objects, one for each player
	Fleet[] fleets = new Fleet[2];
	
	/**
	 * Set up the ocean and fleet objects and
	 * the hit marks for each player
	 * @param   playerA   the hit mark for player A
	 * @param   playerB   the hit mark for player B
	 */
	public void modelSetUp( char playerA, char playerB ){
		
		// Upper left coordinates of the
		// game board
		int minRow = 0;
		int minColumn = 0;
		
		// Lower right coordinates of the
		// game board
		int maxRow = 10;
		int maxColumn = 10; 
		
		// Ocean objects for both players
		oceans[0] = new Ocean(minRow, minColumn, maxRow, maxColumn);
		oceans[1] = new Ocean(minRow, minColumn, maxRow, maxColumn);
		
		// Fleet objects for both players
		fleets[0] = new Fleet( oceans[0], playerA );
		fleets[1] = new Fleet( oceans[1], playerB );
	}
	
	/**
	 * Attempt to build a ship at the given location
	 * @param    player      the numeric id of the player
	 *                       for whom a ship is being made
	 * @param    row         the row on the ocean board
	 *                       where the first point of this
	 *                       ship shall be placed
	 * @param    column      the column on the ocean board
	 *                       where the first point of this
	 *                       ship shall be placed
	 * @param    direction   the direction this ship will
	 *                       be oriented in
	 * @return   boolean     true if it can be built,
	 *                       false otherwise
	 */
	public boolean buildShip( int player, int row, int column, String direction ){
		return fleets[player].buildShip( row, column, direction );
	}
	
	/**
	 * The purpose of this method
	 * is to call the Fleet object's 
	 * checkHit() method to see if we
	 * have hit any of the ships.
	 * @param   row      The row that is
	 *                   being checked
	 * @param   column   The column that is
	 *                   being checked
	 * @return  boolean  If we hit this returns
	 *                   true, otherwise false
	 */
	public boolean checkHit( int player, int row, int column, char mark ) {
		if( oceans[player].checkOcean( row, column) ){
			return fleets[player].checkHit( row, column, mark );
		}
		else{
			return false;
		}
	}
	
	/**
	 * The purpose of this method is to
	 * see whether or not all of our ships
	 * have been sunk.
	 * @return   boolean   If true we still
	 *                     have ship, if false
	 *                     all of our ships
	 *                     have been sunk
	 */
	public boolean checkStatus() {
		return fleets[0].checkShips() && fleets[1].checkShips();
	}
	
	/**
	 * This method calls the Ocean class'
	 * printOcean method
	 * @param   yesOrNo   True if we want to print
	 *                    the ocean with the ships,
	 *                    false otherwise
	 */
	public String printOcean( int player, boolean yesOrNo ) {
		return oceans[player].printOcean(yesOrNo);
	}
	
	/**
	 * This method checks to see who
	 * has won the game.
	 * @return   int   the numeric id of the winner
	 */
	public int victory(){
		return fleets[0].checkShips() ? 0 : 1;
	}
}