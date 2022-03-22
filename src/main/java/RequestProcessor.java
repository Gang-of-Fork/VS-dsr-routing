/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 *
 * @author Fwy
 */
public class RequestProcessor extends Thread {

    public static ArrayList<RequestProcessor> activeProcessors = new ArrayList();
    private static Semaphore processorsMutex = new Semaphore(1);

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

    private Node node;
    private Packet packet;

    private RequestProcessor(Node n, Packet p) {
        this.node = n;
        this.packet = p;
    }

    public void run() {
        switch (this.packet.type) {
            case RREQ:
                this.rreq();
                break;
            case RREP:
                this.rrep();
                break;
            case RERR:
                this.rerr();
                break;
            case DATA:
                this.data();
                break;
        }
    }

    public void rrep() {
        //System.out.println(this.node.getId() + " Received RREP");
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

                    Packet p = PacketFactory.newRREPPacket(this.packet.id, this.packet.source, this.packet.dest, this.node.getId(), this.packet.route);

                    //Routing Requests from other routers can also be used to discover own routing table
                    //Construct Routing Table Entry
                    this.node.addRoutingTableEntryFromPacket(this.packet);

                    this.node.send(p);
                }
            } catch (Exception e) {
            } //do nothing maybe entry expired or I already broadcastet
        }
    }

    public void rreq() {
        //System.out.println("Node " + this.node.getId() + " received rreq " + this.packet.id);
        String newPath = this.packet.route + Config.PATH_DELIMITER + this.node.getId();

        //if node that received package is destination answer with rrep packet
        if (this.packet.isDestination(this.node)) {
            //reverse route to get route back to sender
            String reversedRoute = Utils.reverseRoute(newPath);

            //reverse source and destination and send discovered route to sender
            Packet p = PacketFactory.newRREPPacket(this.packet.id, this.packet.dest, this.packet.source, this.node.getId(), reversedRoute);

            this.node.addRoutingTableEntryFromPacket(p);

            this.node.send(p);
        } //only forward packet if current node has not forwarded in the past
        else if (!this.packet.route.contains(this.node.getId()) && !this.node.getRREQL().contains(this.packet.id)) {
            //just forward the packet as it as and put packet in orb to wait for ack
            Packet p = PacketFactory.newRREQPacket(this.packet.id, this.packet.source, this.packet.dest, this.node.getId(), newPath);
            this.node.getORB().put(p);
            this.node.getRREQL().add(this.packet.id);
            
            this.node.send(p);
        }
    }

    public void rerr() {

    }

    public void data() {
        try {
            String nextNodeId = this.packet.getNextNodeId();

            if (this.packet.isDestination(this.node) && this.node.getId().equals(nextNodeId)) {
                System.out.println("\u001B[31m" + this.node.getId() + " ich habe erhalten von " + this.packet.sender + ":" + this.packet.id + "\u001B[0m");
            } else {
                if (this.node.getId().equals(nextNodeId)) {
                    System.out.println(this.node.getId() + " ich leite weiter");

                    Packet p = PacketFactory.newDataPacket(this.packet.id, this.packet.source, this.packet.dest, this.node.getId(), this.packet.route);

                    this.node.send(p);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}