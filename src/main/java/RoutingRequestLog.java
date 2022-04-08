/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.ArrayList;

/**
 *
 * @author Fwy
 */
public class RoutingRequestLog {
    ArrayList<String> entries;
    
    public RoutingRequestLog() {
        this.entries = new ArrayList();
    }
    
    public synchronized void add(String entry) {
        this.entries.add(entry);
    }
    
    public synchronized Boolean contains(String entry) {
        return this.entries.contains(entry);
    }
}
