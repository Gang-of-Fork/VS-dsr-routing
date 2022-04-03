/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Fwy
 */
public class PacketFactory {
    public static Packet newRREQPacket(String id, String source, String dest, String sender, String route) {
        return new Packet(Packet.PacketTypes.RREQ, id, source, dest, sender, route);
    }
    
    public static Packet newRRESPacket(String id, String source, String dest, String sender, String route) {
        return new Packet(Packet.PacketTypes.RRES, id, source, dest, sender, route);
    }
    
    public static Packet newRERRPacket(String id, String source, String dest, String sender, String route) {
        return new Packet(Packet.PacketTypes.RERR, id, source, dest, sender, route);
    }
    
    public static Packet newDataPacket(String id, String source, String dest, String sender, String route) {
        return new Packet(Packet.PacketTypes.DATA, id, source, dest, sender, route);
    }

    public static Packet newAckPacket(String id, String source, String dest, String sender, String route) {
        return new Packet(Packet.PacketTypes.ACK, id, source, dest, sender, route);
    }
}
