import java.util.ArrayList;
import java.util.UUID;

/**
 *
 * @author RR
 * OutboundDataBuffer buffers sent DataPackets waiting for ACKs
 */
public class OutboundDataBuffer extends Thread {

    class OutboundDataBufferEntry {
        public Packet packet;
        public long ttl;

        public OutboundDataBufferEntry(Packet p, long ttl) {
            this.packet = p;
            this.ttl = ttl;
        }
    }

    private ArrayList<OutboundDataBufferEntry> entries;
    private Node node;

    public OutboundDataBuffer(Node node) {
        this.node = node;
        this.entries = new ArrayList();
    }

    public synchronized void put(Packet p) {
        try {
            long expiry = System.currentTimeMillis() + Config.OUTBOUND_DATA_TIMEOUT;

            OutboundDataBufferEntry odbe = new OutboundDataBufferEntry(p, expiry);
            //System.out.println("Add Data Packet " + p.id +  " to ODB");
            this.entries.add(odbe);
        } finally {
            notifyAll();
        }
    }

    public synchronized OutboundDataBufferEntry pop(String reqId) throws Exceptions.NoMatchingBufferEntryException {
        try {
            for (int i = 0; i < this.entries.size(); i++) {
                if (this.entries.get(i).packet.id.equals(reqId)) {
                    OutboundDataBufferEntry odbe = this.entries.get(i);
                    this.entries.remove(i);
                    return odbe;
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
                OutboundDataBufferEntry odbe = this.entries.get(i);

                long now = System.currentTimeMillis();

                if (now > odbe.ttl) {
                    System.out.println(odbe.packet.id + " expired, no ACK received, sending RERR");

                    //remove all broken routes from RT
                    this.node.removeBrokenTableEntries(this.node.getId() + Config.PATH_DELIMITER + odbe.packet.getNextNodeId());

                    //only send RERR packet, if the original packet does not originate from current node, otherwise
                    //it would result in a loopback message
                    if(!odbe.packet.source.equals(this.node.getId())) {
                        //send RERR Packet
                        Packet rerrP = PacketFactory.newRERRPacket(UUID.randomUUID().toString(), this.node.getId(), odbe.packet.source, this.node.getId(), Utils.reverseRoute(odbe.packet.route));
                        this.node.getNOB().addLast(rerrP);

                    }
                    this.entries.remove(i);
                }
            }
        } catch (Exceptions.IncompatiblePacketTypeException e) {
            e.printStackTrace();
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