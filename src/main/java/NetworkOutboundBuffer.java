import java.util.ArrayList;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

public class NetworkOutboundBuffer extends Thread {
    private LinkedList<Packet> buffer;
    private Node node;

    public NetworkOutboundBuffer(Node node) {
        this.node = node;
        this.buffer = new LinkedList<Packet>();
    }

    public void run() {
        while (true) {
            try {
                Thread.sleep(Config.NETWORK_BUFFER_FREQUENCY);
                Packet p = this.pop();

                //discover neighbours and send packet via broadcast
                ArrayList<Node> neighbours = Config.field.discover(this.node);

                //System.out.println(this.id + " discovered neighbours " + neighbours.size());
                for (int i = 0; i < neighbours.size(); i++) {
                    neighbours.get(i).getNIB().push(p);
                }

                VisualizationLogger.setPacketAndActionsAndSaveSnapshot(this.node, p);

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (NoSuchElementException e) {
            }
        }
    }

    public synchronized void addLast(Packet p) {
        try {
            this.buffer.addLast(p);
        } finally {
            notifyAll();
        }
    }

    private synchronized Packet pop() throws NoSuchElementException {
        Packet p = this.buffer.pop();
        notifyAll();
        return p;


    }
}
