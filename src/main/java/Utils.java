
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
}
