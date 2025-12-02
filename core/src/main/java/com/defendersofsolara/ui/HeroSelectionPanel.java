package com.defendersofsolara.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

/**
 * Animated hero selection panel for the character selection screen.
 * 
 * Features:
 * - Displays hero sprite animation on hover
 * - Shows hero name and role
 * - Visual feedback with glow/border on hover
 * - Click handling for hero selection
 * 
 * The animation starts when the mouse enters the panel and stops when it leaves.
 */
public class HeroSelectionPanel extends JPanel {
    
    private final String heroName;
    private final String heroRole;
    private final String heroClass;
    private final HeroSpriteAnimation animation;
    private boolean isSelected = false;
    private boolean isHovered = false;
    private Runnable onClickCallback;
    
    // Display settings
    private static final int PORTRAIT_SIZE = 120; // Size of the animated portrait
    private static final int PANEL_WIDTH = 200;
    private static final int PANEL_HEIGHT = 220;
    
    // Font settings for improved readability
    private static final Font HERO_NAME_FONT = new Font("Serif", Font.BOLD, 26);
    private static final Font HERO_ROLE_FONT = new Font("SansSerif", Font.PLAIN, 15);
    
    // Hover effect settings
    private static final float HOVER_SCALE = 1.08f; // 8% larger on hover
    private static final Color GOLD_GLOW = new Color(255, 215, 0, 150); // Gold border glow
    private static final Color TEXT_SHADOW = new Color(0, 0, 0, 180); // Text shadow color
    private static final Color TEXT_COLOR = new Color(244, 228, 193); // Parchment text color
    
