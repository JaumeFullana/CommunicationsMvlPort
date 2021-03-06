/*
 * This project is given as is with license GNU/GPL-3.0. For more info look
 * on github
 */
package communications;

import android.content.Context;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import vista.MainActivity;

/**
 * TO DO: Check if we need to use public, private, default or protected for our methods
 * @author masa
 */
public class CommunicationController {
    
    public final int PORT;
    public final int SERVERHEALTHMAXWAIT;
    public final int ACKMAXWAIT;
    
    static final int PC = 37;
    static final int MVL = 38;
    
    static final int SERVER = 24;
    static final int CLIENT = 25;
    
    static final int MAXATTEMPTS = 30;
    
    final ArrayList<Connection> pcConnections = new ArrayList<>();
    private ArrayList<Connection> mobileConnections = new ArrayList<>();
    private final int maxPc;
    private ServerConnector serverConn;
    private ClientConnector clientConn;
    private ConnectionInterfaceInitiater initiater;
    private String localMAC;
    private InetAddress localIP;
    Protocol protocol;
    private Context context;
    
    /**
     * Empty constructor, starts port as 42069, healthwait 1500, ackwait = 2500
     * and maxPc = 2
     */
    public CommunicationController(Context context){
        this.context = context;
        PORT = 42069;
        SERVERHEALTHMAXWAIT = 1500;
        ACKMAXWAIT = 2500;
        maxPc = 2;
        libraryStarter();
    }
    
    /**
     * Constructor with port and maxPc as parameters, healthwait will start as 1500
     * and ackwait as 2500.
     * @param port Port that will be used by the sockets
     * @param maxPc Max amount of connected PCs at the same time, will be overriden if someone uses more and both connect
     */
    public CommunicationController(int port, int maxPc, Context context){
        this.context = context;
        PORT = port;
        SERVERHEALTHMAXWAIT = 1500;
        ACKMAXWAIT = 2500;
        if(maxPc <= 1){
            this.maxPc = 2;
        }else{
            this.maxPc = maxPc;
        }
        libraryStarter();
    }
    
    /**
     * Constructor asking for every possible parameter
     * @param port Port that will be used by the sockets
     * @param serverWait Amount of time to wait between messages to send the server health petition
     * @param ackWait Amount of time to wait for an acknowledge signal by server health
     * @param maxPc Max amount of connected PCs at the same time, will be overriden if someone uses more and both connect
     */
    public CommunicationController(int port, int serverWait, int ackWait, int maxPc, Context context){
        this.context = context;
        PORT = port;
        SERVERHEALTHMAXWAIT = serverWait;
        ACKMAXWAIT = ackWait;
        if(maxPc <= 1){
            this.maxPc = 2;
        }else{
            this.maxPc = maxPc;
        }
        libraryStarter();
    }
    
    /**
     * Method used to initialize all needed classes, methods and threads of the library
     */
    private void libraryStarter(){
        
        
        protocol = new Protocol();
        
        for(int i = 0; i < maxPc; i++){
            Connection conn = null;
            synchronized(this.pcConnections){
                pcConnections.add(conn);
            }
        }
        
        initiater = new ConnectionInterfaceInitiater();
        
        serverConn = new ServerConnector(this);
        clientConn = new ClientConnector(this);
        
        
        
        Thread serverThread = new Thread(serverConn);
        Thread clientThread = new Thread(clientConn);
        
        serverThread.start();
        clientThread.start();
        
        try{
            Thread.sleep(500);
        }catch(Exception ex){
            System.out.println("FUUUUCK");
        }
        
        this.localMAC=this.getMac();
    }

    public InetAddress getLocalIP() {
        return localIP;
    }

    public void setLocalIP(InetAddress localIP) {
        this.localIP = localIP;
    }
    
    public String getLocalMAC() {
        return localMAC;
    }
    
    /**
     * Adds a ConnectionInterface to the initiater to be called
     * as an event when a packet is received.
     * @param conn A class implementing ConnectionInterface
     */
    public void addOnPacketListener(ConnectionInterface conn){
        initiater.addPacketListener(conn);
    }
    
    /**
     * Adds a ConnectionInterface method to the initiater to be called
     * as an event when a connection is accepted
     * @param conn 
     */
    public void addOnConnectionListener(ConnectionInterface conn){
        initiater.addConnectionListener(conn);
    }
    
