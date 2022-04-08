import javax.swing.plaf.nimbus.State;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.UUID;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author Fwy
 */
public class VisualizationLogger {
    private static String configContent = "";
    private static String nodes = "";
    private static String packet = "";
    private static String actions = "";
    private static String table_update = "";
    private static ArrayList<String> states = new ArrayList<String>();




    public static void gatherConfig() {
        String temp_configContent = "";
        temp_configContent += "\"xsize\": " + Config.X_SIZE + ",";
        temp_configContent += "\"ysize\": " + Config.Y_SIZE;
        configContent = temp_configContent;
    }

    public static void gatherNodes() {
        nodes = "\"nodes\":";
        nodes += "[ ";
        for (int i = 0; i < Config.field.getNodes().size(); i++) {
            Node n = Config.field.getNodes().get(i);
            nodes += "{";
            nodes += "\"x\": " + n.getX() + ",";
            nodes += "\"y\": " + n.getY() + ",";
            nodes += "\"id\": " + n.getId() + ",";
            nodes+= "\"routingTable\":";
            nodes+= n.getRT().toJSON();
            nodes+= ",\n";
            nodes+= "\"inRange\":";
            nodes+= "[";
            ArrayList<Node> neighbours = Config.field.discover(n);
            for(int j = 0; j < neighbours.size(); j++) {
                nodes+= "\"" + neighbours.get(j).getId() + "\"";
                if(!(j==neighbours.size()-1)) {
                    nodes+= ",";
                }

            }
            nodes+= "]";

            nodes += "}";

            if (i != (Config.field.getNodes().size() - 1)) {
                nodes += ",\n";
            }
        }
        nodes += " ]";
    }

    public synchronized static void saveSnapshot(String stateType) {
        gatherConfig();
        gatherNodes();
        String state = configContent;
        state += ",\n";
        state += "\"type\": \"" + stateType + "\"";
        state += ",\n";
        state += nodes;
        if (Arrays.stream(new String[]{"RREQ", "RRES", "DATA", "RERR", "ACK"}).anyMatch(s -> s.equals(stateType))) {
            state += ",\n";
            state += packet;

            state += ",\n";
            state += actions;
        }
        if (stateType.equals("RTU")) {
            state += ",\n";
            state += table_update;
        }
        states.add(state);
    }

    public synchronized static void setPacketAndActionsAndSaveSnapshot(Node sendingNode, Packet p) {
        String temp_packet = "\"packet\":";
        temp_packet += "{\n";
        temp_packet += ("\"id\": \"" + p.id + "\",\n");
        temp_packet += ("\"dest\": \"" + p.dest + "\",\n");
        temp_packet += ("\"sender\": \"" + p.sender + "\",\n");
        temp_packet += ("\"source\": \"" + p.source + "\",\n");
        temp_packet += ("\"route\": \"" + p.route + "\",\n");
        temp_packet += ("\"type\": \"" + p.type + "\"\n");
        temp_packet += "}\n";
        packet = temp_packet;

        String temp_actions = "\"actions\":";
        temp_actions += "{\n";
        temp_actions += ("\"sending\": \"" + sendingNode.getId() + "\",\n");

        //set receiving_processing and receiving_discarding props
        ArrayList receiving_processing_Ids = new ArrayList<String>();
        ArrayList<String> receiving_discarding_Ids = new ArrayList<String>();
        ArrayList<Node> neighbourNodes = Config.field.discover(sendingNode);
        //Only DATA and ACK are not overheard by other nodes
        if (!(p.type.equals(Packet.PacketTypes.RERR) || p.type.equals(Packet.PacketTypes.RREQ))) {
            //add the next Node, that should receive the packet, to the receiving_processing array, if it is in range
            //add all other nodes in range into the receiving_discarding array
            for (Node node : neighbourNodes) {
                try {
                    if (node.getId().equals(p.getNextNodeId())) {
                        receiving_processing_Ids.add(node.getId());
                    } else {
                        receiving_discarding_Ids.add(node.getId());
                    }
                } catch (Exceptions.IncompatiblePacketTypeException e) {
                    e.printStackTrace();
                }
            }// RREQs are only processed if the Node hasn't received this id yet
        } /*
        else if(p.type.equals(Packet.PacketTypes.RREQ)) {
            for (Node node : neighbourNodes) {
                if(node.getRREQL().contains(p.id)) {
                    receiving_discarding_Ids.add(node.getId());
                } else {
                    receiving_processing_Ids.add(node.getId());
                }
            }
        } */ else {
            for (Node node : neighbourNodes) {
                receiving_processing_Ids.add(node.getId());
            }
        }

        //add receiving_processing array
        temp_actions += "\"receiving_processing\":\n";
        temp_actions += "[";
        if (receiving_processing_Ids.size() > 0) {
            temp_actions += ("\"" + String.join("\",\"", receiving_processing_Ids) + "\"");
        }
        temp_actions += "],\n";

        temp_actions += "\"receiving_discarding\":\n";
        temp_actions += "[";
        if (receiving_discarding_Ids.size() > 0) {
            temp_actions += ("\"" + String.join("\",\"", receiving_discarding_Ids) + "\"");
        }
        temp_actions += "]\n";
        temp_actions += "}\n";

        actions = temp_actions;
        saveSnapshot(p.type+"");

    }

    public synchronized static void setTableUpdateAndSaveSnapshot(String type, String node, String destination, String route) {
        String temp_table_update = "\"table_update\":";
        temp_table_update += "{\n";
        temp_table_update += ("\"type\": \"" + type + "\",\n");
        temp_table_update += ("\"node\": \"" + node + "\",\n");
        temp_table_update += ("\"destination\": \"" + destination + "\",\n");
        temp_table_update += ("\"route\": \"" + route + "\"\n");
        temp_table_update += "}\n";
        table_update = temp_table_update;
        saveSnapshot("RTU");
    }


    public synchronized static void outputToFile(String path) {
        //init
        gatherConfig();

        try {
            File file = new File(System.getProperty("user.dir") + File.separator + path);
            file.mkdirs();
            file = new File(System.getProperty("user.dir") + File.separator + path + File.separator + UUID.randomUUID() + ".json");
            file.createNewFile();

            System.out.println("printing log to " + file.getPath());

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write("[");
            Iterator<String> i = states.iterator();
            while (i.hasNext()) {
                writer.write("{\n");
                writer.write(i.next());
                writer.write("}");
                if (i.hasNext()) writer.write(",\n");
            }
            writer.write("]");

            writer.close();
        } catch (Exception e) {

        }
    }
}
