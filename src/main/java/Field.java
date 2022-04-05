/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Fwy
 */
public class Field {

    public synchronized ArrayList<Node>  getNodes() {
        return nodes;
    }

    public synchronized void setNodes(ArrayList<Node> nodes) {
        this.nodes = nodes;
    }

    private ArrayList<Node> nodes;
    public int x_size;
    public int y_size;
    
    public Field(int x_size, int y_size, int numOfNodes) {
        this.nodes = new ArrayList();
        this.x_size = x_size;
        this.y_size = y_size;
        
        this.generateRandomNodes(numOfNodes);
        
        System.out.println(this.toString());
    }

    private void generateRandomNodes(int numOfNodes) {
        Random r = new Random();

        for (int i = 0; i < numOfNodes; i++) {

            int x = r.nextInt((this.x_size + 1) - 0) + 0;
            int y = r.nextInt((this.y_size + 1) - 0) + 0;
            
            Node n = new Node(x, y);
            try {
                this.addNode(n);

            } catch (Exceptions.NodeCollisionException e) {
                i--;
                Utils.decreaseNextNodeId();
            }
        }
    }

    public void addNode(Node n) throws Exceptions.NodeCollisionException {
        for (int i = 0; i < this.nodes.size(); i++) {
            Node currNode = nodes.get(i);
            
            if(currNode.getX() == n.getX() && currNode.getY() == n.getY()) {
                throw new Exceptions.NodeCollisionException("Node collision");
            }
        }
        
        this.nodes.add(n);
    }
    
    public ArrayList<Node> discover(Node n) {
        ArrayList<Node> discovered = new ArrayList();
        
        for (int i = 0; i < this.nodes.size(); i++) {
            Node currNode = nodes.get(i);
            
            if(currNode.getX() != n.getX() || currNode.getY() != n.getY()) { //ensure that node is not current node
                if(n.isInReach(currNode)) { //ensure that other node is in reach
                    discovered.add(currNode);
                }
            }
        }
        return discovered;
    }
    
    //getter and setter methods
    public int getNumOfNodes() {
        return this.nodes.size();
    }
    
    public Boolean hasNodeAt(int x, int y) {
        for(int i=0; i<this.nodes.size(); i++) {
            if(this.nodes.get(i).getY() == y && this.nodes.get(i).getX() == x) {
                return true;
            }
        }
        
        return false;
    }
    
    private Node nodeAt(int x, int y) {
        Node n = null;
        
        for(int i=0; i<this.nodes.size(); i++) {
            if(this.nodes.get(i).getY() == y && this.nodes.get(i).getX() == x) {
                n = this.nodes.get(i);
            }
        }
        
        return n;
    }
    
    @Override
    public String toString() {
        String[][] field = new String[Config.Y_SIZE][Config.X_SIZE];
        
        String strField = "";
        
        for(int i=0; i<field.length; i++) {
            for(int j=0; j<field[i].length; j++) {
                if(this.hasNodeAt(i, j)) {
                    strField += "|" + this.nodeAt(i, j).getId();
                } else {
                   strField += "|x"; 
                }
            }
            
            strField += "|\n";
            
            for(int j=0; j<field[i].length; j++) {
                strField += "-";
            }
            
            strField += "\n";
        }
        
        return strField;
    }
}
