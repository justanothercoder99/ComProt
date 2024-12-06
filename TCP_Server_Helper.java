import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * This class handles the connection to a
 * player for the server. It also contains part 
 * of the logic needed to play the game.
 * @author Aneesh Deshmukh
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
				server.markSetup(player);
				
				// Wait for the other player to set up
				// their fleet
				int setupTimeout = 15000; // 15 seconds
				int elapsedTime = 0;
				while( !server.checkSetupComplete() && elapsedTime < setupTimeout) {
					Thread.sleep(1000); // Sleep for 1000ms to avoid overloading CPU
					elapsedTime += 1000;
				}
				if (elapsedTime >= setupTimeout) {
					System.out.println("Timeout reached, setup incomplete.");
				}
				oos.writeObject( new Boolean(true) );
			}

			while( server.checkStatus() ) {
				int setupTimeout = 10000; // 10 seconds
				int elapsedTime = 0;

				int turn = server.getTurn();
				int sign = server.getSign();
				// If it is this player's turn
				if( turn == player ) {
					elapsedTime = 0;

					// Tell the player that the game
					// is still going	
					oos.writeObject(new Boolean(true));
					
					// Send the current player's ocean with their ships
					oos.writeObject( new String("Your Fleet") );
					oos.writeObject( server.printOcean( turn, true ) );
					
					// Send the waiting player's ocean without their
					// ships. This is so that the guessing player
					// can see their hits and misses.
					oos.writeObject( new String("Your Attacks") );
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

					oos.writeObject( new String("Opponent Ocean") );
					oos.writeObject( server.printOcean( turn + sign, false ) );
					// Change the turn
					server.changeTurn();
				} else {
					Thread.sleep(1000); // Sleep for 1000ms to avoid overloading CPU
					elapsedTime += 1000;
				}
			}
			
			// Indicate to the player that the game is over
			oos.writeObject( new Boolean( false ) );
			
			// Tell the player who won
			oos.writeObject( server.victory() );
		} catch ( IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}