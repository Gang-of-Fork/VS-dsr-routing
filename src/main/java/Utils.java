
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Fwy
 */
public class Utils {
    public static int nextNodeId = -1;
    
    public static int getNextNodeId() {
        nextNodeId += 1;
        return nextNodeId;
    }
    
    public static String reverseRoute(String route) {
        List<String> routeIds = Arrays.asList(route.split(Config.PATH_DELIMITER));
        Collections.reverse(routeIds);
        String[] string_splt_arr = Arrays.copyOf(routeIds.toArray(), routeIds.toArray().length, String[].class);
        String reversedRoute = String.join(Config.PATH_DELIMITER, string_splt_arr);
        
        return reversedRoute;
    }

    public static boolean routeContainsLink(String route, String link) {
        String[] route_split = route.split(Config.PATH_DELIMITER);
        String[] link_split = link.split(Config.PATH_DELIMITER);
        for(int i = 0; i < route_split.length-1; i++) {
            if((route_split[i].equals(link_split[0]) && route_split[i+1].equals(link_split[1])) ||(route_split[i].equals(link_split[1]) && route_split[i+1].equals(link_split[0])) ){
                return true;
            }
        }
        return false;
    }
}