    /**
     * Creates a new HeroSelectionPanel.
     * 
     * @param heroName Display name of the hero
     * @param heroRole Role/class description
     * @param heroClass Full class name for selection
     * @param heroResourcePath Resource path to hero assets (e.g., "/lyra/")
     */
    public HeroSelectionPanel(String heroName, String heroRole, String heroClass, String heroResourcePath) {
        this.heroName = heroName;
        this.heroRole = heroRole;
        this.heroClass = heroClass;
        this.animation = new HeroSpriteAnimation(heroResourcePath);
        
        setOpaque(false);
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setMinimumSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setMaximumSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        
        // Enable double buffering for smooth rendering
        setDoubleBuffered(true);
        
        // Set up animation repaint callback
        animation.setRepaintCallback(() -> {
            if (isHovered || isSelected) {
                repaint();
            }
        });
        
        // Set up mouse listeners for hover animation
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                animation.startAnimation();
                repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                if (!isSelected) {
                    animation.stopAnimation();
                }
                repaint();
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onClickCallback != null) {
                    onClickCallback.run();
                }
            }
        });
        
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    
    /**
     * Sets the callback to execute when this panel is clicked.
     */
    public void setOnClick(Runnable callback) {
        this.onClickCallback = callback;
    }
    
    /**
     * Sets the selected state of this panel.
     */
    public void setSelected(boolean selected) {
        this.isSelected = selected;
        if (selected) {
            animation.startAnimation(); // Keep animating when selected
        } else {
            animation.stopAnimation();
        }
        repaint();
    }
    
    /**
     * Gets whether this panel is selected.
     */
    public boolean isSelected() {
        return isSelected;
    }
    
    /**
     * Gets the hero class name.
     */
    public String getHeroClass() {
        return heroClass;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        
        // Enable high-quality rendering with text anti-aliasing for maximum readability
        FontRenderingUtil.applyMixedRenderingHints(g2d);
        
        int width = getWidth();
        int height = getHeight();
        
        // Draw background panel
        Color bgColor = isSelected ? new Color(UITheme.PRIMARY_GREEN.getRed(), 
                                                UITheme.PRIMARY_GREEN.getGreen(), 
                                                UITheme.PRIMARY_GREEN.getBlue(), 50) 
                                    : new Color(UITheme.BG_CARD.getRed(), 
                                                UITheme.BG_CARD.getGreen(), 
                                                UITheme.BG_CARD.getBlue(), 150);
        g2d.setColor(bgColor);
        g2d.fillRect(0, 0, width, height);
        
        // Draw border with glow effect on hover/selection
        Color borderColor;
        int borderWidth;
        if (isSelected) {
            borderColor = UITheme.PRIMARY_GREEN;
            borderWidth = 3;
        } else if (isHovered) {
            // Gold border glow on hover
            borderColor = GOLD_GLOW;
            borderWidth = 4;
            
            // Draw outer glow effect
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
            g2d.setColor(GOLD_GLOW);
            g2d.setStroke(new BasicStroke(6f));
            g2d.drawRect(1, 1, width - 3, height - 3);
            g2d.setComposite(AlphaComposite.SrcOver);
        } else {
            borderColor = UITheme.BORDER_NORMAL;
            borderWidth = 2;
        }
        
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(borderWidth));
        g2d.drawRect(1, 1, width - 3, height - 3);
        
        // Draw animated sprite portrait with hover scale effect
        BufferedImage currentFrame = animation.getCurrentFrame();
        if (currentFrame != null) {
            int baseSpriteX = (width - PORTRAIT_SIZE) / 2;
            int baseSpriteY = 15; // Top padding
            
            // Apply scale effect on hover
            float scale = isHovered ? HOVER_SCALE : 1.0f;
            int scaledSize = (int) (PORTRAIT_SIZE * scale);
            int spriteX = baseSpriteX - (scaledSize - PORTRAIT_SIZE) / 2;
            int spriteY = baseSpriteY - (scaledSize - PORTRAIT_SIZE) / 2;
            
            // Apply brightness increase on hover using RescaleOp
            BufferedImage displayFrame = currentFrame;
            if (isHovered) {
                RescaleOp rescaleOp = new RescaleOp(1.2f, 0, null);
                displayFrame = rescaleOp.filter(currentFrame, null);
            }
            
            // Draw the sprite flipped horizontally to face backward (left)
            g2d.drawImage(displayFrame, 
                         spriteX + scaledSize, spriteY,  // Right x first (flipped)
                         spriteX, spriteY + scaledSize,  // Left x second (flipped)
                         0, 0, 
                         displayFrame.getWidth(), displayFrame.getHeight(),
                         null);
        } else {
            // Draw placeholder if no sprite available
            g2d.setColor(UITheme.TEXT_DIM);
            g2d.setFont(UITheme.FONT_TEXT.deriveFont(Font.BOLD, 48f));
            FontMetrics fm = g2d.getFontMetrics();
            String placeholder = "?";
            int textX = (width - fm.stringWidth(placeholder)) / 2;
            int textY = 15 + PORTRAIT_SIZE / 2 + fm.getAscent() / 2;
            g2d.drawString(placeholder, textX, textY);
        }
        
        // Draw hero name with improved font and text shadow
        g2d.setFont(HERO_NAME_FONT);
        FontMetrics nameFm = g2d.getFontMetrics();
        int nameY = 15 + PORTRAIT_SIZE + 20;
        int nameX = (width - nameFm.stringWidth(heroName)) / 2;
        
        // Draw text shadow first
        g2d.setColor(TEXT_SHADOW);
        g2d.drawString(heroName, nameX + 2, nameY + 2);
        
        // Draw main text
        g2d.setColor(TEXT_COLOR);
        g2d.drawString(heroName, nameX, nameY);
        
        // Draw hero role with improved font and text shadow
        g2d.setFont(HERO_ROLE_FONT);
        FontMetrics roleFm = g2d.getFontMetrics();
        int roleY = nameY + roleFm.getHeight() + 8;
        int roleX = (width - roleFm.stringWidth(heroRole)) / 2;
        
        // Draw text shadow first
        g2d.setColor(TEXT_SHADOW);
        g2d.drawString(heroRole, roleX + 2, roleY + 2);
        
        // Draw main text - use readable light gray/blue instead of unreadable blue
        g2d.setColor(new Color(200, 210, 220)); // Light gray/blue - readable against black
        g2d.drawString(heroRole, roleX, roleY);
        
        // Draw selection indicator
        if (isSelected) {
            g2d.setFont(UITheme.FONT_TEXT.deriveFont(Font.BOLD, 12f));
            g2d.setColor(UITheme.PRIMARY_GREEN);
            FontMetrics selectFm = g2d.getFontMetrics();
            String selectedText = "SELECTED";
            int selectX = (width - selectFm.stringWidth(selectedText)) / 2;
            int selectY = height - 10;
            
            // Draw text shadow
            g2d.setColor(TEXT_SHADOW);
            g2d.drawString(selectedText, selectX + 1, selectY + 1);
            
            // Draw main text
            g2d.setColor(UITheme.PRIMARY_GREEN);
            g2d.drawString(selectedText, selectX, selectY);
        }
        
        g2d.dispose();
    }
    
    /**
     * Cleans up resources when panel is removed.
     */
    public void dispose() {
        animation.dispose();
    }
}

