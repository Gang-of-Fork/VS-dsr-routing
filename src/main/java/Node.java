/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.ArrayList;
import java.util.UUID;

/**
 *
 * @author Fwy
 */
public class Node {

    private int x;
    private int y;
    private String id;
    private RoutingTable rt;
    private OutboundRequestBuffer orb;
    private SendBuffer sb;
    private RoutingRequestLog rreql;
    private NetworkInboundBuffer nib;
    private NetworkOutboundBuffer nob;

    public Node(int x, int y) {
        this.x = x;
        this.y = y;
        this.id = String.valueOf(Utils.getNextNodeId());
        this.rt = new RoutingTable();
        this.orb = new OutboundRequestBuffer();
        this.sb = new SendBuffer(this);
        this.rreql = new RoutingRequestLog();

        this.nib = new NetworkInboundBuffer(this);
        this.nob = new NetworkOutboundBuffer(this);

        this.nib.start(); //start inbound buffer
        this.nob.start(); //start outbound buffer

        this.orb.start(); //start timeout check
    }

    public void receive(Packet p) {
        RequestProcessor.process(this, p);
    }

    public void sendHello(Node dest) {;
        //check routing table if route was already discovered
        if (this.rt.exists(dest.getId())) {
            try {
                RoutingTable.RoutingTableEntry rte = this.rt.get(dest.getId());

                //route was found send data packet
                Packet p = PacketFactory.newDataPacket(UUID.randomUUID().toString(), this.id, dest.getId(), this.id, rte.route);

                System.out.println("I " + this.id + " will use my cached route for this " + dest.id);

                this.getNOB().addLast(p);;
            } catch(Exception e) {
                e.printStackTrace();
            }
        } else {
            //do the route discovery and put packets in sendbuffer
            Packet p = PacketFactory.newRREQPacket(UUID.randomUUID().toString(), this.id, dest.getId(), this.id, this.id);

            this.orb.put(p); //put entry in orb - wait for rrep to arrive
            this.sb.put(p.dest); //also put entry in in sb - wait or route discovery to finish
            this.getNOB().addLast(p);
        }
    }
/*
    public void send(Packet p) {
        //discover neighbours and send packet via flooding
        ArrayList<Node> neighbours = Config.field.discover(this);

        //System.out.println(this.id + " discovered neighbours " + neighbours.size());
        for (int i = 0; i < neighbours.size(); i++) {
            neighbours.get(i).receive(p);
        }
    }
*/
    public void addRoutingTableEntryFromPacket(Packet p) {
        //construct routing table with information derived from request
        ArrayList<RoutingTable.RoutingTableEntry> rtes = p.extractRoutingTableEntries(this.id);

        for (int i = 0; i < rtes.size(); i++) {
            //edit routing table and notify node sendbuffer that route was discovered
            this.addRoutingTableEntry(rtes.get(i));
        }
    }

    public synchronized void addRoutingTableEntry(RoutingTable.RoutingTableEntry rte) {
        //System.out.println(this.id + ": adding new route " + rte.toString());

        try {
            Boolean isNew = !this.rt.exists(rte.dest);

            try {
                //only update entry if new entry has less hops than old entry
                if (!isNew && (rte.numOfHops() < this.rt.get(rte.dest).numOfHops())) {
                    this.rt.put(rte);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (isNew) { //table entry is new send buffer can now send via this entry
                this.rt.put(rte);
                this.sb.notifyRouteDiscovered(rte);
            }
        } finally {
            notifyAll();
        }

    }

    //getter and setter methods
    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getId() {
        return this.id;
    }

    public OutboundRequestBuffer getORB() {
        return this.orb;
    }

    public RoutingTable getRT() {
        return this.rt;
    }

    public SendBuffer getSB() {
        return this.sb;
    }
    
    public RoutingRequestLog getRREQL() {
        return this.rreql;
    }

    public NetworkInboundBuffer getNIB() {
        return nib;
    }

    public void setNIB(NetworkInboundBuffer nib) {
        this.nib = nib;
    }

    public NetworkOutboundBuffer getNOB() {
        return nob;
    }

    public void setNOB(NetworkOutboundBuffer nob) {
        this.nob = nob;
    }

    public boolean isInReach(Node other) {
        int xDistance = Math.abs(this.getX() - other.getX());
        int yDistance = Math.abs(this.getY() - other.getY());
        double distance = Math.sqrt(xDistance * xDistance + yDistance * yDistance);
        return distance <= Config.NODE_REACH;
    }
}
