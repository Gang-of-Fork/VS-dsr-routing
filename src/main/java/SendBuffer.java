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
 * SendBuffer buffers destinations to send DataPackets that wait for routeDiscovery to finish before being sent
 */
public class SendBuffer extends Thread {

    class SendBufferEntry {
        public Packet dataP;
        public long ttl;

        public SendBufferEntry(Packet dataP, long ttl) {
            this.dataP = dataP;
            this.ttl = ttl;
        }
    }

    private ArrayList<SendBufferEntry> entries;
    private Node node;

    public SendBuffer(Node n) {
        this.entries = new ArrayList();
        this.node = n;
    }

    public synchronized void put(Packet dataP) {
        try {
            long expiry = System.currentTimeMillis() + Config.OUTBOUND_REQ_TIMEOUT;

            SendBufferEntry sbe = new SendBufferEntry(dataP, expiry);

            this.entries.add(sbe);
        } finally {
            notifyAll();
        }
    }

    public synchronized void timeout() {
        try {
            for (int i = 0; i < this.entries.size(); i++) {
                SendBufferEntry sbe = this.entries.get(i);

                long now = System.currentTimeMillis();

                if (now > sbe.ttl) {
                    System.out.println("Node " + this.node.getId() + ": " + sbe.dataP.id + " expired");
                    this.entries.remove(i);
                }
            }
        } finally {
            notifyAll();
        }
    }

    public synchronized void notifyRouteDiscovered(RoutingTable.RoutingTableEntry rte) {
        try {
            //System.out.println("Es wurde eine neue Route gefunden zu " + rte.dest + " über " + rte.route);

            //check if sendBuffer contains any entries for the newly discovered route
            for (int i = 0; i < this.entries.size(); i++) {
                String dest = this.entries.get(i).dataP.dest;

                if (dest.equals(rte.dest)) {
                    Packet dataP = this.entries.get(i).dataP;
                    dataP.route = rte.route;
                    this.entries.remove(i);

                    System.out.println("Node "+ this.node.getId() + ": Sending data to " + dataP.dest + " Via " + dataP.route);

                    this.node.getODB().put(dataP);
                    this.node.getNOB().addLast(dataP);
                }
            }
        } finally {
            notifyAll();
        }
    }

    @Override
    public void run() {
        try {
            while (true) { //check every 100ms if entry is expired
                Thread.sleep(Config.TIMEOUT_BUFFER_FREQUENCY);
                timeout();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
