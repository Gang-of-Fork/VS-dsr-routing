/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

/**
 * @author Fwy
 */
public class RequestProcessor extends Thread {

    public static ArrayList<RequestProcessor> activeProcessors = new ArrayList();
    private static Semaphore processorsMutex = new Semaphore(1);
    private Node node;
    private Packet packet;

    private RequestProcessor(Node n, Packet p) {
        this.node = n;
        this.packet = p;
    }

    public static void process(Node n, Packet p) {
        RequestProcessor processor = new RequestProcessor(n, p);
        processor.start();

        addProcessor(processor);
    }

    private static synchronized void addProcessor(RequestProcessor rp) {
        try {
            processorsMutex.acquire();

            activeProcessors.add(rp);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            processorsMutex.release();
        }
    }

    public void run() {
        switch (this.packet.type) {
            case RREQ:
                this.rreq();
                break;
            case RRES:
                this.rres();
                break;
            case RERR:
                this.rerr();
                break;
            case DATA:
                this.data();
                break;
            case ACK:
                this.ack();
        }
    }

    public void ack() {
        try {
            String nextNodeId = this.packet.getNextNodeId();
            if (this.node.getId().equals(nextNodeId)) {
                //ACK has same packetID as the original DataPacket
                System.out.println("Node " + this.node.getId() + ":received ACK for packet " + this.packet.id + " , removing it from ODB");
                this.node.getODB().pop(this.packet.id);
            }
        } catch (Exceptions.NoMatchingBufferEntryException | Exceptions.IncompatiblePacketTypeException e) {
            e.printStackTrace();
        }
    }

    public void rres() {
        //System.out.println(this.node.getId() + " Received RRES");
        String reqId = this.packet.id; //extract id from request


        if (this.packet.isDestination(this.node)) {
            try {
                OutboundRequestBuffer.OutboundRequestBufferEntry orbe = this.node.getORB().pop(reqId);
            } catch (Exception e) {
            }

            //Construct Routing Table Entry
            this.node.addRoutingTableEntryFromPacket(this.packet);

        } else {
            //System.out.println(this.node.getId() + " is not destination");

            try {
                //check if I am the next entry in the route - if so broadcast
                String nextNodeId = this.packet.getNextNodeId();

                if (this.node.getId().equals(nextNodeId)) {
                    this.node.getORB().pop(reqId); //remove entry from orb - received response

                    Packet p = PacketFactory.newRRESPacket(this.packet.id, this.packet.source, this.packet.dest, this.node.getId(), this.packet.route);

                    //Routing Requests from other routers can also be used to discover own routing table
                    //Construct Routing Table Entry
                    this.node.addRoutingTableEntryFromPacket(this.packet);

                    this.node.getNOB().addLast(p);
                }
            } catch (Exception e) {
            } //do nothing maybe entry expired or I already broadcastet
        }
    }

    public void rreq() {
        //System.out.println("Node " + this.node.getId() + " received rreq " + this.packet.id);
        String newPath = this.packet.route + Config.PATH_DELIMITER + this.node.getId();

        //if node that received package is destination answer with rres packet
        //but not, if a rres packet has been sent for this rreq before
        if (this.packet.isDestination(this.node) && !this.node.getRREQL().contains(this.packet.id)) {
            //reverse route to get route back to sender
            String reversedRoute = Utils.reverseRoute(newPath);

            //reverse source and destination and send discovered route to sender
            Packet p = PacketFactory.newRRESPacket(this.packet.id, this.packet.dest, this.packet.source, this.node.getId(), reversedRoute);

            this.node.addRoutingTableEntryFromPacket(p);
            this.node.getRREQL().add(this.packet.id);
            this.node.getNOB().addLast(p);
        } //only forward packet if current node has not forwarded in the past
        else if (!this.packet.route.contains(this.node.getId()) && !this.node.getRREQL().contains(this.packet.id)) {
            //just forward the packet as it as and put packet in orb to wait for ack
            Packet p = PacketFactory.newRREQPacket(this.packet.id, this.packet.source, this.packet.dest, this.node.getId(), newPath);
            this.node.getORB().put(p);
            this.node.getRREQL().add(this.packet.id);

            this.node.getNOB().addLast(p);
        }
    }

    public void rerr() {
        try {

            //remove all broken routes from RT
            String brokenLink = this.packet.getBrokenLink();
            this.node.removeBrokenTableEntries(brokenLink, this.packet.isDestination(this.node));
            //System.out.println(this.node.getId() + ": received RERR Packet from " + this.packet.sender);

            String nextNodeId = this.packet.getNextNodeId();
            //if current node is not the destination, but the message was meant for this destination pass it on
            if (this.node.getId().equals(nextNodeId) && !this.packet.isDestination(this.node)) {
                Packet p = PacketFactory.newRERRPacket(this.packet.id, this.packet.source, this.packet.dest, this.node.getId(), this.packet.route);
                System.out.println(this.node.getId() + " ich leite RERR weiter an " + p.getNextNodeId());
                this.node.getNOB().addLast(p);

            }

        } catch (Exceptions.IncompatiblePacketTypeException | Exceptions.NoBrokenLinksException e) {
            //e.printStackTrace();

        }
    }

    public void data() {
        try {
            String nextNodeId = this.packet.getNextNodeId();
            if (this.node.getId().equals(nextNodeId)) {
                //send ACK to sender
                Packet ack = PacketFactory.newAckPacket(this.packet.id, this.node.getId(), this.packet.sender, this.node.getId(), this.node.getId() + Config.PATH_DELIMITER + this.packet.sender);
                this.node.getNOB().addLast(ack);

                //forward packet, if current node is not the destination
                if (this.packet.isDestination(this.node)) {
                    System.out.println("\u001B[31m" + this.node.getId() + " ich habe erhalten von " + this.packet.source + ":" + this.packet.id + "\u001B[0m");
                } else {
                    System.out.println(this.node.getId() + " ich leite weiter");
                    Packet p = PacketFactory.newDataPacket(this.packet.id, this.packet.source, this.packet.dest, this.node.getId(), this.packet.route);

                    this.node.getODB().put(p);
                    this.node.getNOB().addLast(p);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
