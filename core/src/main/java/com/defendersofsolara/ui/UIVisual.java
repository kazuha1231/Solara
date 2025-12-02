package com.defendersofsolara.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*; // <-- THIS LINE IS CRITICAL!
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.function.Consumer;

public class UIVisual {
    // === DUNGEON BACKGROUND PANEL ===
    public static class BackgroundPanel extends JPanel {
        private final JPanel content;
        private BufferedImage bgImage;
        private javax.swing.Timer timer;

        public BackgroundPanel(JPanel content) {
            super(null);
            this.content = content;
            setOpaque(true);
            loadBackgroundImage();
            timer = new javax.swing.Timer(40, e -> { repaint(); });
            timer.start();
            add(content);
            updateContentBounds();
            addComponentListener(new java.awt.event.ComponentAdapter() {
                public void componentResized(java.awt.event.ComponentEvent evt) { updateContentBounds(); }
            });
        }
        private void loadBackgroundImage() {
            try {
                java.net.URL imgURL = getClass().getResource("/image/eldralune_dungeon_bg.png");
                if (imgURL != null)
                    bgImage = javax.imageio.ImageIO.read(imgURL);
            } catch (IOException ignored) {}
        }
        private void updateContentBounds() { content.setBounds(0, 0, getWidth(), getHeight()); }
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            FontRenderingUtil.applyMixedRenderingHints(g2d);
            // PNG or fallback
            if (bgImage != null) {
                int pw = getWidth(), ph = getHeight(), iw = bgImage.getWidth(), ih = bgImage.getHeight();
                double scale = Math.max((double) pw / iw, (double) ph / ih);
                int sw = (int) (iw * scale), sh = (int) (ih * scale);
                int x = (pw - sw) / 2, y = (ph - sh) / 2;
                g2d.drawImage(bgImage, x, y, sw, sh, this);
            } else {
                g2d.setPaint(new GradientPaint(0, 0, new Color(10, 15, 25), 0, getHeight(), new Color(20, 30, 45)));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
            // Overlay for readability
            g2d.setColor(new Color(0, 0, 0, 60));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.dispose();
        }
    }
    // === HEX/SIDE BUTTONS, LOCK SUPPORT, TECH CORNER DECOR ===
    public static class HexButton {
        public String text;
        public int x, y, width, height;
        public Runnable action;
        public boolean locked;

