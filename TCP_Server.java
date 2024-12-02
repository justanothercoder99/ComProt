import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * A server for a game of battleship. This
 * class communicates with player's through
 * helper threads via TCP.
 * @author   Kyle McGlynn
 * @author   Ajinkya Kolhe
 * @author Aneesh Deshmukh
 * 
 */
public class TCP_Server {
	
	// TCP socket through which we will communicate with
	// the player
	ServerSocket server;
	
	// The port that this server shall listen on
	int port = 4455;
	
	// Model of the BattleShip game
	Model model;
	
	// The players
	Player[] players = new Player[2];
	int[] setupComplete = new int[2];
				
	// The boat and hit marks of the players
	char[] boatMarks = { 'A', 'B' };
	char[] hitMarks = { 'a', 'b' };
	
	// The TCP_Communicators that shall handle 
	// communication with the server
	TCP_Communicator[] comms = new TCP_Communicator[2];
	
	// The threads that shall handle the wireless connection
	// between the server and the player
	TCP_Server_Helper[] helpers = new TCP_Server_Helper[2];

	// Whose turn it currently is
	static int turn = 0;
	
	// Determines whose turn it is
	static int sign = 1;
	
	/**
	 * The constructor. It initializes the ServerSocket
	 * and the model. 
	 */
	public TCP_Server(){
		
		try{
			server = new ServerSocket( port );
		}
		catch ( SocketException e ) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// A model object. It contains the state
		// and basic rules of the game.
		model = new Model();
	}

	public void sendData(Object data, ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
		int packetId;
		long packetTimestamp;
		
		if (data instanceof Boolean) {
            Packet<Boolean> packet = new Packet<>((boolean) data);
			packetId = packet.getPacketId();
			packetTimestamp = packet.getTimestamp();
			oos.writeObject( packet );
        } else if (data instanceof Integer) {
            Packet<Integer> packet = new Packet<>((Integer) data);
			packetId = packet.getPacketId();
			packetTimestamp = packet.getTimestamp();
			oos.writeObject( packet );
        } else if (data instanceof String) {
            Packet<String> packet = new Packet<>(new String[]{(String) data}); // Wrap the string in a string array
			packetId = packet.getPacketId();
			packetTimestamp = packet.getTimestamp();
			oos.writeObject( packet );
        } else if (data instanceof String[]) {
            Packet<String> packet = new Packet<>((String[]) data);
			packetId = packet.getPacketId();
			packetTimestamp = packet.getTimestamp();
			oos.writeObject( packet );
        } else if (data instanceof TCP_Communicator) {
            Packet<TCP_Communicator> packet = new Packet<>((TCP_Communicator) data);
			packetId = packet.getPacketId();
			packetTimestamp = packet.getTimestamp();
			oos.writeObject( packet );
        } else {
            Packet<Object> packet = new Packet<>((Object[]) data);
			packetId = packet.getPacketId();
			packetTimestamp = packet.getTimestamp();
			oos.writeObject( packet );
        }

		System.out.println("Sent Packet ID: " + packetId);
		try {
            server.setSoTimeout(0);
            Packet<Void> acknowledgment = (Packet<Void>) ois.readObject();
            System.out.println("Received ACK for Packet ID: " + acknowledgment.getPacketId());
			long delay = acknowledgment.getTimestamp() - packetTimestamp;
			System.out.println("Total Communication Time: " + delay + " ms");
        } catch (SocketTimeoutException e) {
            System.out.println("Acknowledgment timeout for Packet ID: " + packetId);
        }
	}

	public <T> Packet<T> receiveData(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
		Packet<T> receivedPacket = (Packet<T>) ois.readObject();
		long delay = System.currentTimeMillis() - receivedPacket.getTimestamp();

		System.out.println("Received Packet ID: " + receivedPacket.getPacketId());
		System.out.println("Packet delay: " + delay + " ms");

		Packet<Void> acknowledgment = new Packet<>(receivedPacket.getPacketId(), true);
		oos.writeObject(acknowledgment);
		return receivedPacket;
	}
	
	/**
	 * This method waits to receive a connection from two
	 * players. If it doesn't hear from a player within
	 * three minutes it automatically closes the server.
	 * @return   boolean   true if two players connect,
	 *                     false if the timeout occurred
	 */
	public boolean getPlayers(){
		
		try {
			
			// Set time out to three minutes. This gives
			// any players enough time to connect.
			server.setSoTimeout( 180000 );
			
			// The current player we are trying to make
			int player = 0;
			
			// Get two players to play the game
			while( player != 2 ) {
				
				// Get the player connection
				Socket client = server.accept();
				ObjectInputStream ois = new ObjectInputStream( client.getInputStream() );
				ObjectOutputStream oos = new ObjectOutputStream( client.getOutputStream() );
				
				// The player's name
				Packet<String> pkt = receiveData(oos, ois);
				String playerName = (String) pkt.getObjectData();
				
				// Create a player object
				players[player] = new Player( playerName, boatMarks[player], hitMarks[player] );
				setupComplete[player] = 0;
				// Create a TCP_Communicator object and send it to the player
				comms[player] = new TCP_Communicator( playerName );
				
				// Send a TCP_Communicator object
				sendData(comms[player], oos, ois);
				
				// Create a helper thread				
				helpers[player] = new TCP_Server_Helper( player, client, this, ois, oos );
				player++;
			}
			
			return true;
		
		} catch ( SocketTimeoutException e ) {
			System.out.println( "No players came." );
			if( helpers[0] != null ) {
				
			}
			return false;
		} catch ( IOException e ) {
			e.printStackTrace();
			return false;
		} catch ( ClassNotFoundException e ) {
			e.printStackTrace();
			return false;
		} 
	}

