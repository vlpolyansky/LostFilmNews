/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javabots;

import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class CookieUtility {
    /*
     * Map that holds all of the cookie values.
     */

    private Map<String, String> map = new HashMap<String, String>();

    /**
     * Allows access to the name/value pair list of cookies.
     *
     * @return the map
     */
    public Map<String, String> getMap() {
        return this.map;
    }


    /**
     * Load any cookies from the specified URLConnection
     * object. Cookies will be located by their Set-Cookie
     * headers. Any cookies that are found can be moved to a
     * new URLConnection class by calling saveCookies.
     *
     * @param http
     * The URLConnection object to load the cookies
     * from.
     */
    public void loadCookies(URLConnection http) {
        String str;
        int n = 1;
        do {
            str = http.getHeaderFieldKey(n);
            if ((str != null) && str.equalsIgnoreCase("Set-Cookie")) {
                str = http.getHeaderField(n);
//                StringTokenizer tok = new StringTokenizer(str, "=");
//                String name = tok.nextToken();
//                String value = tok.nextToken();
//                this.map.put(name, value);
                StringTokenizer tok = new StringTokenizer(str, ";");
                while (tok.hasMoreTokens()) {
                    String cookie = tok.nextToken();
                    StringTokenizer tok2 = new StringTokenizer(cookie, "=");
                    String name = tok2.nextToken();
                    String value = tok2.nextToken();
                    this.map.put(name, value);
                }
            }
            n++;
        } while (str != null);
    }


    /**
     * Once you have loaded cookies with loadCookies, you can
     * call saveCookies to copy these cookies to a new HTTP
     * request. This allows you to easily support cookies.
     *
     * @param http
     * The URLConnection object to add cookies to.
     */
    public void saveCookies(URLConnection http) {
        StringBuilder str = new StringBuilder();
        Set<String> set = this.map.keySet();
        for (String key : set) {
            String value = this.map.get(key);
            if (str.length() > 0) {
                str.append("; ");
            }
            str.append(key + "=" + value);
        }
        http.setRequestProperty("Cookie", str.toString());
    }


}
