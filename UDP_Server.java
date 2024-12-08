import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * This class is a server in a game
 * of battle ship. It communicates to its
 * players via UDP.
 *
 */
public class UDP_Server {
	
	// UDP socket through which we will communicate with
	// the player
	DatagramSocket server;
	
	// The port that this server shall listen on
	int port = 4455;
	
	// Model of the BattleShip game
	Model model;
	
	// The players
	Player[] players = new Player[2];
	
	// The IP Address objects of the players' machines
	InetAddress[] playerMachine = new InetAddress[2];
	
	// Ports through which the players connected to the server
	int[] playerPort = new int[2];
			
	// The boat and hit marks of the players
	char[] boatMarks = { 'A', 'B' };
	char[] hitMarks = { 'a', 'b' };
	
	// The UDP_Communicators that shall handle 
	// communication with the server
	UDP_Communicator[] comms = new UDP_Communicator[2];
	
	// Whose turn it currently is
	int turn = 0;
	
	// Determines whose turn it is
	int sign = 1;

	/**
	 * The constructor. It initializes the DatagramSocket
	 * and the model. 
	 */
	public UDP_Server(){
		
		try{
			server = new DatagramSocket( port );
		}
		catch ( SocketException e ) {
			e.printStackTrace();
		}
		
		model = new Model();
	}

	public void sendData(Object data, ObjectOutputStream oos) throws IOException, ClassNotFoundException {
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
            Packet<String> packet = new Packet<>((String) data);
			packetId = packet.getPacketId();
			packetTimestamp = packet.getTimestamp();
			oos.writeObject( packet );
        } else if (data instanceof String[]) {
            Packet<String> packet = new Packet<>((String[]) data);
			packetId = packet.getPacketId();
			packetTimestamp = packet.getTimestamp();
			oos.writeObject( packet );
        } else if (data instanceof int[]) {
            Packet<int[]> packet = new Packet<>((int[]) data);
			packetId = packet.getPacketId();
			packetTimestamp = packet.getTimestamp();
			oos.writeObject( packet );
        } else if (data instanceof UDP_Communicator) {
            Packet<UDP_Communicator> packet = new Packet<>((UDP_Communicator) data);
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
	}

	public <T> Packet<T> receiveData(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		Packet<T> receivedPacket = (Packet<T>) ois.readObject();
		long delay = System.currentTimeMillis() - receivedPacket.getTimestamp();

		System.out.println("Received Packet ID: " + receivedPacket.getPacketId());
		System.out.println("Packet delay: " + delay + " ms");
		return receivedPacket;
	}
	
	/**
	 * This method contains the logic
	 * needed to send objects through
	 * UDP.
	 * @param   obj      the object to be sent
	 * @param   player   the player to whom
	 *                   an object is being sent
	 */
	public void send( Object obj, int player ) {
		
		try {
			
			// Byte array to hold object we wish to send
			byte[] bufInput = new byte[1024]; 
			
			// Convert the object into bytes
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream( baos );
			sendData(obj, oos);
			oos.flush();
			bufInput = baos.toByteArray();
			oos.close();
			baos.close();
			
			// Create a datagram packet to send the object
			DatagramPacket send = new DatagramPacket( bufInput, bufInput.length,
					playerMachine[player], playerPort[player] );
			server.send( send );
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.out.println("Error attempting to send data of type " + obj.getClass());
			e.printStackTrace();
		}
	}
	
