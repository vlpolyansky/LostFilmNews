/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MainFrame.java
 *
 * Created on 03.12.2010, 22:25:03
 */
package lostfilmnews;

import java.awt.AWTException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javabots.CookieUtility;
import javabots.FormUtility;
import javabots.HTMLTag;
import javabots.ParseHTML;
import javax.imageio.ImageIO;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Polyansky Vladislav
 */
public class MainFrame extends javax.swing.JFrame implements Runnable {
    
    public static final String version = "v1.0";

    //------------------------
    public BufferedImage avatar_image = null;

    public String[] profInfo = null;
    //------------------------

    private Thread t = new Thread(this);

    public static PrintWriter logfile = null;

    private MainFrame This = this;

    private Refresher refresher = null;

    private CookieUtility cu = new CookieUtility();

    public boolean logged = false;

    public boolean running = true;

    public boolean refreshing = false;

    public boolean paused = false;

    public boolean firstlog = true;

    private String login = null, pass = null;

    public NewsGetter.New chosen = null, prev = null;

    public NewsGetter ng;

    public TrayIcon tray;

    public Preferences mainnode = null,
            settingsnode = null;

    public int sleep = 5;

    private boolean first = true;

    public void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            log(ex);
        }
    }


    public void paintChosen() {
        try {
            Graphics2D g2 = (Graphics2D) getGraphics();
            if (chosen != null) {
                g2.drawImage(chosen.image, jScrollPane1.getLocation().x + 6,
                         200, 150, 130, null);
//                if (chosen != prev) {
//                    if (!chosen.details_link.equals(linkField.getText())) {
//                        linkField.setText(chosen.details_link);
//                    }
//                }
                prev = chosen;
            }
        } catch (Exception ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            log(ex);
        }
    }


    public void paintUser() {
        if (avatar_image == null) {
            return;
        }
        try {
            BufferedImage buf = (BufferedImage) createImage(300, 130);
            Graphics2D g2 = buf.createGraphics();
            g2.drawImage(avatar_image, 0, 0, 130 - 30, 130 - 30, null);
            for (int i = 0; i < profInfo.length; i++) {
                String s = profInfo[i];
                g2.drawString(s, 130 - 30 + 5,
                        (i + 1) * 20 - 5);
            }

            Graphics2D g = (Graphics2D) getGraphics();
            g.drawImage(buf, jScrollPane1.getLocation().x + 6 + 150 + 10,
                    200, null);
        } catch (Exception ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            log(ex);
        }
    }


    public void run() {
        refresher = new Refresher();
        while (running) {
            while (paused) {
                sleep(500);
            }
            paintChosen();
            paintUser();
            sleep(30);
        }
    }


    public static void log(final Throwable ex) {
        if (logfile == null) {
            try {
                logfile = new PrintWriter("log.txt");
//                new Thread(new Runnable() {
//                    public void run() {
//                        JOptionPane.showMessageDialog(null, ex.getLocalizedMessage()
//                                + "\nSee the created log file",
//                                ex.toString(), JOptionPane.ERROR_MESSAGE);
//                    }
//
//                }).start();
            } catch (FileNotFoundException e) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, e);
                return;
            }
        }
        logfile.println("----------------");
        logfile.println(ex);
        for (StackTraceElement stackTraceElement : ex.getStackTrace()) {
            logfile.println("\t" + stackTraceElement);
        }
        logfile.flush();
    }


    public void start() {
        paused = false;
        loadSettings();
        loadInfo();
        tray.getPopupMenu().getItem(0).setEnabled(true);
        tray.getPopupMenu().getItem(1).setEnabled(true);

//        setTitle("LostFilmNews" + "    logged as " + login);

        this.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - this.getWidth()) / 2,
                (Toolkit.getDefaultToolkit().getScreenSize().height - this.getHeight()) / 2);
        list0.setListData(new Object[0]);
        if (firstlog) {
            ng = new NewsGetter(this);
            try {
                refreshing = true;
                NewsGetter.New n = ng.refresh(getType());
                if (n != null) {
                    new MessageFrame(this, n, first? "3sec": "");
                    first = false;
                }
                refreshing = false;
            } catch (Exception ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                if (!ex.getMessage().contains("503")) {
                    log(ex);
                }
            }
        }
        minimize();
        if (firstlog) {
            t.start();
        }
        firstlog = false;
    }


    public MainFrame() {
        initComponents();

        mainnode = Preferences.userRoot().node("lostfilmnews");
        settingsnode = mainnode.node("settings");

        for (WindowListener windowListener : this.getWindowListeners()) {
            this.removeWindowListener(windowListener);
        }
        this.addWindowListener(new WindowListener()                  {

            public void windowOpened(WindowEvent e) {
            }


            public void windowClosing(WindowEvent e) {
                formWindowClosing(e);
            }


            public void windowClosed(WindowEvent e) {
            }


            public void windowIconified(WindowEvent e) {
            }


            public void windowDeiconified(WindowEvent e) {
            }


            public void windowActivated(WindowEvent e) {
            }


            public void windowDeactivated(WindowEvent e) {
            }


        });
        list0.addListSelectionListener(new ListSelectionListener()                  {

            public void valueChanged(ListSelectionEvent e) {
                NewsGetter.New n = (NewsGetter.New) list0.getSelectedValue();
                chosen = n;
            }


        });
        try {
            URL iconurl = getClass().getResource("/data/icon.gif");
            BufferedImage iconbuf = ImageIO.read(iconurl.openStream());
            setIconImage(iconbuf);
        } catch (Exception ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            log(ex);
        }
        createTrayIcon();
        try {
            SystemTray.getSystemTray().add(tray);
        } catch (AWTException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            log(ex);
        }
        new LoginFrame(this);
    }


    private void createTrayIcon() {
        tray = new TrayIcon(getIconImage().getScaledInstance(16, 16, 100), "LostFilmNews", createPopupMenu());
        tray.addActionListener(new ActionListener()                  {

            public void actionPerformed(ActionEvent e) {
                if (logged) {
                    setVisible(true);
                }
            }


        });
    }


    private PopupMenu createPopupMenu() throws
            HeadlessException {

        PopupMenu menu = new PopupMenu();

        MenuItem exit = new MenuItem("Exit");
        MenuItem show = new MenuItem("Show");

        exit.addActionListener(new ActionListener()                  {

            public void actionPerformed(ActionEvent e) {

                System.exit(0);

            }


        });
        show.addActionListener(new ActionListener()                  {

            public void actionPerformed(ActionEvent e) {
                setVisible(true);
//                SystemTray.getSystemTray().remove(tray);
            }


        });

        show.setEnabled(false);

        menu.add(show);
        menu.add(exit);

        return menu;

    }


    private String getType() {
        if (sdButton.isSelected()) {
            return "sd";
        } else {
            return "hd";
        }
    }


    private void ifFirstRun() throws MalformedURLException, IOException {
    }


    public boolean loadInfo() {
        if (!logged) {
            return false;
        }
        try {
            HttpURLConnection http = (HttpURLConnection) (new URL("http://lostfilm.tv").openConnection());
            cu.saveCookies(http);
            ParseHTML parse = new ParseHTML(http.getInputStream());
            HTMLTag start = new HTMLTag();
            start.setName("div");
            start.setAttribute("class", "user_avatar");
            NewsGetter.advance(parse, start, 0);
            NewsGetter.advance(parse, "img", 0);
            String imglink = "http://lostfilm.tv" + parse.getTag().getAttributeValue("src");
            avatar_image = NewsGetter.downloadImage(new URL(imglink));

            profInfo = new String[5];
            int i = 0;
            while (i < 5) {
                int ch;
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                while ((ch = parse.read()) != -1000) {
                    if (ch == 1000) {
                        if (parse.getTag().getName().equals("/span")) {
                            profInfo[i] = new String(bout.toByteArray(), "windows-1251");
                            i++;
                            break;
                        }
                    } else {
                        bout.write(ch);
                    }
                }
            }

            return true;
        } catch (Exception ex) {
            if (ex instanceof IOException && ex.getMessage() != null && 
                    ex.getMessage().contains("503")) {
                return loadInfo();
            }
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            log(ex);
            return false;
        }
    }


    public void minimize() {
//            SystemTray.getSystemTray().add(tray);
        setVisible(false);
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        hdsdGroup = new javax.swing.ButtonGroup();
        jMenuItem3 = new javax.swing.JMenuItem();
        jScrollPane1 = new javax.swing.JScrollPane();
        list0 = new javax.swing.JList();
        refreshButton = new javax.swing.JButton();
        downloadButton = new javax.swing.JButton();
        sdButton = new javax.swing.JRadioButton();
        hdButton = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        sleepField = new javax.swing.JTextField();
        sleepScrollBar = new javax.swing.JScrollBar();
        exitButton = new javax.swing.JButton();
        logoutButton = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();

        jMenuItem3.setText("jMenuItem3");

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("LostFilm Новинки");
        setResizable(false);

        list0.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        list0.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(list0);

        refreshButton.setText("Обновить");
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        downloadButton.setText("Скачать");
        downloadButton.setEnabled(false);
        downloadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downloadButtonActionPerformed(evt);
            }
        });

        hdsdGroup.add(sdButton);
        sdButton.setSelected(true);
        sdButton.setText("sd");
        sdButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sdButtonActionPerformed(evt);
            }
        });

        hdsdGroup.add(hdButton);
        hdButton.setText("hd");
        hdButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hdButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Обновлять каждые");

        jLabel2.setText("сек.");

        sleepField.setEditable(false);
        sleepField.setText("5");

        sleepScrollBar.setBlockIncrement(1);
        sleepScrollBar.setMaximum(-1);
        sleepScrollBar.setMinimum(-60);
        sleepScrollBar.setValue(-5);
        sleepScrollBar.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                sleepScrollBarAdjustmentValueChanged(evt);
            }
        });

        exitButton.setText("Выход");
        exitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitButtonActionPerformed(evt);
            }
        });

        logoutButton.setText("LogOut");
        logoutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logoutButtonActionPerformed(evt);
            }
        });

        jMenu1.setText("Autorun");

        jMenuItem1.setText("Add");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuItem2.setText("Remove");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Help");

        jMenuItem4.setText("О программе");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem4);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 562, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sleepField, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sleepScrollBar, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(hdButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sdButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(exitButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(logoutButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(downloadButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(refreshButton))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(sleepField, javax.swing.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
                        .addComponent(jLabel1))
                    .addComponent(sleepScrollBar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(refreshButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(sdButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(hdButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel2)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(downloadButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(logoutButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(exitButton)
                .addGap(37, 37, 37))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        try {
            refresher.refresh(false);
        } catch (Exception ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            log(ex);
        }
        list0.setListData(ng.news.toArray());
    }//GEN-LAST:event_refreshButtonActionPerformed


    private boolean logIn(String login, String pass) {
        //FormLogin=login&FormPassword=pass&module=1&repage=user&act=login
        if (logged) {
            return false;
        }
        URL url;
        try {
            url = new URL("http://lostfilm.tv/useri.php");
        } catch (MalformedURLException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            log(ex);
            return false;
        }
        try {
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setDoOutput(true);
            http.setRequestProperty("Host", "lostfilm.tv");
            http.setRequestProperty("Connection", "keep-alive");
            http.setRequestProperty("Referer", "http://lostfilm.tv/");
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            http.setRequestProperty("Accept", "application/xml,application/xhtml+xml,"
                    + "text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
            http.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) "
                    + "AppleWebKit/534.13 (KHTML, like Gecko) Chrome/9.0.597.19 Safari/534.13");
            FormUtility fu = new FormUtility(http.getOutputStream(), null);
            fu.add("FormLogin", login);
            fu.add("FormPassword", pass);
            fu.add("module", "1");
            fu.add("repage", "user");
            fu.add("act", "login");
            fu.complete();
//            cu = new CookieUtility();
            cu.getMap().clear();
            cu.loadCookies(http);
            if (cu.getMap().size() > 0) {
                logged = true;
                downloadButton.setEnabled(true);
            } else {
                downloadButton.setEnabled(false);
            }
            return cu.getMap().size() > 0;
        } catch (IOException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            log(ex);
            return false;
        }
    }


    private void downloadPage(String surl) throws Exception {
        URL url = new URL(surl);
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
//        http.setDoOutput(true);
        cu.saveCookies(http);
        byte b[] = new byte[100000];
        InputStream in = http.getInputStream();
        OutputStream out = new FileOutputStream("loaded.html");
        int size;
        while ((size = in.read(b)) != -1) {
            out.write(b, 0, size);
        }
        out.close();
    }

    private void downloadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downloadButtonActionPerformed
        if (!logged) {
            return;
        }
        if (chosen != null) {
            try {
                URL url = new URL(chosen.torrent_link);
                cu.getMap().put("dlt", chosen.dlt_cookie);
                cu.getMap().put("dlt_2", chosen.dlt_cookie);

                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setRequestProperty("Host", "lostfilm.tv");
                http.setRequestProperty("Connection", "keep-alive");
                http.setRequestProperty("Referer", "http://lostfilm.tv/");
                http.setRequestProperty("Accept", "application/xml,application/xhtml+xml,"
                        + "text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
                http.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) "
                        + "AppleWebKit/534.13 (KHTML, like Gecko) Chrome/9.0.597.19 Safari/534.13");
                http.setRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
                http.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.6,en;q=0.4");
                http.setRequestProperty("Accept-Charset", "UTF-8,*;q=0.5");
                cu.saveCookies(http);
                InputStream in = http.getInputStream();
                String fileName = chosen.torrent_fname;
//                OutputStream out = System.out;
                FileOutputStream out = new FileOutputStream(fileName);
                byte[] b = new byte[10000];
                int size;
                while ((size = in.read(b)) != -1) {
                    out.write(b, 0, size);
                }
                out.close();
                Runtime.getRuntime().exec("cmd /c start " + fileName);
            } catch (Exception ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                log(ex);
            }
        }
    }//GEN-LAST:event_downloadButtonActionPerformed


    public void loginExit() {
        logged = false;
        downloadButton.setEnabled(false);
        avatar_image = null;
    }


    public boolean loginEnter(String login, String pass) {
        this.login = login;
        this.pass = pass;
        logged = false;
        return logIn(login, pass);
    }

    
    public void saveSetting(String key, String value) {
        settingsnode.put(key, value);
    }

    public void loadSettings() {
        String delay = settingsnode.get("delay", null);
        if (delay != null) {
            sleep = new Integer(delay);
            sleepField.setText(delay);
            sleepScrollBar.setValue(-sleep);
        }
        String def = settingsnode.get("def", null);
        if (def != null) {
            if (def.equals("hd")) {
                hdButton.setSelected(true);
            } else if (def.equals("sd")) {
                sdButton.setSelected(true);
            } else if (def.equals("both")) {
                
            }
        }
    }

    private void sleepScrollBarAdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {//GEN-FIRST:event_sleepScrollBarAdjustmentValueChanged
        sleep = sleepScrollBar.getValue() * (-1);
        sleepField.setText(sleep + "");
        saveSetting("delay", sleep + "");
    }//GEN-LAST:event_sleepScrollBarAdjustmentValueChanged

    private void exitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitButtonActionPerformed
        System.exit(0);
    }


    @Override
    public void paintComponents(Graphics g) {
        super.paintComponents(g);
    }//GEN-LAST:event_exitButtonActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        addToAutorun();
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        removeFromAutorun();
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void hdButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hdButtonActionPerformed
        refresher.refresh(true);
        saveSetting("def", "hd");
    }//GEN-LAST:event_hdButtonActionPerformed

    private void sdButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sdButtonActionPerformed
        refresher.refresh(true);
        saveSetting("def", "sd");
    }//GEN-LAST:event_sdButtonActionPerformed

    private void logoutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoutButtonActionPerformed
        mainnode.put("auto", "no");
        mainnode.put("login", "");
        mainnode.put("pass", "");
        loginExit();
        tray.getPopupMenu().getItem(0).setEnabled(false);
        setVisible(false);
        paused = true;
        new LoginFrame(this);
    }//GEN-LAST:event_logoutButtonActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        final AboutFrame af = new AboutFrame(this);
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                af.setVisible(true);
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
            }
        });
    }//GEN-LAST:event_jMenuItem4ActionPerformed


    private void formWindowClosing(java.awt.event.WindowEvent evt) {
        minimize();
    }


    private byte[] normalizeString(String s) {
        byte[] b = new byte[s.length() * 2 + 2];
        b[0] = -1;
        b[1] = -2;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch <= 'Я' && ch >= 'А' || ch <= 'я' && ch >= 'а') {
                b[i * 2 + 1 + 2] = 4;
            } else {
                b[i * 2 + 1 + 2] = 0;
            }
            b[i * 2 + 2] = (byte) ch;

        }
        return b;
    }


    private void addToAutorun() {
        String path = System.getProperty("user.dir");
        path = path.replace("\\", "\\\\");
        String runPath = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\"
                + "CurrentVersion\\Run";
        String filename = "LostFilmNews";
        try {
            FileOutputStream reg = new FileOutputStream("autorun.reg");
            String write = "Windows Registry Editor Version 5.00\n\n";
            write += "[" + runPath + "]\n";
            write += "\"" + filename + "\"=\""
                    + path + "\\\\" + filename + ".jar\"\n";
            reg.write(normalizeString(write));
            reg.close();
            Runtime.getRuntime().exec("regedit /s autorun.reg").waitFor();
            File regf = new File("autorun.reg");
            regf.delete();
        } catch (Exception ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            log(ex);
        }
    }


    private void removeFromAutorun() {
        String path = System.getProperty("user.dir");
        path = path.replace("\\", "\\\\");
        String runPath = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\"
                + "CurrentVersion\\Run";
        String filename = "LostFilmNews";
        try {
            // <editor-fold defaultstate="collapsed" desc="for me">
//            FileInputStream in1 = new FileInputStream("run.reg");
//            byte[] b1 = new byte[100000];
//            int sz1 = in1.read(b1);
//            FileInputStream in2 = new FileInputStream("regg.reg");
//            byte[] b2 = new byte[100000];
//            int sz2 = in2.read(b2);
//            String s1 = new String(b1, 0, sz1);
//            String s2 = new String(b2, 0, sz2);
//            System.out.println(s1);
//            normalizeString(s1);
//            for (int i = 0; i < sz1; i++) {
//                System.out.print(b1[i] + " ");
//            }
//            System.out.println();
//            System.out.println(s2);
//            for (int i = 0; i < sz2; i++) {
//                System.out.print(b2[i] + " ");
//            }
//            System.out.println();
//            System.exit(0);
            // </editor-fold>

            FileOutputStream reg = new FileOutputStream("autorun.reg");
            String write = "Windows Registry Editor Version 5.00\n\n";
            write += "[" + runPath + "]\n";
            write += "\"" + filename + "\"=-";
            reg.write(normalizeString(write));
            reg.close();
            Runtime.getRuntime().exec("regedit /s autorun.reg").waitFor();
            File regf = new File("autorun.reg");
            regf.delete();
        } catch (Exception ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            log(ex);
        }
    }
    
    void a(Point2D a) {
        System.out.println(1);
    }
    
    void b(BufferedImage a) {
        System.out.println(2);
    }


    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable()                  {

            public void run() {
                new MainFrame().setVisible(true);
            }


        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton downloadButton;
    private javax.swing.JButton exitButton;
    private javax.swing.JRadioButton hdButton;
    private javax.swing.ButtonGroup hdsdGroup;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList list0;
    private javax.swing.JButton logoutButton;
    private javax.swing.JButton refreshButton;
    private javax.swing.JRadioButton sdButton;
    private javax.swing.JTextField sleepField;
    private javax.swing.JScrollBar sleepScrollBar;
    // End of variables declaration//GEN-END:variables

    public class Refresher implements Runnable {

        Thread t = new Thread(this);

        boolean running = true;

        public Refresher() {
            t.start();
        }


        public void refresh(boolean dontshow) {
            if (!refreshing) {
                refreshing = true;
                NewsGetter.New n = null;
                try {
                    n = ng.refresh(getType());
                } catch (IOException ex) {
                    refreshing = false;
                    if (!ex.getMessage().contains("503")) {
                        Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                        log(ex);
                    } else {
                        sleep(100);
                        refresh(dontshow);
                    }
                    return;
                }
                int idx = list0.getSelectedIndex();
                list0.setListData(ng.news.toArray());
                list0.setSelectedIndex(idx);
                if (idx == -1) {
                    list0.setSelectedIndex(0);
                }
                if (n != null && !dontshow) {
                    new MessageFrame(This, n, first? "3sec": "");
                    first = false;
                }
                refreshing = false;
            }
        }


        public void run() {
            int i = 0;
            while (running) {
                if (i >= 60) {
                    i -= 60;
                    loadInfo();
                }
                while (paused) {
                    sleep(500);
                }
                try {
                    refresh(false);
                } catch (Exception ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                    log(ex);
                }
                i += sleep;
                sleep(1000 * sleep);
            }
        }


    }

}