    /**
     * Adds a ConnectionInterface method to the initiater to be called 
     * as an event when a connection is fully closed
     * @param conn 
     */
    public void addOnDisconnectListener(ConnectionInterface conn){
        initiater.addClosingListener(conn);
    }
    
    /**
     * Adds all of the above listeners at the same time
     * @param conn 
     */
    public void addAllListeners(ConnectionInterface conn){
        initiater.addPacketListener(conn);
        initiater.addConnectionListener(conn);
        initiater.addClosingListener(conn);
    }
    
    /**
     * Creates a new ProtocolDataPacket
     * @param targetMac String with the mac address to send the message to
     * @param packetId ID of the packet to know how to react
     * @param data Object to be sent
     * @return 
     */
    public ProtocolDataPacket createPacket(String targetMac, int packetId, Object data){
        ProtocolDataPacket packet = new ProtocolDataPacket(this.localMAC, targetMac, packetId, data);
        return packet;
    }
    
    /**
     * Attempts to connect to the given ip
     * @param ip String with the ip direction to connect to
     */
    public void connectToIp(String ip){
        this.getClientSocket(this.clientConn.connect(ip));
    }
    
    /**
     * Sends this message via the fastest available path. Also checks if the packet
     * is valid.
     * @param packet Packet to be sent
     * @return True if packet is valid and sent, else false
     */
    public boolean sendMessage(ProtocolDataPacket packet){
        return checkPacket(packet);
    }
    
    /**
     * Checks if the packet is valid and also sends it to resend to send it via the fastest path available.
     * Also overrides the packet source MAC to use this one
     * @param packet A packet to be checked and sent
     * @return True if packet is valid, else false
     */
    private boolean checkPacket(ProtocolDataPacket packet){
        boolean isValid = true;
        if(packet.getId() > protocol.getMinId() && packet.getTargetID() != null && !packet.getTargetID().equals(localMAC)){
            //send based on target id
            sendPacket(null, new ProtocolDataPacket(localMAC,packet.getTargetID(),packet.getId(),packet.getObject()));
        }else{
            isValid = false;
        }
        return isValid;
    }
    
    /**
     * Returns all the pc and mobile connections concatenated
     * @return Pc + Mobile connections arraylist
     */
    private ArrayList getAllConnections(){
        ArrayList<Connection> joinedMacs = new ArrayList();
        synchronized(this.pcConnections){
            joinedMacs.addAll(this.pcConnections);
        }
        joinedMacs.addAll(this.mobileConnections);
        return joinedMacs;
    }
    
    /**
     * Removes the connection which mac address is received by parameter
     * and sends the corresponding closure protocol
     * @param mac String with the mac of a connection
     */
    public void disconnect(String mac){
        ArrayList<Connection> joinedMacs = this.getAllConnections();
        for(Connection conn : joinedMacs){
            if(conn != null){
                if(conn.getConnectedMAC().equals(mac)){
                    conn.notifyClousure();
                    break;
                }
            }
        }
    }
    
    /**
     * Serach for all the MACs in all the lookup tables of every connection. 
     * @return ArrayList where are all the MACs
     */
    public ArrayList<String> getConnectedMacs(){
        ArrayList <String> connectedMacs=new ArrayList<>();
        HashMap <String,Integer> mapMacs=this.joinMaps();
            for (String mac:mapMacs.keySet()){
                if (!mac.equals(this.localMAC)){
                    connectedMacs.add(mac);
                }
            }
        return connectedMacs;
    }
    
    /**
     * Sends a broadcast Message to all the connections stored in the lookup tables.
     * @param comand the comand given to the message
     * @param data the data stored in the message
     */
    public void sendBroadcastMessage (int comand, Object data){
        HashMap <String,Integer> joinedLookup=this.joinMaps();
        HashMap <String,Connection> macPaths=this.connectMaps(joinedLookup);
        for (String mac: macPaths.keySet()){
            macPaths.get(mac).send(new ProtocolDataPacket(this.localMAC, mac, comand, data));
        }
    }
    
    /**
     * Sends a message to the closest neighbors
     * @param command Id of the packet
     * @param data Object to be sent with the packet
     */
    public void sendToNeighbors(int command, Object data){
        synchronized(this.pcConnections){
            for(Connection e : this.pcConnections){
                if(e != null){
                    e.send(new ProtocolDataPacket(this.localMAC,e.getConnectedMAC(),command,data));
                }
            }
        }
    }
    
