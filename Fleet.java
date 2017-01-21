import java.util.Arrays;

/**
 * The purpose of this class is
 * to create and managed a fleet
 * of ships for a game of battleship
 * 
 * @author Kyle McGlynn
 * @author Ajinkya Kolhe
 */

public class Fleet {
	
	// The four lengths of the 
	// different ships used in 
	// a game of battle ship.
	final int[] lengths = {2, 3, 4, 5};
	
	// Position in the array of lengths
	int type = 0;
	
	// The array of ships
	Ship[] ships = new Ship[4];
	
	// A reference to an ocean
	// object
	Ocean ocean;
	
	// The ship marker
	char shipMark;
	
	/**
	 * The constructor, it creates the
	 * object and initializes the Ocean
	 * object.
	 * @param   ocean   an Ocean object
	 */
	public Fleet( Ocean ocean, char shipMark){
		this.ocean = ocean;
		this.shipMark = shipMark;
	}
	
	/**
	 * The purpose of this method is to check
	 * whether or not the ship can be built at
	 * the given position ( indicated by row
	 * and column ) or in the given direction
	 * ( indicated by rowColumn ).
	 * @param   row         the row where we
	 *                      want to build
	 * @param   column      the column where we 
	 *                      want to build
	 * @param   length      the length of the
	 *                      desired ship
	 * @param   rowColumn   the values by which
	 *                      we shall change row
	 *                      and column
	 * @return  boolean     true if we can,
	 *                      false otherwise
	 */
	private boolean checkBuild( int row, int column,
			int length, int[] rowColumn ) {
			
		// Was it a valid direction?
		if( rowColumn != null ) {
			
			// Set row and column changes
			int rowChange = rowColumn[0];
			int columnChange = rowColumn[1];
			
			// Controls how many positions
			// along the length we have checked
			int spot = 0;
			
			// If we it goes out of bounds,
			// this will be false;
			boolean inside = true;
			
			// Loop over the spots the ship may occupy
			while( spot < length && inside ) {
				
				// If it is within the bounds of the
				// game board, inside will remain true
				inside = ocean.checkOcean( row, column );
				
				// Put the position into an int array
				// for comparison with the locations
				// of the ships that are already on
				// the board.
				int[] location = { row, column };
				
				// Check to see if that location is already taken
				// Loop over the ships and then the positions of those
				// ships. At the same time, check to see if that spot 
				// is on the ocean.
				int shipNumber = 0;
				while( ships[shipNumber] != null && shipNumber < 4 && inside ) {
					
					// Loop over the positions taken by the selected ship
					for( int position = 0; position < ships[shipNumber].length; position++ ) {
						
						// If we still haven't found a position outside of the
						// ocean or taken by another ship, compare the current
						// position
						if( inside ) {
							
							// If the current position is already taken, then inside will 
							// go false and we shall exit all of the loops.
							inside = !Arrays.equals( ships[shipNumber].positions[position], location );
						}
					}
					
					// Look at the next ship already on the board
					shipNumber++;
				}
						
				// Alter the values of row and column
				// according to rowChange and columnChange
				row += rowChange;
				column += columnChange;
				
				// Move to the next spot
				spot++;
			}
		
			return inside;
		}
		
		// If the given direction is not valid
		else {
			return false;
		}		
	}
	
