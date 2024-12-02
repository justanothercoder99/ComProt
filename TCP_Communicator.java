import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketTimeoutException;

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

	public void sendData(Object data) throws IOException, ClassNotFoundException {
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
        } else if (data instanceof int[]) {
            Packet<int[]> packet = new Packet<>((int[]) data);
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
        } else {
            Packet<Object> packet = new Packet<>((Object[]) data);
			packetId = packet.getPacketId();
			packetTimestamp = packet.getTimestamp();
			oos.writeObject( packet );
        }

		System.out.println("Sent Packet ID: " + packetId);
		try {
            socket.setSoTimeout(30000);
            Packet<Void> acknowledgment = (Packet<Void>) ois.readObject();
            System.out.println("Received ACK for Packet ID: " + acknowledgment.getPacketId());
			long delay = acknowledgment.getTimestamp() - packetTimestamp;
			System.out.println("Total Communication Time: " + delay + " ms");
        } catch (SocketTimeoutException e) {
            System.out.println("Acknowledgment timeout for Packet ID: " + packetId);
        }
	}

	public <T> Packet<T> receiveData() throws IOException, ClassNotFoundException {
		socket.setSoTimeout(30000);
		Packet<T> receivedPacket = (Packet<T>) ois.readObject();
		long delay = System.currentTimeMillis() - receivedPacket.getTimestamp();

		System.out.println("Received Packet ID: " + receivedPacket.getPacketId());
		System.out.println("Packet delay: " + delay + " ms");

		Packet<Void> acknowledgment = new Packet<>(receivedPacket.getPacketId(), true);
		oos.writeObject(acknowledgment);
		return receivedPacket;
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
				Packet<Integer> packet = receiveData();
				int length = ( int ) packet.getObjectData();
				
				// Get the user's choice through View and send it
				// to the server for verification
				sendData( view.getPositions( playerName, length ) );
				
				// If the ship could be built in that position
				// and in that direction, a boolean value of true
				// will be returned.
				Packet<Boolean> packet2 = receiveData();
				if( (Boolean) packet2.getObjectData() ) {
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
			Packet<Boolean> packet = receiveData();
			if( (boolean) packet.getObjectData() ){
			
				// Set up the fleet
				fleetSetUp();
							
				// Flag that controls if the game
				// is still being played
				Packet<Boolean> packet2 = receiveData();
				boolean game = (boolean) packet2.getObjectData();
				
				// So long as no one has won, keep playing
				while( game ) {
					
					// Blocks until the server sends a boolean
					// value. Once it receives a value, it must
					// be this player's turn. If the value is 
					// false, then the game is over.
					Packet<Boolean> packet3 = receiveData();
					if( (boolean) packet3.getObjectData() ){
						// The player's ocean
						Packet<String> pkt = receiveData();
						view.message( (String) pkt.getObjectData() );
						pkt = receiveData();
						view.message( (String) pkt.getObjectData() );
						
						// The player's guesses
						pkt = receiveData();
						view.message( (String) pkt.getObjectData() );
						pkt = receiveData();
						view.message( (String) pkt.getObjectData() );
						
						// Get desired target
						sendData( view.getInput(playerName) );
						
						// See if it was a hit or miss
						pkt = receiveData();
						view.message( ( String ) pkt.getObjectData() );

						// The player's guesses
						pkt = receiveData();
						view.message( (String) pkt.getObjectData() );
						pkt = receiveData();
						view.message( (String) pkt.getObjectData() );
					}
					else{
						game = false;
					}
				}
				
				// See who the winner is
				Packet<String> packet4 = receiveData();
				view.message( (String) packet4.getObjectData()); 
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