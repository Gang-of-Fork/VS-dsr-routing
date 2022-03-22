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
    
    public static Packet newRREPPacket(String id, String source, String dest, String sender, String route) {
        return new Packet(Packet.PacketTypes.RREP, id, source, dest, sender, route);
    }
    
    public static Packet newRERRPacket(String id, String source, String dest, String sender, String route) {
        return new Packet(Packet.PacketTypes.RERR, id, source, dest, sender, route);
    }
    
    public static Packet newDataPacket(String id, String source, String dest, String sender, String route) {
        return new Packet(Packet.PacketTypes.DATA, id, source, dest, sender, route);
    }
}
