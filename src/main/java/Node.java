/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

/**
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


    private OutboundDataBuffer odb;

    public Node(int x, int y) {
        this.x = x;
        this.y = y;
        this.id = String.valueOf(Utils.getNextNodeId());
        this.rt = new RoutingTable();
        this.orb = new OutboundRequestBuffer();
        this.odb = new OutboundDataBuffer(this);
        this.sb = new SendBuffer(this);
        this.rreql = new RoutingRequestLog();

        this.nib = new NetworkInboundBuffer(this);
        this.nob = new NetworkOutboundBuffer(this);

        this.nib.start(); //start inbound buffer
        this.nob.start(); //start outbound buffer

        this.orb.start(); //start timeout check - RREQ
        this.odb.start(); //start timeout check - DATA
    }

    public void sendHello(Node dest) {
        ;
        //check routing table if route was already discovered
        if (this.rt.exists(dest.getId())) {
            try {
                RoutingTable.RoutingTableEntry rte = this.rt.get(dest.getId());

                //route was found send data packet
                Packet dataP = PacketFactory.newDataPacket(UUID.randomUUID().toString(), this.id, dest.getId(), this.id, rte.route);

                System.out.println("I " + this.id + " will use my cached route for this " + rte.route);

                this.getODB().put(dataP);
                this.getNOB().addLast(dataP);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //do the route discovery and put packets in sendbuffer
            Packet rreqP = PacketFactory.newRREQPacket(UUID.randomUUID().toString(), this.id, dest.getId(), this.id, this.id);

            this.orb.put(rreqP); //put entry in orb - wait for rres to arrive
            this.sb.put(rreqP.dest); //also put entry in sb - wait for route discovery to finish
            this.getNOB().addLast(rreqP);
        }
    }

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
                    VisualizationLogger.setTableUpdateAndSaveSnapshot("update", this.id, rte.dest, rte.route);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (isNew) { //table entry is new send buffer can now send via this entry
                this.rt.put(rte);
                VisualizationLogger.setTableUpdateAndSaveSnapshot("add", this.id, rte.dest, rte.route);
                this.sb.notifyRouteDiscovered(rte);
            }
        } finally {
            notifyAll();
        }

    }

    public synchronized void removeBrokenTableEntries(String brokenLink, boolean isOriginalSender) {
        try {
            //System.out.println("node " + this.getId() + " : removing broken link from routing table: "+ brokenLink);
            String[] destinations = this.rt.keys();
            //remove all entries, that have routes that use the brokenLink
            for (int i = 0; i < destinations.length; i++) {
                if (Utils.routeContainsLink(this.rt.get(destinations[i]).route, brokenLink)) {
                    System.out.println("Node " + this.getId() + ": removed route to " + destinations[i] + "(" + this.rt.get(destinations[i]).route + ")");
                    this.rt.remove(destinations[i]);
                    VisualizationLogger.setTableUpdateAndSaveSnapshot("remove", this.id, destinations[i], this.rt.get(destinations[i]).route);
                    if(isOriginalSender) {

                        //do the route discovery to find a new route
                        System.out.println("Node " + this.getId() + ": starting new Route Discovery to " + destinations[i]);
                        Packet rreqP = PacketFactory.newRREQPacket(UUID.randomUUID().toString(), this.id, destinations[i], this.id, this.id);

                        this.orb.put(rreqP); //put entry in orb - wait for rres to arrive
                        this.getNOB().addLast(rreqP);
                    }
                }
            }
        }  catch (Exceptions.RoutingEntryNotFoundException e) {
            //e.printStackTrace();
        }
    }



    //getter and setter methods
    public int getX() {
        return x;
    }

    public  void setX(int x) {
        this.x = x;
    }

    public  int getY() {
        return y;
    }

    public  void setY(int y) {
        this.y = y;
    }

    /**
     * moves the node to a new random position
     */
    public void move() {
        int x;
        int y;
        do {
            Random r = new Random();
            x = r.nextInt((Config.X_SIZE + 1) - 0) + 0;
            y = r.nextInt((Config.Y_SIZE + 1) - 0) + 0;
        } while(Config.field.hasNodeAt(x,y));
            this.setX(x);
            this.setY(y);
        System.out.println("moved node " + this.getId() + " to X:" + x + " ,Y: " + y);

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

    public OutboundDataBuffer getODB() {
        return odb;
    }

    public void setODB(OutboundDataBuffer odb) {
        this.odb = odb;
    }

    public boolean isInReach(Node other) {
        int xDistance = Math.abs(this.getX() - other.getX());
        int yDistance = Math.abs(this.getY() - other.getY());
        double distance = Math.sqrt(xDistance * xDistance + yDistance * yDistance);
        return distance <= Config.NODE_REACH;
    }
}
