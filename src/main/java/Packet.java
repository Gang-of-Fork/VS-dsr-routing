/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Fwy
 */

public class Packet {

    static enum PacketTypes {
        RREP,
        RREQ,
        RERR,
        DATA,
        ACK
    }

    public PacketTypes type;
    public String id;
    public String dest;
    public String route;
    public String source;
    public String sender;

    public Packet(PacketTypes type, String id, String source, String dest, String sender, String route) {
        this.type = type;
        this.dest = dest;
        this.route = route;
        this.source = source;
        this.sender = sender;
        this.id = id;
    }

    public ArrayList<RoutingTable.RoutingTableEntry> extractRoutingTableEntries(String self) {
        ArrayList<RoutingTable.RoutingTableEntry> entries = new ArrayList();
        
        String[] splt_route = this.route.split(Config.PATH_DELIMITER);
        
        int indexOfself = Arrays.asList(splt_route).indexOf(self);

        //construct all forward routes
        String fwd_route = self;
        
        for(int i=(indexOfself + 1); i<splt_route.length; i++) {
            fwd_route += Config.PATH_DELIMITER + splt_route[i];
            
            String to = splt_route[i];
            
            RoutingTable.RoutingTableEntry rte = new RoutingTable.RoutingTableEntry(to, fwd_route);
            entries.add(rte);
        }
        
        String backwd_route = self;
        
        //construct all backwards routes
        for(int i=(indexOfself - 1); i>=0; i--) {
            backwd_route += Config.PATH_DELIMITER + splt_route[i];
            
            String to = splt_route[i];
            
            RoutingTable.RoutingTableEntry rte = new RoutingTable.RoutingTableEntry(to, backwd_route);
            entries.add(rte);
        }
        
        return entries;
    }

    public String getNextNodeId() throws Exceptions.IncompatiblePacketTypeException{
        String[] splt_path = route.split(Config.PATH_DELIMITER);

        for (int i = 0; i < splt_path.length; i++) {
            if (splt_path[i].equals(this.sender)) {
                return splt_path[i + 1];
            }
        }
        
        throw new Exceptions.IncompatiblePacketTypeException("Da ging was schief");
        
    }

    public String getBrokenLink() throws Exceptions.NoBrokenLinksException{

        String[] splt_path = route.split(Config.PATH_DELIMITER);

        for (int i = 0; i < splt_path.length; i++) {
            if (splt_path[i].equals(this.source)) {
                if (i == 0) {
                    throw new Exceptions.NoBrokenLinksException("RERR must not originate from first station in the route");
                }
                return splt_path[i] + Config.PATH_DELIMITER + splt_path[i-1];
            }
        }
        throw new Exceptions.NoBrokenLinksException("Source Station is not present in Route");
    }

    public boolean isDestination(Node n) {
        return n.getId().equals(dest);
    }
}