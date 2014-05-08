/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MessageFrame.java
 *
 * Created on 05.12.2010, 12:37:19
 */
package lostfilmnews;

import com.sun.awt.AWTUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;

/**
 *
 * @author Polyansky Vladislav
 */
public class MessageFrame extends javax.swing.JFrame {

//    Thread t = new Thread(this);
    NewsGetter.New n = null;

    MainFrame frame = null;

    MessageFrame This = this;

    private boolean threesec = false,
            hiding = false;

    private JPanel basePanel = null;

    private JPanel panel = null;

    private boolean running = true;

    /** Creates new form MessageFrame */
    public MessageFrame(MainFrame frame, NewsGetter.New n, String... args) {
        this.frame = frame;
        this.n = n;
        if (n == null) {
            return;
        }
        for (String arg : args) {
            if (arg.equals("3sec")) {
                threesec = true;
            }
        }
        myInit();
        new Show();
    }


    public void sleep(int millis) {
        Date d = new Date();
        long c = d.getTime();
        while (c + millis > new Date().getTime()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                Logger.getLogger(MessageFrame.class.getName()).log(Level.SEVERE, null, ex);
                MainFrame.log(ex);
            }
        }
    }


    public void myInit() {
//        try {
//            throw new IndexOutOfBoundsException("bla bla bla");
//        } catch (Exception ex) {
//            MainFrame.log(ex);
//        }
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        setFocusable(false);
        setFocusableWindowState(false);
        setFont(new java.awt.Font("Arial", Font.BOLD, 12));
        setResizable(false);
        setUndecorated(true);
        addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }


        });
        setSize(262, 85);

        basePanel = new JPanel() {

            @Override
            public void paintComponent(Graphics g) {
                int R = 0;
                int G = 0;
                int B = 255;
                GradientPaint gradient = new GradientPaint(0.0f, 0.0f, new Color(R, G, B, 100),
                        getWidth(), getHeight(), new Color(R, G, B, 100), true);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }


        };

        add(basePanel, BorderLayout.CENTER);


        setBackground(new Color(0, 127, 255, 0));
