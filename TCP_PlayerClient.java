import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * This class is a plain client class that
 * connects via TCP to a game server to 
 * play some game.
 *
 */
public class TCP_PlayerClient {
	
	// The socket through which this object
	// is connected to the server
	Socket socket;
	
	// Streams for writing and reading
	// objects
	ObjectOutputStream oos;
	ObjectInputStream ois;

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
			long delay = System.currentTimeMillis() - packetTimestamp;
			System.out.println("Total Round Trip Time: " + delay + " ms");
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
	 * This method sets up the TCP connection
	 * and waits for the server to send a game
	 * object. If nothing arrives within a certain
	 * time period, the connection is closed.
	 * @param    playerName   the name of the player
	 * @param    server       an object representing
	 *                        the IP address of the server
	 * @param    port         the port number of the server
	 *                        
	 */
	public void connect( String playerName, InetAddress server, int port ) {
		
		try {
			
			// Let the user know that a connection is pending
			System.out.println("Connecting...");

			// Set up the client's end
			socket = new Socket( server, port );
			oos = new ObjectOutputStream( socket.getOutputStream() );
			ois = new ObjectInputStream( socket.getInputStream() );
			
			// Set the timeout to 10000 milliseconds. If 10000 milliseconds
			// pass while waiting to receive an answer from the server,
			// a SocketException will be thrown and this program will
			// terminate
			socket.setSoTimeout( 10000 );
			
			// Send the player name to the server
			sendData( playerName );
			
			// Get the TCP_Communicator object from the
			// server
			Packet<TCP_Communicator> packet = receiveData();
			TCP_Communicator communicate = ( TCP_Communicator ) packet.getObjectData();
			
			// Return timeout on the socket to infinity
			socket.setSoTimeout( 0 );
			
			// Send the socket, Outputstream, and Inputstream
			// to the TCP_Communicator object.
			communicate.setSocket( socket, oos, ois );
			
			// Start the game
			communicate.game();
			
		} catch ( SocketException e) {
			e.printStackTrace();
		} catch ( SocketTimeoutException e ) {
			System.out.println( "No response." );
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally{
			try {
				
				// Close the socket and streams
				// once the game is over
				socket.close();
				oos.close();
				ois.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}
	
	/**
	 * The main method. It reads in the player's
	 * name, the server's name, and the server's
	 * port number from the command line arguments. 
	 * It then calls the connect() method
	 * to set up the TCP connection and begin the game.
	 * @param   args   command line arguments,
	 *                 used to enter the player's
	 *                 name, server name, and server
	 *                 port
	 */
	public static void main( String [] args ) {
		try {
			
			// The player's name
			String playerName = args[0];
			
			// The name of the server
			String serverName = args[1];
			
			// InetAddress object needed to send
			// DatagramPackets
			InetAddress server = InetAddress.getByName( serverName );
			
			// The server's port
			int port = Integer.parseInt( args[2] ); 
			
			// Set up the UDP connection
			new TCP_PlayerClient().connect( playerName, server, port );	
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}