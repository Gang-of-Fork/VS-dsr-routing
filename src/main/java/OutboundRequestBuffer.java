/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.ArrayList;

/**
 *
 * @author Fwy
 * OutboundRequestBuffer buffers sent RREQ Packets waiting for RRESs
 */
public class OutboundRequestBuffer extends Thread {

    class OutboundRequestBufferEntry {
        public Packet packet;
        public long ttl;

        public OutboundRequestBufferEntry(Packet p, long ttl) {
            this.packet = p;
            this.ttl = ttl;
        }
    }

    private ArrayList<OutboundRequestBufferEntry> entries;

    public OutboundRequestBuffer() {
        this.entries = new ArrayList();
    }

    public synchronized void put(Packet p) {
        try {
            long expiry = System.currentTimeMillis() + Config.OUTBOUND_REQ_TIMEOUT;

            OutboundRequestBufferEntry orbe = new OutboundRequestBufferEntry(p, expiry);

            this.entries.add(orbe);
        } finally {
            notifyAll();
        }
    }

    public synchronized OutboundRequestBufferEntry pop(String reqId) throws Exceptions.NoMatchingBufferEntryException {
        try {
            for (int i = 0; i < this.entries.size(); i++) {
                if (this.entries.get(i).packet.id.equals(reqId)) {
                    OutboundRequestBufferEntry orbe = this.entries.get(i);
                    this.entries.remove(i);
                    return orbe;
                }
            }

            throw new Exceptions.NoMatchingBufferEntryException("The Entry is not in the Buffer");
        } finally {
            notifyAll();
        }
    }

    public synchronized void timeout() {
        try {
            for (int i = 0; i < this.entries.size(); i++) {
                OutboundRequestBufferEntry orbe = this.entries.get(i);

                long now = System.currentTimeMillis();

                if (now > orbe.ttl) {
                    //System.out.println(orbe.packet.id + " expired");
                    this.entries.remove(i);
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
                Thread.sleep(300);
                timeout();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