	public Player getPlayer(int player_id) {
		return players[player_id];
	}
	
	/**
	 * This method sets up a game of BattleShip.
	 * If the server does not receive connections
	 * to two players, this method will return false.
	 * @return   boolean   true if two players connected,
	 *                     false otherwise
	 */
	public boolean controlSetUp(){
		
		// Wait for two players to connect
		if( getPlayers() ) {
			
			// Once two players have connected, 
			// set up the model and tell the helper
			// threads to start
			model.modelSetUp( players[0].shipMark, players[1].shipMark );
			helpers[0].start();
			helpers[1].start();

			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * This method creates one ship
	 * out of many. It is called by a 
	 * helper thread to check that the 
	 * ship can indeed be built at the 
	 * desired spot and in the desired 
	 * direction.
	 * @param   player   the numeric id of
	 *                   the player for whom
	 *                   the ship is being built
	 * @param   inputs   the desired place and
	 *                   direction
	 * @return  boolean  true if it can be
	 *                   built, false otherwise
	 */
	public boolean fleetSetUp( int player, Object[] inputs ) {
		
		// The input row
		int row = 0;
		
		// The input column
		int column = 0;
		
		// The input direction
		String direction = "";
		
		// Retrieve row, column, and direction
		// values from input
		row = ( Integer ) inputs[0];
		column = ( Integer ) inputs[1];
		direction = ( String ) inputs[2];
		
		// If the ship can be built, return true
		return model.buildShip( player, row, column, direction );
	}

	/**
	 * This method is used to mark when 
	 * each player completes their setup.
	 * 
	 * @param	player  the numberic id of
	 * 					the player who has
	 * 					completed their setup
	 * 
	 * @return	boolean true on successful update
	 */
	public void markSetup(int player) {
		setupComplete[player] = 1;
	}

	/**
	 * This method is used to check if both 
	 * players have completed their fleet setup
	 * 
	 * @return	boolean	true is both players have 
	 * 					completed their setup
	 */
	public boolean checkSetupComplete() {
		int player = 0;
		boolean isComplete = true;
		while(player != 2) {
			isComplete &= setupComplete[player] == 1;
			player++;
		}
		return isComplete;
	}
	
	/**
	 * Check to see if the game is still
	 * going.
	 * @return   boolean   true if the game is
	 *                     still going, false
	 *                     otherwise
	 */
	public boolean checkStatus() {
		return model.checkStatus();
	}

	public int getTurn() {
		return turn;
	}

	public int getSign() {
		return sign;
	}

	public void changeTurn() {
		turn += sign;
		sign *= -1;
	}
	
	/**
	 * Get and return a string representation
	 * of the ocean belonging to the player
	 * argument.
	 * @param   player    the numeric id of the player
	 *                    whose ocean we want to return
	 * @param   yesOrNo   a boolean value, indicating
	 *                    if we want the ships included
	 * @return  String    a string representation of the
	 *                    player's arguments ocean
	 */
	public String printOcean( int player, boolean yesOrNo ) {
		return model.printOcean(player, yesOrNo);
	}
	
	/**
	 * Check the model to see if the given coordinates
	 * hit a ship or not 
	 * @param    targetedPlayer    the player who is being attacked
	 * @param    target            the coordinates being targeted
	 * @param    attackingPlayer   the player who is attacking
	 * @return   boolean           true if it was a hit, false
	 *                             otherwise
	 */
	public boolean checkHit( int targetedPlayer, int[] target, int attackingPlayer ) {
		return model.checkHit( targetedPlayer, target[0], 
				target[1], players[attackingPlayer].hitMark );
	}
	
	/**
	 * Check to see who the winner is.
	 * @return   String   the name of the winner
	 */
	public String victory(){
		
		// Get the numeric id of the winner
		int winner = model.victory();
		
		return players[winner].playerName + " has won!";
	}
	
	/**
	 * The main method. It starts the server, listening
	 * on a pre-designated port on the local host.
	 * @param   args   command line arguments ( not used )
	 */
	public static void main( String [] args ) {
		
		// Create an object of this server
		TCP_Server server = new TCP_Server();
		
		// Set up the game
		server.controlSetUp();
	}
}