    /**
     * Private getter of mobileConnections
     * @return 
     */
    ArrayList<Connection> getMobileConnections(){
        return this.mobileConnections;
    }
    
    /**
     * Receives a packet to be sent via the best path using the lookup tables
     * @param conn Connection to be avoided, can be null if there is no connection to be avoided
     * @param packet Packet to be sent
     */
    void sendPacket(Connection conn, ProtocolDataPacket packet){
        ArrayList<Connection> allConnections = this.getAllConnections();
        boolean found = false;
        Connection leastJumps = null;
        int minJumps = -1;
        int aux;
        for(Connection e : allConnections){
            if(e != null && e != conn){
                if(e.getConnectedMAC() != null){
                    aux = e.getJumpsTo(packet.getTargetID());
                    if(aux != -1){
                        if(aux == 1){
                            e.send(packet);
                            found = true;
                            break;
                        }else{
                            if(minJumps == -1 || aux < minJumps){
                                leastJumps = e;
                                minJumps = aux;
                            }
                        }
                    }
                }
            }
        }
        if(!found){
            leastJumps.send(packet);
        }
    }
    
    /**
     * Joins the lookup tables of the pc connections into 1
     * @return Big lookup table of all the connections
     */
    HashMap<String, Integer> joinMaps(){
        HashMap<String,Integer> map = new HashMap<>();
        HashMap<String,Integer> pointerMap = new HashMap<>();
        ArrayList<Connection> allConnections = this.getAllConnections();
        for(Connection e : allConnections){
            if(e != null){
                pointerMap = e.getLookup();
                for(String macs : pointerMap.keySet()){
                    if(!map.containsKey(macs)){
                        map.put(macs, e.getJumpsTo(macs));
                    }else{
                        if(map.get(macs) > e.getJumpsTo(macs)){
                            map.replace(macs, e.getJumpsTo(macs));
                        }
                    }
                }
            }
        }
        map.put(localMAC, 0);
        return map;
    }

    /**
     * Returns a boolean if there's enough space for a new connection
     * @return Boolean true if there's space, false if there isn't
     */
    boolean availableConnections(){
        boolean available = false;
        for(int i = 0; i < maxPc && available == false; i++){
            synchronized(this.pcConnections){
                if(pcConnections.get(i) == null){
                    available = true;
                }else{
                    if(pcConnections.get(i).getIp() == null && pcConnections.get(i).getSocket() == null){
                        available = true;
                    }
                }
            }
        }
        return available;
    }
    
    /**
     * When a connection is accepted it gets sent to this socket
     * @param socket Accepted socket
     */
    void getServerSocket(Socket socket){
        try{
            Connection conn = new Connection(this, socket,initiater, protocol);
            conn.setConnectionType(SERVER);
            Thread thread = new Thread(conn);
            thread.start();
        }catch(IOException ex){
            System.out.println("Problem creating a connection: " + ex.getMessage());
        }
    }
    
    /**
     * When a connection is started this method gets called with the accepted socket
     * @param socket 
     */
    private void getClientSocket(Socket socket){
        try{
            Connection conn = new Connection(this, socket,initiater, protocol);
            Thread thread = new Thread(conn);
            thread.start();
        }catch(IOException ex){
            System.out.println("Problem creating a connection: " + ex.getMessage());
        }
    }
    
    /**
     * Adds a controller connection to the mobileConnections ArrayList
     * @param conn Connection with a controller
     */
    void addMobileConnection(Connection conn){
        if(!sameMobileConnection(conn)){
            ServerHealth health = new ServerHealth(this,conn);
            conn.setServerHealth(health);
            mobileConnections.add(conn);
        }
    }
    
    /**
     * Adds a new Pc connection to the pcConnections arrayList
     * @param conn A handshaken pc connection
     */
    void addPcConnection(Connection conn){
        if(availableConnections()){
            if(!samePcConnection(conn)){
                ServerHealth health = new ServerHealth(this,conn);
                conn.setServerHealth(health);
                synchronized(this.pcConnections){
                    pcConnections.add(conn);
                }
            }
        }
    }
    
    /**
     * Checks if the connection passed by parameter is already existing
     * Checked to avoid adding already existing connections usually due to a serverhealth reconnection
     * @param conn Handshaken connection
     * @return True if already existing, else false
     */
    private boolean samePcConnection(Connection conn){
        boolean same = false;
        synchronized(this.pcConnections){
            for(Connection con : this.pcConnections){
                if(con != null && con.getConnectedMAC() != null && con.getConnectedMAC().equals(conn.getConnectedMAC())){
                    same = true;
                    break;
                }
            }
        }
        return same;
    }
    