//        setBackground(getAverageColor(n.image));

        AWTUtilities.setWindowOpacity(this, 0.85f);
        AWTUtilities.setWindowShape(this, makeShape(getWidth(), getHeight(), 15));
        setLocation(Toolkit.getDefaultToolkit().getScreenSize().width,
                Toolkit.getDefaultToolkit().getScreenSize().width);
        setVisible(true);

        new Thread(new Runnable() {

            public void run() {
                while (running) {
//                    if (This.dis) {
//
//                    }
                    if (This.getMousePosition() == null) {
                        AWTUtilities.setWindowOpacity(This, 0.6f);
                    } else {
                        AWTUtilities.setWindowOpacity(This, 0.9f);
                    }
                }
            }


        }).start();
    }


    private Shape makeShape(int width, int height, int r) {
        Polygon poly = new Polygon();
        for (double i = -Math.PI; i <= -Math.PI / 2; i += Math.PI / 16) {
            poly.addPoint((int) (r * (1 + Math.cos(i))), (int) (r * (1 + Math.sin(i))));
        }
        for (double i = -Math.PI / 2; i <= 0; i += Math.PI / 16) {
            poly.addPoint((int) (width - r + r * Math.cos(i)), (int) (r * (1 + Math.sin(i))));
        }
        poly.addPoint(width, height);
        poly.addPoint(0, height);

        return poly;
    }


    public void drawAll(String name1, String name2, String sub1, String sub2, String ep) {
//        if (true) return;
        Graphics2D g = (Graphics2D) getGraphics();
        BufferedImage buf = (BufferedImage) createImage(getWidth(), getHeight());
        Graphics2D g2 = buf.createGraphics();

        if (n != null) {
            int x = getHeight() + 20;
            int y = getHeight();
            g2.drawImage(n.image, 0, 0, x, y, null);

            for (int i = 0; i < 2; i++) {
                g2.drawString(name1, x + 5, 15);
                g2.drawString(name2, x + 5, 30);
                g2.drawString(sub1, x + 5, 45);
                g2.drawString(sub2, x + 5, 60);
            }

            g2.drawString(ep, x + 5, 75);
        }
        g.drawImage(buf, 0, 0, null);
    }


    public Color getAverageColor(BufferedImage img) {
        long r = 0, g = 0, b = 0, total = 0;
        for (int i = 0; i < img.getTileWidth(); i++) {
            for (int j = 0; j < img.getTileHeight(); j++) {
                total++;
                Color c = new Color(img.getRGB(i, j));
                r += c.getRed();
                g += c.getGreen();
                b += c.getBlue();

            }
        }

        return new Color((int) (r / total), (int) (g / total), (int) (b / total));
    }


    public String getFirstPart(String s, int x) {
        String last = s;
        while (last.lastIndexOf(" ") > -1) {
            String t = last.substring(0, last.lastIndexOf(" "));
            if (getGraphics().getFont().getStringBounds(t,
                    ((Graphics2D) getGraphics()).getFontRenderContext()).getWidth()
                    <= getWidth() - x - 5) {
                return t;
            } else {
                last = t;
            }
        }
        return last;
    }


    public String getSecondPart(String s, int x) {
        if (s.length() == getFirstPart(s, x).length()) {
            return "";
        }
        return s.substring(getFirstPart(s, x).length() + 1);
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setBackground(new java.awt.Color(153, 153, 255));
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        setFocusable(false);
        setFocusableWindowState(false);
        setFont(new java.awt.Font("Arial", 1, 12));
        setResizable(false);
        setUndecorated(true);
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 262, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 85, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
        if (evt.getButton() == MouseEvent.BUTTON1 && frame.logged) {
            frame.setVisible(true);
        }
        if (!hiding) {
            new Away(false);
        }
    }//GEN-LAST:event_formMouseClicked


    public class Show implements Runnable {

        Thread t = new Thread(this);

        public Show() {
            t.start();
        }


        public void run() {
            setAlwaysOnTop(false);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Rectangle rect = ge.getMaximumWindowBounds();
            int h = rect.height;
            int w = rect.width;
            int w0 = w - getWidth();
            int x = getHeight() + 20;
            int height = getHeight();
            String name1 = null, name2 = null, sub1 = null, sub2 = null, ep = null;
            if (n != null) {
                name1 = getFirstPart(n.name, x);
                name2 = getSecondPart(n.name, x);
                sub1 = getFirstPart(n.subname, x);
                sub2 = getSecondPart(n.subname, x);
                ep = n.episode;
            }
            for (int i = h; i >= h - height; i -= 2) {
                setLocation(w0, i);
//                setSize(getSize().width, h - i + 1);
                drawAll(name1, name2, sub1, sub2, ep);
                setVisible(true);
                sleep(30);
//                try {
//                    Thread.sleep(30);
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(MessageFrame.class.getName()).log(Level.SEVERE, null, ex);
//                    MainFrame.log(ex);
//                }
            }
            if (threesec) {
                new Away(true);
            }
        }


    }

    public class Away implements Runnable {

        private boolean wait = false;

        Thread t = new Thread(this);

        public Away(boolean wait) {
            if (hiding) {
                return;
            }
            this.wait = wait;
            t.start();
        }


        public void run() {
            if (wait) {
                sleep(3000);
//                try {
//                    Thread.sleep(3000);
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(MessageFrame.class.getName()).log(Level.SEVERE, null, ex);
//                    MainFrame.log(ex);
//                }
            }
            if (hiding) {
                return;
            }
            hiding = true;
            setAlwaysOnTop(false);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Rectangle rect = ge.getMaximumWindowBounds();
            int h = rect.height;
            int w = rect.width;
            int w0 = w - This.getWidth();
            int x = getHeight() + 20;
            String name1 = null, name2 = null, sub1 = null, sub2 = null, ep = null;
            if (n != null) {
                name1 = getFirstPart(n.name, x);
                name2 = getSecondPart(n.name, x);
                sub1 = getFirstPart(n.subname, x);
                sub2 = getSecondPart(n.subname, x);
                ep = n.episode;
            }
//            if (!frame.isVisible() && !wait) {
//                frame.setVisible(true);
//            }
            for (int i = h - This.getHeight(); i <= h; i++) {
                This.setLocation(w0, i);
                drawAll(name1, name2, sub1, sub2, ep);
                sleep(15);
//                try {
//                    Thread.sleep(15);
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(MessageFrame.class.getName()).log(Level.SEVERE, null, ex);
//                    MainFrame.log(ex);
//                }
            }
            if (!wait) {
//                SystemTray.getSystemTray().remove(frame.tray);
            }
            running = false;
            This.dispose();
        }


    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
