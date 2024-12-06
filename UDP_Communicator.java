import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * This class handles communication via UDP
 * between the server and a player.
 *
 */
@SuppressWarnings("serial")
public class UDP_Communicator implements Serializable{
	
	// A View object used to interact with
	// the player
	View view;
	
	// The name of the player
	String playerName;
	
	// IP address of the server
	InetAddress server;
	
	// Port the server is listening
	// on in its machine
	int serverPort;
	
	// DatagramSocket connection to server
	DatagramSocket socket;
	
	/**
	 * Constructor. It initializes nearly everything
	 * that is needed to communicate with the
	 * server and play a game of battle ship.
	 * @param   playerName   the name of the player
	 * @param   server       the IP address of the server
	 * @param   serverPort   the port of the server
	 */
	public UDP_Communicator( String playerName, InetAddress server, int serverPort ){
		this.playerName = playerName;
		this.server = server;
		this.serverPort = serverPort;
	}
	
	/**
	 * Initialize the socket.
	 * @param   socket   the Datagramsocket through
	 *                   which this object connects
	 *                   to the server
	 */
	public void setSocket( DatagramSocket socket ) {
		this.socket = socket;
		view = new View();
	}
	
	/**
	 * This method contains the logic
	 * needed to send objects through
	 * UDP.
	 * @param   obj      the object to be sent
	 * @param   player   the player to whom
	 *                   an object is being sent
	 */
	public void send( Object obj ) {
		
		try {
			
			// Byte array to hold object we wish to send
			byte[] bufInput = new byte[2048]; 
			
			// Convert the object into bytes
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream( baos );
			oos.writeObject( obj );
			oos.flush();
			bufInput = baos.toByteArray();
			oos.close();
			baos.close();
			
			// Create a datagram packet to send the object
			DatagramPacket send = new DatagramPacket( bufInput, bufInput.length,
					server, serverPort );
			socket.send( send );
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method contains the logic
	 * needed for receiving objects 
	 * through UDP.
	 * @param    player   the player to whom obj is sent
	 * @return   Object   the object that is received through
	 *                    the connection
	 */
	public Object receive(){
		
		try {
			
			// Byte array to hold the object that we receive
			byte[] bufOutput = new byte[2048];
			
			// Create a datagram object to receive the object
			DatagramPacket receive = new DatagramPacket( bufOutput, bufOutput.length );
			socket.receive( receive );
			
			// Convert the object from bytes into an object
			ByteArrayInputStream bais = new ByteArrayInputStream( receive.getData() );
			ObjectInputStream ois = new ObjectInputStream( bais );
			Object result = ois.readObject();
			ois.close();
			bais.close();
			
			return result;
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} 		
		return null;
	}
	
	
	/**
	 * This method sets up the fleet object 
	 * for this player on the server by getting
	 * user input until four ships have been built
	 */
	public void fleetSetUp(){
		
		// The number of ships built so far
		int ships = 0;
		
		// While there are still ships to build
		while( ships < 4 ) {
			
			// Find out what type of ship is being made
			int length = (int) receive();
			
			// Get user input and send it to the server
			// for verification of validity
			send( view.getPositions(playerName, length) );
			
			// If the ship was successfully built
			if( (boolean) receive() ) {
				ships++;
			}
		}
	}
	
	/**
	 * This method contains the player side
	 * logic of a game of battle ship. 
	 */
	public void game(){
		
		// Set up the fleet
		fleetSetUp();
		
		// Flag that controls if the game
		// is still being played
		boolean game = true;
		
		// So long as no one has won, keep playing
		while( game ) {
			
			// Blocks until the server sends a boolean
			// value. Once it receives a value, it must
			// be this player's turn. If the value is 
			// false, then the game is over.
			if( (boolean) receive() ){
				
				// The player's ocean
				view.message( ( String ) receive() );
				view.message( ( String ) receive() );
				
				// The player's guesses
				view.message( ( String ) receive() );
				view.message( ( String ) receive() );
				
				// Get desired target
				send( view.getInput(playerName) );
				
				// See if it hit or not
				view.message( ( String ) receive() );

			}
			else{
				game = false;
			}
		}
		
		// Find out who won
		view.message( ( String ) receive()); 
	}
}