    /**
     * Checks if the controller connection passed by parameter is already existing
     * Checked to avoid adding already existing connections usually due to a serverhealth reconnection
     * @param conn
     * @return True if already existing, else false
     */
    private boolean sameMobileConnection(Connection conn){
        boolean same = false;
        for(Connection con : this.mobileConnections){
            if(con != null && con.getConnectedMAC() != null && con.getConnectedMAC().equals(conn.getConnectedMAC())){
                same = true;
                break;
            }
        }
        return same;
    }
    
    /**
     * Puts a closed connection to null to avoid ClientConnector reconnecting to it
     * @param conn Closed connection
     */
    void nullifyConnection(Connection conn){
        synchronized(this.pcConnections){
            if(this.pcConnections.contains(conn)){
                this.pcConnections.set(this.pcConnections.indexOf(conn), null);
            }else if(this.mobileConnections.contains(conn)){
               this.mobileConnections.set(this.mobileConnections.indexOf(conn), null);
            }
        }
    }
    
    /**
     * Creates a new HashMap based on the joined map where every mac has the connection
     * with the fastest path to a mac address.
     * @param joinedMap HashMap of all lookup tables joined with the fastest paths
     * @return A HashMap with the fastest connection for a given mac address
     */
    HashMap<String, Connection> connectMaps(HashMap<String, Integer> joinedMap){
        HashMap<String, Connection> returnMap = new HashMap();
        ArrayList<Connection> allConnections = this.getAllConnections();
        for(String str : joinedMap.keySet()){
            for(Connection e : allConnections){
                if(e.getLookup().containsKey(str) && e.getLookup().get(str).equals(joinedMap.get(str))){
                    returnMap.put(str, e);
                    break;
                }
            }
        }
        return returnMap;
    }
    
    /**
     * Gets any valid mac of the pc
     * @return Returns a String of the mac address
     */
    private String getMac(){
        try{
            String address = android.provider.Settings.Secure.getString(
                    context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            return address;
        }catch(Exception ex){
            ex.printStackTrace();
            System.out.println("Couldnt get Mac address");
            return null;
        }
    }
    
    /**
     * Attempts to connect to the given ip
     * @param ip String with the ip direction to connect to
     */
    public void connectToIp(InetAddress ip){
        this.getClientSocket(clientConn.connect(ip, PORT));
    }

    ArrayList<InetAddress> getConnectedPcsIps(){
        ArrayList <InetAddress> connectionsIps=new ArrayList<>();
        synchronized(this.pcConnections){
            for (Connection con:this.pcConnections){
                if (con!=null){
                    connectionsIps.add(con.getIp());
                }
            }
        }
        return connectionsIps;
    }
    
    /**
     * Close all the pc connections. You can pass a connection to not close that 
     * connections in this moment.
     * @param conn the connection to not close or null
     */
    public void closePcConnections(Connection conn){
        synchronized(this.pcConnections){
            for (Connection con:this.pcConnections){
                if (con!=null && (conn==null || con!=conn)){
                    con.notifyClousure();
                }
            }
        }
    }
    
    /**
     * Close all the mobile connections. You can pass a connection to not close that 
     * connections in this moment.
     * @param conn the connection to not close or null
     */
    public void closeMobileConnections(Connection conn){
        synchronized(this.mobileConnections){
            for (Connection con:this.mobileConnections){
                if (con!=null && (conn==null || con!=conn)){
                    con.notifyClousure();
                }
            }
        }
    }
    
    /**
     * Close all the connections. You can pass a connection to not close that 
     * connections in this moment.
     * @param conn the connection to not close or null
     */
    public void closeAllConnections(Connection conn){
        this.closePcConnections(null);
        this.closeMobileConnections(null);
    }
    
    /**
     * Create new connections getted from a inetAddress ArrayList
     * @param ips ArrayList of ips in InetAddress form
     * @param changerPositionIp ip of the pc that are switching positions with you
     */
    void createNewConnections(ArrayList <InetAddress> ips, String changerPositionIp){
        for (InetAddress ip:ips){
            if (localIP!=ip){
                this.connectToIp(ip);
            } else {
                this.connectToIp(changerPositionIp);
            }
        }
    }
}
