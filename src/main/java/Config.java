/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Assumptions made:
 * - Routes are bidirectional
 * - Only one node per coordinate
 * - Only ints as coordinates
 * 
 * Improvements:
 * - OBR for every node
 * - lifetime for rt entries
 */
public class Config {
    public static final int X_SIZE = 25; //size of x axis of field
    public static final int Y_SIZE = 25; //size o y axis of field
    public static final int NUM_OF_NODES = 10; //number of nodes
    public static final int NODE_REACH = 10; // reach of nodes
    
    public static final long OUTBOUND_REQ_TIMEOUT = 20000;
    public static final long OUTBOUND_DATA_TIMEOUT = 10000;
    public static final String PATH_DELIMITER = ";";
    
    //DO NOT EDIT - THIS VARIABLES ARE USED INTERNALLY
    public static Field field;
}
