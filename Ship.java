import java.util.Arrays;

/**
 * The purpose of this class is to 
 * create ships in a game of battleship.
 */

public class Ship {
	
	// The number of hits this ship
	// has sustained
	int numberOfHits = 0;
	
	// Status of the ship. True if 
	// afloat, false if sunk.
	boolean aFloat = true;
	
	// The positions on the ocean
	// that the ship takes up
	int[][] positions;
	
	// The length of the ship
	int length;
	
	// A reference to an ocean object
	Ocean ocean;
	
	/**
	 * The constructor of this class.
	 * It creates the object and initializes
	 * an array of the ship's positions and its
	 * length.
	 * 
	 * @param   positions   a 2D int array of the 
	 *                      ship's positions on
	 *                      the ocean
	 * @param   length      the length of the ship
	 */
	public Ship ( int[][] positions, int length, char boatMarker, Ocean ocean ) {
		this.positions = positions;
		this.length = length;
		this.ocean = ocean;
		
		// Record the marker for the ship
		// in the game board
		ocean.boatCharacter(boatMarker);
		
		// Loop over the positions and put the ship on the
		// game board
		for( int outer = 0; outer < length; outer++ ) {
				ocean.setMark( positions[outer][0],
						positions[outer][1], boatMarker);
		}
	}
	
	/**
	 * The purpose of this method is to see 
	 * whether the given coordinates of the
	 * incoming attack will hit or miss the
	 * ship.
	 * @param   row      the row position
	 *                   of the attack
	 * @param   column   the column position
	 *                   of the attack
	 */
	public boolean hitOrMiss( int row, int column, char hitMark ) {
		
		// Put the coordinates into
		// a int array for comparison
		int[] target = { row, column };
		
		// True if we found a hit
		boolean hit = false;
		
		// Loop over the different coordinates
		// in the 2D positions array.
		for( int outer = 0; outer < length; outer++ ) {
			
			// Check to see if the ship is afloat and 
			// the target matches
			if( aFloat && Arrays.equals( target, positions[outer] ) ) {
				ocean.setMark( row, column, hitMark );
				numberOfHits++;
				hit = true;
			}
		}
		
		// If we didn't find a hit,
		// it's a miss.
		if( !hit ) {
			
			// Put a miss mark on the board
			ocean.setMark( row, column, '*' );
			return false;
		}
		
		else {
			
			// Check to see whether the ship
			// is sunk.
			if ( numberOfHits == length ) {
				aFloat = false;
				System.out.println( "You sank my " + name(length) + "!" );
			}
			
			return true;
		}
	}
	
	/**
	 * This method returns the status
	 * of the ship, given by the 
	 * boolean variable aFloat.
	 * @return   boolean   returns the value
	 *                     of the boolean 
	 *                     variable aFloat.
	 */
	public boolean sunkOrAfloat() {
		return aFloat;
	}
	
	/**
	 * This method returns the class of ship
	 * that corresponds to the given length.
	 * @param   length   the length of the
	 *                   ship
	 * @return  String   the class of ship
	 */
	private String name( int length ) {
		
		switch( length ) {
			
			case 2: return "Destroyer";
			
			case 3: return "Cruiser";
			
			case 4: return "Battleship";
			
			case 5: return "Carrier";
			
			default:
				return "Nemo";
		}
	}
}