	/**
	 * This method contains the logic
	 * needed for receiving objects 
	 * through UDP.
	 * @return   Object   the object that is received through
	 *                    the connection
	 */
	public Object receive(boolean isArray){
		
		try {
			
			// Byte array to hold the object that we receive
			byte[] bufOutput = new byte[1024];
			
			// Create a datagram object to receive the object
			DatagramPacket receive = new DatagramPacket( bufOutput, bufOutput.length );
			server.receive( receive );
			
			// Convert the object from bytes into an object
			ByteArrayInputStream bais = new ByteArrayInputStream( receive.getData() );
			ObjectInputStream ois = new ObjectInputStream( bais );
			Packet<Object> packet = receiveData(ois);
			Object result;
			if (isArray) result = packet.getObjectsArray();
			else result = packet.getObjectData();
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
	 * This method waits to receive a packet from two
	 * players. If it doesn't hear from a player within
	 * three minutes it automatically closes the server.
	 * @return   boolean   true if two players connect,
	 *                     false if the timeout occurred
	 */
	public boolean getPlayers(){
		
		try {
			
			// Set time out to three minutes. This gives
			// any players enough time to connect.
			server.setSoTimeout(180000);
			
			// The current player we are trying to make
			int player = 0;
			
			// Get two players to play the game
			while( player != 2 ) {
				
				// A byte array to hold the name of a player
				byte[] nameBuf = new byte[1024];
				
				// Datagram packet to receive the name of a player
				DatagramPacket receive = new DatagramPacket( nameBuf, nameBuf.length);
				server.receive(receive);
								
				// The IP address of the machine that sent the name
				playerMachine[player] = receive.getAddress();
				
				// Port through which the name was sent
				playerPort[player] = receive.getPort();
								
				// Convert the name from bytes into a String
				ByteArrayInputStream bais = new ByteArrayInputStream( receive.getData() );
				ObjectInputStream ois = new ObjectInputStream( bais );
				Packet<String> packet = receiveData(ois);
				String playerName = (String) packet.getObjectData();
				
				// Create a player object
				players[player] = new Player( playerName, boatMarks[player], hitMarks[player]);
				
				// Create a UDP_Communicator object and send it to the player
				comms[player] = new UDP_Communicator( playerName, InetAddress.getLocalHost(), port );
				
				// Convert the UDP_Communicator object into bytes
				ByteArrayOutputStream boas = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream( boas );
				sendData( comms[player], oos );
				oos.flush();
				byte[] commBuf = boas.toByteArray();
								
				// Send the UPD_Communicator object to the player
				DatagramPacket send = new DatagramPacket( commBuf, commBuf.length,
						playerMachine[player], playerPort[player] );
				server.send(send);
				oos.close();
				boas.close();
				
				player++;
			}
			
			return true;
		} catch ( SocketTimeoutException e ) {
			System.out.println( "Not enough players." );
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			server.disconnect();
			server.close();
			return false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		} 
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
			// set up the model and the fleets
			model.modelSetUp( players[0].shipMark, players[1].shipMark );
			fleetSetUp(0);
			fleetSetUp(1);
			return true;
		}
		else{
			return false;
		}
	}
	
	/** 
	 * This method creates a fleet 
	 * of ships for a game of
	 * battle ship according to user
	 * input. However, ships can not
	 * touch or overlap.
	 */
	private void fleetSetUp( int player ) {
		
		// The input row
		int row = 0;
		
		// The input column
		int column = 0;
		
		// The input direction
		String direction = "";
		
		// Index in the ships array where we
		// will place the new ship
		int index = 0;
		
		// Continue until the user has filled the 
		// array of ships
		while( index != 4 ) {
			
			// Array of Objects to hold the returned
			// integer coordinates and String direction
			send( new Integer(index), player );
			Object[] inputs = (Object[]) receive(true);
			row = (Integer) inputs[0];
			column = (Integer) inputs[1];
			direction = (String) inputs[2];
			
			// If the ship can be built, increment index
			if( model.buildShip( player, row, column, direction ) ) {
				send( new Boolean( true ), player );
				index++;
			}
			else{
				send( new Boolean( false ), player );
			}
		}
	}
	
	/**
	 * The purpose of this method is to manage a game
	 * of battle ship.
	 */
	public void playGameWith(  ) {
		
		// Set the first player to guess
		Player current = players[0];
		
		// Set the player waiting for their turn.
		Player waiting = players[1];
		
		// Used for when the current player changes
		Player temp;
		
		// So long as either player has at least one ship still
		// afloat, the game continues
		while( model.checkStatus() ) {
			
			// Tell the current player that the game
			// is still going
			send( new Boolean(true), turn );
			
			// Print the current player's ocean with their ships
			send( new String( "Your ocean:" ), turn );
			send( model.printOcean( turn, true), turn );
			
			// Print the waiting player's ocean without their
			// ships. This is so that the guessing player
			// can see their hits and misses.
			send( new String( "Your guesses:" ), turn );
			send( model.printOcean( turn + sign, false), turn );
			
			// Ask the current player where they want to attack
			int[] target = (int[]) receive(false);
						
			// Check to see if it hit or not.If it returns true, 
			// then it hit, otherwise it is a miss.
			if( model.checkHit( turn+sign, target[0], target[1], players[turn].hitMark ) ) {
				send( new String( "Hit!" ), turn );
			}
			else {
				send( new String( "Miss!" ), turn );
			}
			
			// Switch current and waiting players
			temp = current;
			current = waiting;
			waiting = temp;
			
			// Update the turn
			turn += sign;
			sign *= -1;
		}
		
		// Let the players know that the game is over
		send( new Boolean( false ), 0 );
		send( new Boolean( false ), 1 );
		
		// Find out who the winner is
		int winner = model.victory();
		send( players[winner].playerName + " has won!", 0 );
		send( players[winner].playerName + " has won!", 1 );
		
		// The game is over, so close the socket.
		server.disconnect();
		server.close();
	}
	
	/**
	 * The main method. It sets up the server
	 * and starts the game. If there are not enough
	 * players it stops running after three minutes.
	 * @param   args   command line arguments ( not used )
	 */
	public static void main( String [] args ) {
		UDP_Server server = new UDP_Server();
		if( server.controlSetUp() ) {
			server.playGameWith();
		}
	}
}