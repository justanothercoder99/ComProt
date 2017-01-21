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
import java.net.UnknownHostException;

/**
 * This class is a plain client class that
 * connects via UDP to a game server to 
 * play some game.
 * @author   Kyle McGlynn
 * @author   Ajinkya Kolhe
 *
 */
public class UDP_PlayerClient {
	
	/**
	 * This method sets up the UDP connection
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
		
		// The client's end
		DatagramSocket socket = null;
		
		try {
			
			// Set up the client's end
			socket = new DatagramSocket();
			
			// Set the timeout to 1000 milliseconds. If 1000 milliseconds
			// pass while waiting to receive an answer from the server,
			// a SocketException will be thrown and this program will
			// terminate
			socket.setSoTimeout( 1000 );
						
			// Send the player's name to the server
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream( baos );
			oos.writeObject(playerName);
			byte[] send = baos.toByteArray();
			
			// A DatagramPacket to send to the server
			DatagramPacket packet = new DatagramPacket( 
					send, send.length, server, port );
			
			// Send the packet
			socket.send(packet);
			
			// A byte array. It is used to receive data from a DatagramPacket
			byte receive[] = new byte[4096];
			
			// Create a packet to receive whatever the server may send
			packet = new DatagramPacket( receive, receive.length );
			socket.receive(packet);
						
			// Although these streams are not used for I/O, they
			// are useful in converting bytes back into objects
			ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData());
			ObjectInputStream ois = new ObjectInputStream(bais);
			UDP_Communicator communicate = (UDP_Communicator) ois.readObject();
			
			// Set the timeout to infinity
			socket.setSoTimeout(0);
			
			// Pass the socket to the UDP_Communicator object
			communicate.setSocket( socket );
			
			// Start the game
			communicate.game();
			
		} catch ( SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch ( SocketTimeoutException e ) {
			System.out.println( "No response." );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			socket.disconnect();
			socket.close();
		}		
	}
	
	/**
	 * The main method. It reads in the player's
	 * name, the server's name, and the desired
	 * client port number from the command line
	 * arguments. It then calls the connect() method
	 * to set up the UDP connection and begin the game.
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
			new UDP_PlayerClient().connect( playerName, server, port );	
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}