/**
 * The purpose of this class is to
 * simulate an ocean in a game of
 * battle ship. It is represented
 * by a 2D char array of ' ' characters.
 * Hit, miss, and boat markers are 
 * placed on it. 
 *
 */
public class Ocean {
	
	// The 2D char array that will
	// hold the game board
	char [][] ocean;
	
	// Size of the game board
	int rowSize;
	int columnSize;
	
	// Coordinates of upper left hand corner
	int minRow;
	int minColumn;
	
	// Coordinates of lower right hand corner
	int maxRow;
	int maxColumn;
	
	// Indicates a boat piece
	char boat = ' ';
	
	/**
	 * The constructor. It creates the
	 * object and initializes the 
	 * 2d char array.
	 * @param   minRow      the row position of the
	 *                      upper left corner   
	 * @param   minColumn   the column position of
	 *                      the upper left corner
	 * @param   maxRow      the row position of the
	 *                      lower right corner 
	 * @param   maxColumn   the column position of
	 *                      the lower right corner
	 */
	public Ocean( int minRow, int minColumn,
			int maxRow, int maxColumn ) {
		
		this.minRow = minRow;
		this.minColumn = minColumn;
		this.maxRow = maxRow;
		this.maxColumn = maxColumn;
		
		// Determine the number of rows
		rowSize = maxRow - minRow;
		
		// Determine the number of columns
		columnSize = maxColumn - minColumn;
		
		// Initialize the size of the ocean
		ocean = new char [maxRow - minRow] [maxColumn - minColumn];
		
		// Initalize all positions in the ocean to ' '
		for( int outer = 0; outer < rowSize; outer++ ) {
			for( int inner = 0; inner < columnSize; inner++ ) {
				ocean[outer][inner] = ' ';
			}
		}
	}
	
	/**
	 * This method sets the character that
	 * represents a piece of a boat.
	 * @param   boat   the character that
	 *                 represents a 
	 *                 piece of a boat
	 */
	public void boatCharacter( char boat ) {
		this.boat = boat;
	}
	
	/**
	 * This method sets a mark on the 
	 * game board. This can be a hit 
	 * or miss mark.
	 * @param   row      the row where the mark 
	 *                   will be placed
	 * @param   column   the column where the mark
	 *                   will be placed  
	 * @param   mark     the char that will represent
	 *                   a hit or miss
	 */
	public void setMark( int row, int column, char mark ) {	
		ocean[row][column] = mark;
	}
	
	/**
	 * This method checks at the given position
	 * on the board for the given mark.
	 * @param    row      the row where
	 *                    we will check
	 * @param    column   the column where
	 *                    we will check
	 * @param    mark     the mark we will
	 *                    look for
	 * @return   boolean  true if we found it,
	 *                    false otherwise
	 */
	public boolean checkMark( int row, int column, char mark ) {
		
		// Look for the mark at the position
		if( ocean[row][column] == mark ) {
			return true;
		}
		
		else {
			return false;
		}
	}
	
	/**
	 * The purpose of this method is to check to
	 * see whether the given coordinates are within
	 * the game board.
	 * @param   row      the row we will be 
	 *                   checking
	 * @param   column   the column we will be 
	 *                   checking
	 * @return  boolean  true if it is within,
	 *                   false otherwise
	 */
	public boolean checkOcean( int row, int column ) {
		
		// If either the row or the column exceeds
		// the bounds of the game board, than the
		// position must be outside.
		if( row >= maxRow || column >= maxColumn ||
				row < minRow || column < minColumn ) {
			System.out.println( "Off the ocean!" );
			return false;
		}
		
		else {
			return true;
		}
	}
	
	/**
	 * This method prints the game board. 
	 * If the boolean parameter is true,
	 * the boats are printed. If false, 
	 * they are not.
	 * @param   yesOrNo   a boolean value
	 *                    that determines
	 *                    if the boats will 
	 *                    be printed
	 */
	public String printOcean( boolean yesOrNo ) {
		
		// A string representation of the ocean
		String rep = "";
		
		// Place a character above the actual board
		// as a sort of border.
		for( int upperBound = 0; upperBound < columnSize + 2; upperBound++ ) {
			rep += "-";
		}
		rep += "\n";
		
		// Loop over the game board, printing
		// out each character.
		for( int outer = 0; outer < rowSize; outer++ ) {
			
			// Left border character
			rep += "|";
			for( int inner = 0; inner < columnSize; inner++ ) {
				
				if( ocean[outer][inner] == boat && !yesOrNo ) {
					rep += ' ';
				}
				
				else {
					rep += ocean[outer][inner];
				}
			}
			
			// Right border character
			rep += "|" + "\n";
		}
		
		// Place a character below the actual board
		// as a sort of border.
		for( int lowerBound = 0; lowerBound < columnSize + 2; lowerBound++ ) {
			rep += "-";
		}
		rep += "\n";
		return rep;
	}
}
