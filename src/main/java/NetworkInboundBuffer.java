import java.util.ArrayList;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

public class NetworkInboundBuffer extends Thread {
    private LinkedList<Packet> buffer;
    private Node node;

    public NetworkInboundBuffer(Node node) {
        this.node = node;
        this.buffer = new LinkedList<Packet>();
    }

    public void run() {
        while (true) {
            try {
                Thread.sleep(Config.NETWORK_BUFFER_FREQUENCY);
                //throw NoSuchElementException when buffer is empty
                Packet p = this.pop();
                RequestProcessor.process(this.node, p);

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (NoSuchElementException e) {
            }
        }
    }

    public synchronized void push(Packet p) {
        try {
            this.buffer.push(p);
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
