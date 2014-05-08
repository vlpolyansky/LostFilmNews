/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lostfilmnews;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javabots.HTMLTag;
import javabots.ParseHTML;
import javax.imageio.ImageIO;

/**
 *
 * @author Polyansky Vladislav
 */
public class NewsGetter {

    public List<New> news = new ArrayList<New>();

    public MainFrame frame = null;

    /**
     * Advance to the specified HTML tag.
     * @param parse The HTML parse object to use.
     * @param tag The HTML tag.
     * @param count How many tags like this to find.
     * @return True if found, false otherwise.
     * @throws IOException If an exception occurs while reading.
     */
    public static boolean advance(ParseHTML parse, HTMLTag tag, int count)
            throws IOException {
        int ch;
        ByteArrayOutputStream temp = new ByteArrayOutputStream();
        while ((ch = parse.read()) != -1000) {
            temp.write(ch);
            if (ch == 1000) {
                HTMLTag t = parse.getTag();
                String n = t.getName();
                if (includes(t, tag)) {
//                if (n.equals("div") && t.getAttributeValue("id") != null
//                        && t.getAttributeValue("id").equals(tag.getAttributeValue("id"))) {
                    count--;
                    if (count < 0) {
                        return true;
                    }
                }

            }
        }
        return false;
    }


    public static boolean advance(ParseHTML parse, String tag, int count)
            throws IOException {
        int ch;
        ByteArrayOutputStream temp = new ByteArrayOutputStream();
        while ((ch = parse.read()) != -1000) {
            temp.write(ch);
            if (ch == 1000) {
                HTMLTag t = parse.getTag();
                String n = t.getName();
                if (t.getName().equals(tag)) {
                    count--;
                    if (count < 0) {
                        return true;
                    }
                }

            }
        }
        return false;
    }


    private String untilTag(ParseHTML parse, HTMLTag tag, int count)
            throws IOException {
        int ch;
        ByteArrayOutputStream res = new ByteArrayOutputStream();
        while ((ch = parse.read()) != -1000) {
            if (ch == 1000) {
                HTMLTag t = parse.getTag();
                String n = t.getName();
                if (includes(t, tag)) {
                    count--;
                    if (count <= 0) {
                        return new String(res.toByteArray(), "windows-1251");
                    }
                }

            } else {
                res.write(ch);
            }
        }
        return new String(res.toByteArray(), "windows-1251");
    }


    public static BufferedImage downloadImage(URL url) throws IOException {
        InputStream in = url.openStream();
        BufferedImage buf = ImageIO.read(in);
//        FileOutputStream temp = new FileOutputStream("temp");
//        byte[] b = new byte[100000];
//        int size;
//        while ((size = in.read(b)) != -1) {
//            temp.write(b, 0, size);
//        }
//        return new MyImage(frame.c, "temp");
        return buf;
    }


    public static String trim(String s) {
        int l, r;
        for (l = 0; l < s.length(); l++) {
            char ch = s.charAt(l);
            if (ch <= '9' && ch >= '0'
                    || ch >= 'A' && ch <= 'Z'
                    || ch >= 'a' && ch <= 'z'
                    || ch >= 'А' && ch <= 'Я'
                    || ch >= 'а' && ch <= 'я'
                    || ch == ')') {
                break;
            }
        }
        for (r = s.length() - 1; r >= 0; r--) {
            char ch = s.charAt(r);
            if (ch <= '9' && ch >= '0'
                    || ch >= 'A' && ch <= 'Z'
                    || ch >= 'a' && ch <= 'z'
                    || ch >= 'А' && ch <= 'Я'
                    || ch >= 'а' && ch <= 'я'
                    || ch == ')') {
                break;
            }
        }
        if (l <= r) {
            return s.substring(l, r + 1);
        } else {
            return "";
        }
    }


    public static boolean includes(HTMLTag tag, HTMLTag subtag) {
        String na = subtag.getName();
        String nb = tag.getName();
        if (!na.equalsIgnoreCase(nb)) {
            return false;
        }
        Map<String, String> aat = subtag.getAttributes();
        for (String key : aat.keySet()) {
            if (tag.getAttributeValue(key) == null
                    || !subtag.getAttributeValue(key).equals(tag.getAttributeValue(key))) {
                return false;
            }
        }
        return true;
    }


    public NewsGetter(MainFrame frame) {
        this.frame = frame;
    }


    public void wr(InputStream in) throws IOException {
        byte[] b = new byte[100000];
        int size = in.read(b);
        if (size >= 0) {
            String s = new String(b, 0, size, "windows-1251");
            System.out.println(s);
        } else {
            System.out.println(-1);
        }
    }


