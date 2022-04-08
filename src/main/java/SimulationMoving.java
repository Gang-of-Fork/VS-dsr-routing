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
            VisualizationLogger.saveSnapshot("INIT");

            Node n1;
            Node n2;

            n1 = f.getNodes().get(0);
            n2 = f.getNodes().get(1);
            n1.sendHello(n2);


            Thread.sleep(30000);
            f.getNodes().get(1).move();
            n1.sendHello(n2);

            System.out.println("Waiting " + Config.LOGGER_WAIT_AFTER_LAST_PACKET + "ms before finalizing log...");
            Thread.sleep(Config.LOGGER_WAIT_AFTER_LAST_PACKET);

            VisualizationLogger.outputToFile("moving_sims");

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    
}
