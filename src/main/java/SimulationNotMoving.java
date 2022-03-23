
import java.util.Random;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Fwy
 */
public class SimulationNotMoving {

    //DO NOT EDIT CUSTOMIZE VIA Config.java
    public static void main(String[] args) {



        Field f = new Field(Config.X_SIZE, Config.Y_SIZE, Config.NUM_OF_NODES);
        Config.field = f;
        try {
            Node n1;
            Node n2;

            n1 = f.nodes.get(2);
            n2 = f.nodes.get(4);
            n1.sendHello(n2);
            Thread.sleep(2000);

            n1 = f.nodes.get(1);
            n2 = f.nodes.get(2);
            n1.sendHello(n2);
            Thread.sleep(2000);

            n2 = f.nodes.get(4);
            n2 = f.nodes.get(3);
            n1.sendHello(n2);


        } catch(Exception e) {
            e.printStackTrace();
        }

        //generate test nodes
        /*
        Node n1 = new Node(0, 0);
        Node n2 = new Node(0, 10);
        Node n3 = new Node(0, 20);

        try {
            f.addNode(n1);
            f.addNode(n2);
            f.addNode(n3);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try {
            n1.sendHello(n3);
            Thread.sleep(2000);
            n1.sendHello(n3);
            Thread.sleep(2000);
            n1.sendHello(n3);
            n1.sendHello(n2);
            Thread.sleep(2000);
            n2.sendHello(n3);
            Thread.sleep(2000);
            n3.sendHello(n1);
        } catch(Exception e) {
            e.printStackTrace();
        }
         */
    }
}
