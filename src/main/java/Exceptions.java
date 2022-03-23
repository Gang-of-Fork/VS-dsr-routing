/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Fwy
 */
public class Exceptions {
    static class RoutingEntryNotFoundException extends Exception {
        public RoutingEntryNotFoundException(String msg) {
            super(msg);
        }
    }
    
    static class SerealizationException extends Exception {
        public SerealizationException(String msg) {
            super("Error during Serialization:" + msg);
        }
    }
    
    static class NodeCollisionException extends Exception {
        public NodeCollisionException(String msg) {
            super(msg);
        }
    }
    
    static class IncompatiblePacketTypeException extends Exception {
        public IncompatiblePacketTypeException(String msg) {
            super(msg);
        }
    }
    
    static class NoMatchingBufferEntryException extends Exception {
        public NoMatchingBufferEntryException(String msg) {
            super(msg);
        }
    }

    static class NoBrokenLinksException extends Exception {
        public NoBrokenLinksException(String msg) {super(msg);}
    }
}
