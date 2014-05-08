/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lostfilmnews;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;

/**
 *
 * @author Polyansky Vladislav
 */
public class StartClass {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            MainFrame.log(ex);
        }

        new MainFrame();
    }


}