        public HexButton(String text, int x, int y, int width, int height, Runnable action, boolean locked) {
            this.text = text; this.x = x; this.y = y;
            this.width = width; this.height = height;
            this.action = action; this.locked = locked;
        }
        public boolean contains(Point p) {
            return createHex(x, y, width, height).contains(p);
        }
        public void draw(Graphics2D g2d, boolean hovered) {
            Path2D hex = createHex(x, y, width, height);
            g2d.setColor(locked ? new Color(60,60,60,170) : (hovered ? new Color(60,120,180,215) : new Color(35,82,132,205)));
            g2d.fill(hex);
            g2d.setColor(locked ? new Color(90,90,90,240) : (hovered ? new Color(0,255,255) : new Color(80,130,160)));
            g2d.setStroke(new BasicStroke(3)); g2d.draw(hex);
            if(hovered && !locked) {
                g2d.setColor(new Color(0,255,255,95));
                g2d.setStroke(new BasicStroke(7));
                g2d.draw(hex);
            }
            drawTechCorners(g2d, x, y, width, height);

            // lock icon
            if (locked) drawLock(g2d, x + width - 33, y + 17);

            // text
            g2d.setFont(new Font("Arial", Font.BOLD, 22));
            FontMetrics fm = g2d.getFontMetrics();
            int tx = x+(width-fm.stringWidth(text))/2, ty = y+(height+fm.getAscent())/2-9;
            g2d.setColor(locked ? new Color(150,90,90) : (hovered ? Color.white : new Color(200,240,255)));
            g2d.drawString(text, tx, ty);
        }
        static Path2D createHex(int x, int y, int w, int h) {
            Path2D p = new Path2D.Double();
            p.moveTo(x-20,y+h/2); p.lineTo(x+10,y); p.lineTo(x+w-30,y);
            p.lineTo(x+w+20,y+h/2); p.lineTo(x+w-30,y+h); p.lineTo(x+10,y+h); p.closePath();
            return p;
        }
        static void drawTechCorners(Graphics2D g2d,int x,int y,int w,int h) {
            g2d.setStroke(new BasicStroke(2)); g2d.setColor(new Color(55,155,190));
            int cs=10;
            g2d.drawLine(x+10,y-5,x+10+cs,y-5); g2d.drawLine(x+10,y-5,x+10,y+cs);
            g2d.drawLine(x+w-30,y-5,x+w-30+cs,y-5); g2d.drawLine(x+w-30+cs,y-5,x+w-30+cs,y+cs);
            g2d.drawLine(x+w-30+cs,y+h-cs,x+w-30+cs,y+h+5); g2d.drawLine(x+w-30,y+h+5,x+w-30+cs,y+h+5);
        }
        static void drawLock(Graphics2D g2d, int x, int y) {
            g2d.setColor(new Color(255,60,60));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(x-7, y, 14, 12);
            g2d.drawArc(x-8, y-7, 16, 14, 0, 180);
            g2d.fillOval(x-2, y+4, 4, 5);
        }
        public static JButton makeButton(String text, Runnable run) {
            JButton btn = new JButton(text);
            btn.setFont(new Font("Arial", Font.BOLD, 18));
            btn.setBackground(new Color(40, 60, 100));
            btn.setForeground(new Color(0, 220, 255));
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createLineBorder(new Color(0,200,255), 2));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> run.run());
            return btn;
        }
        public static JLabel makeLabel(String txt, int sz, boolean cyan) {
            JLabel l = new JLabel(txt, SwingConstants.CENTER);
            l.setFont(new Font("Arial Black", cyan ? Font.BOLD : Font.PLAIN, sz));
            l.setForeground(cyan ? new Color(0,255,255) : Color.white);
            return l;
        }
    }
    // === GENERIC HEX-PANEL (side menu: for profile/world select AND main menu) ===
    public static class HexPanel extends JPanel {
        private final java.util.List<HexButton> buttons = new java.util.ArrayList<>();
        private HexButton hovered;
        private final String title;
        public HexPanel(String title, java.util.List<String> items, Consumer<Integer> clicked, Runnable back, java.util.function.IntPredicate unlocked) {
            setOpaque(false); setLayout(null); this.title = title;
            int count = items.size(), cx=250, y0=200, s=85, w=440, h=70;
            for(int i=0; i<count; ++i) {
                boolean ok = unlocked==null || unlocked.test(i);
                int xx=cx,yy=y0+i*s, finalI=i;
                buttons.add(new HexButton(items.get(i), xx,yy,w,h, ok? ()->clicked.accept(finalI):null, !ok));
            }
            // Back button at bottom
            buttons.add(new HexButton("BACK", cx, y0+count*s+40,w,h, back, false));
            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseMoved(MouseEvent e) {
                    hovered = null; for(HexButton b:buttons) if(b.contains(e.getPoint())) hovered=b;
                    setCursor(hovered!=null && hovered.action!=null ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR): Cursor.getDefaultCursor()); repaint();
                }
            });
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    for(HexButton b:buttons) if(b.contains(e.getPoint()) && b.action!=null) b.action.run();
                }
            });
        }
        public HexPanel(String title, String[] items, Consumer<Integer> clicked, Runnable back) {
            this(title, java.util.Arrays.asList(items), clicked, back, null);
        }
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g); Graphics2D g2d=(Graphics2D)g;
            g2d.setFont(new Font("Arial Black", Font.BOLD, 46));
            FontMetrics fm = g2d.getFontMetrics();
            int x=(getWidth()-fm.stringWidth(title))/2, y=100;
            g2d.setColor(new Color(0,200,255,150)); g2d.drawString(title,x-2,y-2); g2d.drawString(title,x+2,y+2);
            g2d.setColor(new Color(0,255,255)); g2d.drawString(title, x, y);
            for(HexButton b:buttons) b.draw(g2d, b==hovered);
        }
    }
    // Options/Credits popups
    public static class InfoPanel extends JPanel {
        public InfoPanel(String title, String html, Runnable back) {
            setOpaque(false); setLayout(null);
            JLabel t = HexButton.makeLabel(title, 42, true); t.setBounds(450, 60, 300, 50); add(t);
            JLabel info = new JLabel(html, SwingConstants.CENTER);
            info.setOpaque(false); info.setBounds(350, 140, 500, 240);
            info.setFont(new Font("Arial", Font.PLAIN, 24));
            info.setForeground(new Color(0,255,255)); add(info);
            JButton backBtn = HexButton.makeButton("BACK", back);
            backBtn.setBounds(520, 430, 140, 50); add(backBtn);
        }
    }
    // Options tab - styled (resolution and audio)
    public static class OptionsPanel extends JPanel {
        private int resIdx = 2;
        private int masterVol = 80;
        public OptionsPanel(Runnable back, UnifiedGameUI frame) {
            setOpaque(false); setLayout(null);
            JLabel title = HexButton.makeLabel("OPTIONS", 40, true);
            title.setBounds(400, 30, 400, 60); add(title);

            JTabbedPane tabs = new JTabbedPane();
            tabs.setBounds(250,120,700,380);
            tabs.setFont(new Font("Arial",Font.BOLD,16));

            // --- VIDEO ---
            JPanel v = new JPanel(); v.setOpaque(false); v.setLayout(null);
            JLabel resLabel = new JLabel("Resolution:");
            resLabel.setFont(new Font("Arial", Font.BOLD, 18));
            resLabel.setForeground(new Color(0,255,255));
            resLabel.setBounds(80,60,200,35); v.add(resLabel);

            String[] resolutions = {"800x600", "1024x768", "1280x720", "1920x1080", "Fullscreen"};
            JComboBox<String> resBox = new JComboBox<>(resolutions);
            resBox.setSelectedIndex(resIdx);
            resBox.setBounds(80,105,260,35);
            resBox.setFont(new Font("Arial",Font.BOLD,16));
            v.add(resBox);

            JButton applyBtn = HexButton.makeButton("APPLY", () -> {
                resIdx = resBox.getSelectedIndex();
                frame.applyResolution(resIdx);
            });
            applyBtn.setBounds(80,160,140,40); v.add(applyBtn);
            tabs.addTab("Video", v);

            // --- AUDIO ---
            JPanel a = new JPanel(); a.setOpaque(false); a.setLayout(null);
            JLabel vLabel = new JLabel("Master Volume:");
            vLabel.setFont(new Font("Arial", Font.BOLD, 18));
            vLabel.setForeground(new Color(0,255,255)); vLabel.setBounds(80,60,200,35); a.add(vLabel);
            JSlider volSlider = new JSlider(0,100,masterVol);
            volSlider.setBounds(80,110,260,40);
            volSlider.setOpaque(false);
            a.add(volSlider);
            tabs.addTab("Audio", a);

            add(tabs);

            JButton backBtn = HexButton.makeButton("BACK", back);
            backBtn.setBounds(550, 530, 150, 50);
            add(backBtn);
        }
    }
}