    public New refresh(String type) throws MalformedURLException, IOException {
        New last = null;
        if (news.size() > 0) {
            last = news.get(0);
        }
        New now = null;
        String stringurl = "http://lostfilm.tv/";
        URL url = new URL(stringurl);
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        InputStream in = http.getInputStream();
        ParseHTML parse = new ParseHTML(in);
        HTMLTag start = new HTMLTag();
        start.setName("div");
        start.setAttribute("class", "bb");
        start.setAttribute("id", "new_" + type + "_list");
        advance(parse, start, 0);

        HTMLTag epTag = new HTMLTag();
        epTag.setName("div");
        epTag.setAttribute("style", "float:right;font-family:arial;font-size:18px;color:#000000");
        HTMLTag nameTag = new HTMLTag();
        nameTag.setName("span");
        nameTag.setAttribute("style", "font-family:arial;font-size:14px;color:#000000");
        HTMLTag subnameTag = new HTMLTag();
        subnameTag.setName("span");
        subnameTag.setAttribute("class", "torrent-title");
        HTMLTag endDiv = new HTMLTag();
        endDiv.setName("/div");
        HTMLTag endSpan = new HTMLTag();
        endSpan.setName("/span");
        HTMLTag startB = new HTMLTag();
        startB.setName("b");
        HTMLTag endB = new HTMLTag();
        endB.setName("/b");

        int ch;
        news.clear();
        New n = new New();
        ByteArrayOutputStream word = new ByteArrayOutputStream();
        // <editor-fold defaultstate="collapsed" desc="old">
//        while ((ch = parse.read()) != -1000) {
//            if (ch == 1000) {
//                HTMLTag t = parse.getTag();
//                if (includes(t, endTag)) {
//                    break;
//                } else if (includes(t, epTag)) {
//                    n.set(state, word.toString().trim());
//                    if (n.got) {
//                        news.add(n);
//                        n = new New();
//                    }
//                    word.reset();
//                    state = State.EPISODE;
//                } else if (includes(t, nameTag)) {
//                    n.set(state, word.toString().trim());
//                    if (n.got) {
//                        news.add(n);
//                        n = new New();
//                    }
//                    word.reset();
//                    state = State.NAME;
//                } else if (includes(t, subnameTag)) {
//                    n.set(state, word.toString().trim());
//                    if (n.got) {
//                        news.add(n);
//                        n = new New();
//                    }
//                    word.reset();
//                    state = State.SUBNAME;
//                } else if (t.getName().contains("/")) {
//                    n.set(state, word.toString().trim());
//                    state = State.NONE;
//                    if (n.got) {
//                        news.add(n);
//                        n = new New();
//                    }
//                    word.reset();
//                } else if (t.getName().equalsIgnoreCase("/b") && state == State.EPISODE) {
//
//                }
//            } else {
//                if (state != State.NONE) {
//                    word.write(ch);
////                    System.out.write(ch);
//                }
//            }
//        }// </editor-fold>
        for (int i = 0; i < 5; i++) {
            n = new New();
            advance(parse, epTag, 0);
            n.episode = trim(untilTag(parse, endDiv, 0));
            advance(parse, nameTag, 0);
            n.name = trim(untilTag(parse, endSpan, 0));
            while ((ch = parse.read()) != -1000) {
                if (ch == 1000) {
                    HTMLTag p = parse.getTag();
                    if (p.getName().equals("img")) {
                        n.image = downloadImage(new URL("http://lostfilm.tv" + p.getAttributeValue("src")));
                        break;
                    }
                }
            }
            advance(parse, startB, 0);
            n.subname = trim(untilTag(parse, endB, 0));
            boolean d = true, t = true;
            while (d || t) {
                advance(parse, "a", 0);
                HTMLTag linktag = parse.getTag();
                String link = "http://lostfilm.tv"
                        + linktag.getAttributeValue("href");
                if (link.contains("details")) {
                    n.details_link = link;
                    d = false;
                }
                if (link.contains("download")) {
                    n.torrent_link = link;
                    n.torrent_fname = link.substring(link.lastIndexOf("/") + 1);
                    String tempstr = linktag.getAttributeValue("onmouseover");
                    n.dlt_cookie = parseSetCookie(tempstr);
                    t = false;
                }
            }
            if (i == 0) {//!!!
                now = n;
            }
            news.add(n);
        }
        if (last != null && now != null && last.toString().equalsIgnoreCase(now.toString())) {
            return null;
        } else {
            return now;
        }

    }
    
    
    public static String parseSetCookie(String line) {
        int a = line.indexOf("\'");
        a = line.indexOf("\'", a + 1);
        a = line.indexOf("\'", a + 1);
        int b = line.indexOf("\'", a + 1);
        return line.substring(a + 1, b);
    }


    public enum State {

        NAME,
        SUBNAME,
        EPISODE,
        NONE

    }

    public State state = State.NONE;

    public class New {

        public String name, subname, episode, 
                pic_link, details_link, torrent_link,
                dlt_cookie,
                torrent_fname;

        public BufferedImage image = null;

        @Override
        public String toString() {
            return name + ": " + subname + " [" + episode + "]";
        }


    }

}
