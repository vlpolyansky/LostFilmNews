// Translucent and shaped windows

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import com.sun.awt.*;

public class TranslucentTestFrame extends JFrame {

    private JPanel mainPanel;
	private JPanel panel;
	private JButton closeButton;
	private float opacity;
    private int x1,  y1;
    private int x2,  y2;

    public TranslucentTestFrame() {
        setTitle("Test translucent");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setUndecorated(true);

		opacity = 1.0f;

        mainPanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                int R = 100;
                int G = 250;
                int B = 50;
                GradientPaint gradient = new GradientPaint(0.0f, 0.0f, new Color(R, G, B, 100),
                        getWidth(), getHeight(), new Color(R, G, B, 100), true);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setDoubleBuffered(false);
        mainPanel.setLayout(null);

        panel = new JPanel();
        panel.setBounds(10, 10, getWidth() - 20, getHeight() - 20);
        panel.setBackground(new Color(230, 215, 50, 100));
        mainPanel.add(panel);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                x1 = e.getX();
                y1 = e.getY();
                setCursor(Cursor.HAND_CURSOR);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                setCursor(Cursor.DEFAULT_CURSOR);
            }
        });

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                x2 = e.getX();
                y2 = e.getY();
                setLocation(getX() + (x2 - x1), getY() + (y2 - y1));
            }
        });

        closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        });

        panel.add(closeButton, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);

        AWTUtilities.setWindowOpacity(this, opacity);
        AWTUtilities.setWindowOpaque(this, false);
        Shape shape = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 50, 50);
        AWTUtilities.setWindowShape(this, shape);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TranslucentTestFrame();
            }
        });
    }
}

