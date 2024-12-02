import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

/**
 * This class handles the connection to a
 * player for the server. It also contains part 
 * of the logic needed to play the game.
 * @author   Kyle McGlynn
 * @author   Ajinkya Kolhe
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
        } else if (data instanceof String) {
            Packet<String> packet = new Packet<>((String) data); // Wrap the string in a string array
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
            client.setSoTimeout(30000);
            Packet<Void> acknowledgment = (Packet<Void>) ois.readObject();
            System.out.println("Received ACK for Packet ID: " + acknowledgment.getPacketId());
			long delay = System.currentTimeMillis() - packetTimestamp;
			System.out.println("Total Round Trip Time: " + delay + " ms");
        } catch (SocketTimeoutException e) {
            System.out.println("Acknowledgment timeout for Packet ID: " + packetId);
        }
	}

	public <T> Packet<T> receiveData() throws IOException, ClassNotFoundException {
		client.setSoTimeout(30000);
		Packet<T> receivedPacket = (Packet<T>) ois.readObject();
		long delay = System.currentTimeMillis() - receivedPacket.getTimestamp();

		System.out.println("Received Packet ID: " + receivedPacket.getPacketId());
		System.out.println("Packet delay: " + delay + " ms");

		Packet<Void> acknowledgment = new Packet<>(receivedPacket.getPacketId(), true);
		oos.writeObject(acknowledgment);
		return receivedPacket;
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
				sendData( new Integer(index) );
				
				// Get the desired starting point and direction
				Packet<Object> packet = receiveData();
				Object[] inputs = packet.getObjectsArray();
				
				
				// If the ship can be built, increment index
				if( server.fleetSetUp( player, inputs ) ) {
					
					// If the ship was built, return true
					sendData( new Boolean(true) );
					index++;
				}
				else{
					sendData( new Boolean(false) );
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
			sendData( new Boolean( play ) );
			
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
				sendData( new Boolean(true) );
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
					sendData(new Boolean(true));
					
					// Send the current player's ocean with their ships
					sendData( new String("Your Fleet") );
					sendData( server.printOcean( turn, true ) );
					
					// Send the waiting player's ocean without their
					// ships. This is so that the guessing player
					// can see their hits and misses.
					sendData( new String("Your Attacks") );
					sendData( server.printOcean( turn + sign, false ) );
					
					// Get the current player's desired target
					Packet<int[]> packet = receiveData();
					int[] target = packet.getObjectData();
											
					// Pass the target to the model. If it returns 
					// true, then it hit, otherwise it is a miss.
					if( server.checkHit(turn+sign, target, turn)){
						sendData( new String( "Hit!" ) );
					}
					else {
						sendData( new String( "Miss!" ) );
					}

					sendData( new String("Opponent Ocean") );
					sendData( server.printOcean( turn + sign, false ) );
					// Change the turn
					server.changeTurn();
				} else {
					Thread.sleep(1000); // Sleep for 1000ms to avoid overloading CPU
					elapsedTime += 1000;
				}
			}
			
			// Indicate to the player that the game is over
			sendData( new Boolean( false ) );
			
			// Tell the player who won
			sendData( server.victory() );
		} catch ( IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}