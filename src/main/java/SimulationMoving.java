/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author RR
 */

public class SimulationMoving {
    //DO NOT EDIT CUSTOMIZE VIA Config.java
    public static void main(String[] args) {
        Field f = new Field(Config.X_SIZE, Config.Y_SIZE, Config.NUM_OF_NODES);
        Config.field = f;
        try {
            Node n1;
            Node n2;

            n1 = f.nodes.get(0);
            n2 = f.nodes.get(1);
            n1.sendHello(n2);
    /*
            Thread.sleep(5000);
            f.nodes.get(2).move();
            n1.sendHello(n2);

            Thread.sleep(5000);
            f.nodes.get(3).move();
            n1.sendHello(n2);

            Thread.sleep(5000);
            f.nodes.get(4).move();
            n1.sendHello(n2);

            Thread.sleep(5000);
            f.nodes.get(0).move();
            n1.sendHello(n2);

     */


            Thread.sleep(5000);
            f.nodes.get(1).move();
            n1.sendHello(n2);

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    
}
