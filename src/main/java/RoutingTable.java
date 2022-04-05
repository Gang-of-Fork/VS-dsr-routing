/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Fwy
 */
public class RoutingTable {
    public static class RoutingTableEntry {

        public String dest;
        public String route;

        public RoutingTableEntry(String dest, String route) {
            this.dest = dest;
            this.route = route;
        }
        
        public int numOfHops() {
            return route.split(Config.PATH_DELIMITER).length;
        }
        
        @Override
        public String toString() {
            return "TO: " + this.dest + " VIA: " + this.route;
        }
    }
    
    private HashMap<String, RoutingTableEntry> entries;

    public RoutingTable() {
        this.entries = new HashMap();
    }

    //returns an array in JSON Format
    public synchronized String toJSON() {
        Iterator<RoutingTableEntry> rteIterator = entries.values().iterator();
        String rt_json = "[";
        while(rteIterator.hasNext()) {
            RoutingTableEntry rte = rteIterator.next();
            rt_json+="{\n";
            rt_json+=("\"destination\":\"" + rte.dest + "\",\n");
            rt_json+=("\"route\":\"" + rte.route + "\"\n");
            rt_json+="}";
            if(rteIterator.hasNext()) {
                rt_json+=",\n";
            }
        }
        rt_json += "]";
        return rt_json;
    }

    public synchronized RoutingTableEntry get(String dest) throws Exceptions.RoutingEntryNotFoundException {
        try {
            if(this.entries.containsKey(dest)) {
                RoutingTableEntry rte = this.entries.get(dest);
                
                return rte;
            } 
            
            throw new Exceptions.RoutingEntryNotFoundException("No Route Found");
        } finally {
            notifyAll();
        }
    }

    public synchronized void put(RoutingTableEntry rte) {
        try {
            this.entries.put(rte.dest, rte);
        } finally {
            notifyAll();
        }
    }

    public synchronized String[] keys() {
        return this.entries.keySet().toArray(new String[0]);
    }
    
    public boolean exists(String dest) {
        return this.entries.containsKey(dest);
    }

    public synchronized void remove(String dest) {
        try {
            this.entries.remove(dest);
        } finally {
            notifyAll();
        }
    }
}
