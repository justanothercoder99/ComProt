import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

/**
 * This class handles communication via TCP
 * between the server and a player.
 * @author Kyle McGlynn
 * @author Ajinkya Kolhe
 *
 */
@SuppressWarnings("serial")
public class TCP_Communicator implements Serializable{
	
	// An object of the View class. It is
	// used to communicate with the player.
	View view;
	
	// The name of the player
	String playerName;
	
	// The socket through which we are
	// connected to the server
	Socket socket;
	
	// Streams for reading and writing
	// objects
	ObjectOutputStream oos;
	ObjectInputStream ois;
	
	/**
	 * The constructor. It initializes the name of the player.
	 * @param   playerName   the name of this player
	 */
	public TCP_Communicator( String playerName ){
		this.playerName = playerName;
	}
	
	/**
	 * This method acquires the socket, ObjectOutputStream, 
	 * and ObjectInputStream needed to communicate with the
	 * server
	 * @param   socket   the socket through which we are 
	 *                   connected to the server
	 * @param   oos      the ObjectOutputStream
	 * @param   ois      the ObjectInputStream
	 */
	public void setSocket( Socket socket, ObjectOutputStream oos, ObjectInputStream ois ) {
		this.socket = socket;
		this.oos = oos;
		this.ois = ois;
		view = new View();
	}
	
	/**
	 * This method asks the user to pick spots
	 * for their ships. It then communicates with
	 * the server to make sure they are valid
	 * positions and directions.
	 */
	public void fleetSetUp(){
		
		try{
			
			// The number of ships created so far
			int ships = 0;
			
			// So long as the user has not successfully
			// created four ships
			while( ships < 4 ) {
				
				// Read from the server what length ship we
				// are supposed to be creating
				int length = ( int ) ois.readObject();
				
				// Get the user's choice through View and send it
				// to the server for verification
				oos.writeObject( view.getPositions( playerName, length ) );
				
				// If the ship could be built in that position
				// and in that direction, a boolean value of true
				// will be returned.
				if( (boolean) ois.readObject() ) {
					ships++;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
		
	/**
	 * This method contains part of the logic 
	 * needed to play a game of battleship. It 
	 * shows the user their guesses so far, the
	 * state of their board, and asks them to
	 * pick a target. 
	 */
	public void game(){
		
		try{
			
			// If the server is all set, it will send
			// a boolean value of true. If there were not enough
			// players it will send a boolean value of false.
			if( (boolean) ois.readObject() ){
			
				// Set up the fleet
				fleetSetUp();
							
				// Flag that controls if the game
				// is still being played
				boolean game = (boolean) ois.readObject();
				
				// So long as no one has won, keep playing
				while( game ) {
					
					// Blocks until the server sends a boolean
					// value. Once it receives a value, it must
					// be this player's turn. If the value is 
					// false, then the game is over.
					if( (boolean) ois.readObject() ){
						// The player's ocean
						view.message( (String) ois.readObject() );
						view.message( (String) ois.readObject() );
						
						// The player's guesses
						view.message( (String) ois.readObject() );
						view.message( (String) ois.readObject() );
						
						// Get desired target
						oos.writeObject( view.getInput(playerName) );
						
						// See if it was a hit or miss
						view.message( ( String ) ois.readObject() );

						// The player's guesses
						view.message( (String) ois.readObject() );
						view.message( (String) ois.readObject() );
					}
					else{
						game = false;
					}
				}
				
				// See who the winner is
				view.message( (String) ois.readObject()); 
			}
			else{
				
				// Error message
				view.message( "Not enough players." );
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}