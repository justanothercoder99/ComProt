import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * This class handles the connection to a
 * player for the server. It also contains part 
 * of the logic needed to play the game.
 * @author   Kyle McGlynn
 * @author   Ajinkya Kolhe
 *
 */
public class TCP_Server_Helper extends Thread {
	
	// The numeric id of this player
	int player;
	
	// The socket through which this is 
	// connected to the player
	Socket client;
	
	// A reference to the main server
	TCP_Server server;
	
	// Streams for reading and writing
	// objects
	ObjectInputStream ois;
	ObjectOutputStream oos;
	
	// Whose turn it currently is
	static int turn = 0;
	
	// Determines whose turn it is
	static int sign = 1;
	
	// Keeps track of who has completed
	// setting up their ships.
	static int setUp = 0;
	
	// If there is a problem on the main
	// server side, before the game starts,
	// this shall go to false
	boolean play = true;
	
	/**
	 * The constructor. It initializes this helper thread
	 * with all of the information needed to handle the 
	 * connection to a player
	 * @param   player   the id number of the player this
	 *                   helper thread is connected to
	 * @param   client   the socket through which this helper
	 *                   thread is connected to the player
	 * @param   server   a reference to the main server
	 * @param   ois      used for reading objects from
	 *                   a byte stream
	 * @param   oos      used for writing objects to
	 *                   a byte stream
	 */
	public TCP_Server_Helper( int player, Socket client, TCP_Server server, 
			ObjectInputStream ois, ObjectOutputStream oos ){
		this.player = player;
		this.client = client;
		this.server = server;
		this.ois = ois;
		this.oos = oos;
	}
	
	/** 
	 * This method creates a fleet 
	 * of ships for a game of
	 * battle ship according to user
	 * input. However, ships can not
	 * touch or overlap.
	 */
	private void fleetSetUp( int player ) {
		
		try{
			
			// Index in the ships array where we
			// will place the new ship
			int index = 0;
			
			// Continue until the user has filled the 
			// array of ships
			while( index != 4 ) {
				
				// Tell the player which ship number
				// it is working on
				oos.writeObject( new Integer(index) );
				
				// Get the desired starting point and direction
				Object[] inputs = ( Object[] ) ois.readObject();
				
				
				// If the ship can be built, increment index
				if( server.fleetSetUp( player, inputs ) ) {
					
					// If the ship was built, return true
					oos.writeObject( new Boolean(true) );
					index++;
				}
				else{
					oos.writeObject( new Boolean(false) );
				}
			}
		} catch ( IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * The run method. It handles set up of the player's
	 * fleet and contains the server-side logic for 
	 * playing the game.
	 */
	public void run() {
				
		try{
			
			// Tell the player we are set to play
			oos.writeObject( new Boolean( play ) );
			
			// If we are ready to play, play
			if( play ) {
				
				// Set up the fleet for this player
				fleetSetUp(player);
				
				// Once the player's fleet is set up, 
				// increment this value by one
				setUp++;
				
				// Wait for the other player to set up
				// their fleet
				while( setUp != 2 ){}
				
				// So long as either player has at least one ship still
				// afloat, the game continues
				while( server.checkStatus() ) {
					
					// If it is this player's turn
					if( turn == player ) {
						
						// Tell the player that the game
						// is still going	
						oos.writeObject(new Boolean(true));
						
						// Send the current player's ocean with their ships
						oos.writeObject( server.printOcean( turn, true ) );
						
						// Send the waiting player's ocean without their
						// ships. This is so that the guessing player
						// can see their hits and misses.
						oos.writeObject( server.printOcean( turn + sign, false ) );
						
						// Get the current player's desired target
						int[] target = ( int[] ) ois.readObject();
												
						// Pass the target to the model. If it returns 
						// true, then it hit, otherwise it is a miss.
						if( server.checkHit(turn+sign, target, turn)){
							oos.writeObject( new String( "Hit!" ) );
						}
						else {
							oos.writeObject( new String( "Miss!" ) );
						}
						
						// Change the turn
						turn += sign;
						sign *= -1;
					}
				}
				
				// Indicate to the player that the game is over
				oos.writeObject( new Boolean( false ) );
				
				// Tell the player who won
				oos.writeObject( server.victory() );
			}
		} catch ( IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}