	/**
	 * This method does two things:
	 * First, it determines how row and 
	 * column should be altered for any
	 * given direction. Second, it determines
	 * whether the given direction is a 
	 * valid direction.
	 * @param   direction      a String indicating NESW
	 *                         or up, down, left, and right
	 * @return  int[] / null   int[] if a valid direction,
	 *                         null otherwise
	 */
	private int[] getDirection( String direction ) {
		
		// Depending on the direction, these 
		// values will either be 0, 1, or -1.
		int rowChange = 0;
		int columnChange = 0;
		
		// Determine which direction the ship shall
		// be built.
		if( direction.equalsIgnoreCase( "east" ) ||
				direction.equalsIgnoreCase( "right" ) ) {
			columnChange = 1;
		}
		
		else if ( direction.equalsIgnoreCase( "west" ) ||
				direction.equalsIgnoreCase( "left" ) ) {
			columnChange = -1;
		}
		
		else if ( direction.equalsIgnoreCase( "north" ) ||
				direction.equalsIgnoreCase( "up" ) ) {
			rowChange = -1;
		}
		
		else if ( direction.equalsIgnoreCase( "south" ) ||
				direction.equalsIgnoreCase( "down" ) ) {
			rowChange = 1;
		}
		
		else {
			
			// If the given direction is not NESW,
			// or up, down, left, or right,
			// then we can not build the boat since
			// that is not a valid direction.
			System.out.println( "Not a valid direction! Must be NESW or " +
					"up, down, left, right.");
			return null;
		}
		
		
		// Return the normal vector of the direction
		// as an int array.
		int[] dirValue = { rowChange, columnChange };
		
		return dirValue;
	}
	
	/**
	 * The purpose of this method is to call
	 * checkBuild to see if we can build a ship
	 * at the given spot, of the given length,
	 * and in the given direction. If we can it
	 * creates a ship according to these parameters.
	 * @param   row         the starting row we want
	 *                      to place the ship in
	 * @param   column      the starting column
	 *                      we want to place the ship in
	 * @param   length      the length of the ship
	 * @param   direction   the direction the ship
	 *                      is facing
	 * @return  boolean     true if it was built,
	 *                      false otherwise
	 */
	public boolean buildShip(int row, int column, String direction ) {
		
		// Determines the modification of row
		// and column according to direction
		int[] rowColumn = getDirection( direction );
		
		// If it can be built, do so
		if( checkBuild( row, column, lengths[type], rowColumn ) ) {
			
			// Index where we shall place the ship
			int index = 0;
			
			// Find the first, null value in the array
			// of ships
			while( ships[index] != null && index < ships.length ) {
				index++;
			}
			
			// Positions of the ship
			int[][] positions = new int[lengths[type]][];
			
			// Calculate the different positions
			for( int position = 0; position < lengths[type]; position++ ) {
				int [] spot = { row, column };
				positions[position] = spot;
				
				// Calculate the next spot
				row += rowColumn[0];
				column += rowColumn[1];
			}
			
			// Create the ship object and place it in the array
			ships[index] = new Ship(positions, lengths[type], shipMark, ocean);
			type++;
			return true;
		}
		
		else {
			System.out.println( "Not a valid position and or direction!" );
			return false;
		}
	}
	
	/**
	 * The purpose of this method is to
	 * check to see if the attack hits
	 * any of the ships in the fleet
	 * @param   row      the row being
	 *                   attacked
	 * @param   column   the column being
	 *                   attacked
	 * @return  boolean  true if it hit,
	 *                   false otherwise
	 */
	public boolean checkHit( int row, int column, char hitMark ) {
		
		// Initialize as false, since we
		// don't know it hit until all of
		// the ships are checked.
		boolean hit = false;
		
		// Position in the ships array of the
		// current ship we are checking
		int shipNumber = 0;
		
		// Loop over the ships
		while( shipNumber < 4 && ships[shipNumber] != null && !hit ) {
			
			// Make a call to the current ship's hitOrMiss method.
			hit = ships[shipNumber].hitOrMiss( row, column, hitMark );
			
			// Check the next ship
			shipNumber++;
		}
		
		return hit;
	}
	
	/**
	 * The purpose of this method is to
	 * check all of our ships to see if they
	 * are still afloat.
	 * @return   boolean   so long as one ship is
	 *                     still afloat, this will
	 *                     return true
	 */
	public boolean checkShips() {
		
		// If a single ship is still afloat,
		// this will become true
		boolean afloat = false;
		
		// Loop over the ships
		for( int ship = 0; ship < ships.length; ship++ ) {
			
			// If we still haven't found a ship that is
			// afloat, check again.
			if( !afloat ) {
				afloat = ships[ship].sunkOrAfloat();
			}
		}
		
		return afloat;
	}
}
