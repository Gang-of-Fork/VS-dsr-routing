import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Fwy
 */
public class VisualizationLogger {
    private static String configContent = "";
    private static String nodes = "";
    
    public static void gatherConfig() {
        configContent += "{";
        configContent += "\"xsize\": " + Config.X_SIZE + ",";
        configContent += "\"ysize\": " + Config.Y_SIZE + ",";
        configContent += "\"numOfNodes\": " + Config.NUM_OF_NODES;
        configContent += "}";
    }
    
    public static void setField(Field f) {
        nodes += "[ ";
        for(int i=0; i<f.nodes.size(); i++) {
            Node n = f.nodes.get(i);   
            nodes += "{";
            nodes += "\"x\": " + n.getX() + ",";
            nodes += "\"y\": " + n.getY() + ",";
            nodes += "\"id\": " + n.getId();
            nodes += "}";
            
            if(i != (f.nodes.size() - 1)) {
                nodes += ",";
            }
        }
        nodes += " ]";
    }
    
    
    public static void outputToFile(String path) {
        //init
        gatherConfig();
        
        try {
            File file = new File(System.getProperty("user.dir") + File.separator + path);
            file.mkdirs();
            file.createNewFile();
            
            System.out.println(file.getPath());
            
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write("{\n");
            writer.write("\"config\": " + configContent);
            writer.write(",");
            writer.write("\n");
            writer.write("\"nodes\": " + nodes);
            writer.write("\n}");
            
            writer.close();
        } catch(Exception e) {
            
        }
    }
}
