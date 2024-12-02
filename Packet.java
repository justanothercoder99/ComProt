import java.io.Serializable;
import java.util.Random;

public class Packet<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Random random = new Random();

    private long timestamp;        // For measuring delay
    private int packetId;          // Unique ID for each packet
    private T objectData;  // String array data
    private T[] objectsArray;  // Objects array data


    private boolean isAck = false;   // Boolean to mark packet as ack packet

    // Constructor for string  data
    public Packet(T objectData) {
        this.packetId = generatePacketId();
        this.objectData = objectData;
        this.timestamp = System.currentTimeMillis();
    }

    // Constructor for objects array data
    public Packet(T[] objectsArray) {
        this.packetId = generatePacketId();
        this.objectsArray = objectsArray;
        this.timestamp = System.currentTimeMillis();
    }

    // Constructor for both boolean and string array data
    public Packet(T objectData, T[] objectsArray) {
        this.packetId = generatePacketId();
        this.objectData = objectData;
        this.objectsArray = objectsArray;
        this.timestamp = System.currentTimeMillis();
    }

    // Constructor for ACK packet
    public Packet(int packetId, boolean isAck) {
        this.packetId = packetId;
        this.isAck = isAck;
        this.timestamp = System.currentTimeMillis();
    }

    public static int generatePacketId() {
        return random.nextInt(Integer.MAX_VALUE) + 1; // Ensures ID is positive
    }

    // Getters
    public long getTimestamp() {
        return timestamp;
    }

    public int getPacketId() {
        return packetId;
    }

    public T getObjectData() {
        return objectData;
    }

    public T[] getObjectsArray() {
        return objectsArray;
    }
}