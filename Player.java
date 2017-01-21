/**
 * This class stores the player name, 
 * ship marker, and hit marker.
 * @author Ajinkya Kolhe 
 * @author Kyle McGlynn
 */
public class Player {

	// This player's name
	String playerName;
	
	// The mark indicating this player's
	// ships
	char shipMark;
	
	// The mark indicating a hit by
	// this player
	char hitMark;
	
	/**
	 * The constructor. It initializes the player's 
	 * name, ship marker, and hit marker.
	 * @param   playerName   The name of the player
	 * @param   shipMark     The mark representing this
	 *                       player's ships
	 * @param   hitMark      The mark representing a hit
	 *                       by this player
	 */
	public Player( String playerName, char shipMark, char hitMark ) {
		this.playerName = playerName;
		this.shipMark = shipMark;
		this.hitMark = hitMark;
	}
}