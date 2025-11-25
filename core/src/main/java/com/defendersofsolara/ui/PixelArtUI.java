package com.defendersofsolara.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Pixel-art UI system matching the reference style.
 * Uses decorative borders and simple panels.
 */
public class PixelArtUI {
    
    private static final Map<String, BufferedImage> imageCache = new HashMap<>();
    
    // 9-slice border size for pixel-art panels (typically 8px)
    private static final int BORDER_SIZE = 8;
    
    /**
     * Load a pixel-art asset from resources.
     */
    public static BufferedImage loadImage(String resourcePath) {
        if (imageCache.containsKey(resourcePath)) {
            return imageCache.get(resourcePath);
        }
        
        try {
            URL url = PixelArtUI.class.getResource(resourcePath);
            if (url == null) {
                return null;
            }
            BufferedImage img = ImageIO.read(url);
            if (img != null) {
                imageCache.put(resourcePath, img);
            }
            return img;
        } catch (IOException e) {
            return null;
        }
    }
    
    /**
     * Draw a 9-slice image (stretches center, keeps corners/borders intact).
     */
    public static void drawNineSlice(Graphics g, BufferedImage img, int x, int y, int width, int height) {
        if (img == null) return;
        
        int border = BORDER_SIZE;
        int imgW = img.getWidth();
        int imgH = img.getHeight();
        
        // Source regions (9 slices)
        int centerW = imgW - (border * 2);
        int centerH = imgH - (border * 2);
        
        // Destination regions
        int destCenterW = Math.max(0, width - (border * 2));
        int destCenterH = Math.max(0, height - (border * 2));
        
        // Draw 9 slices
        // Top row
        g.drawImage(img, x, y, x + border, y + border,
                   0, 0, border, border, null); // Top-left
        if (destCenterW > 0) {
            g.drawImage(img, x + border, y, x + border + destCenterW, y + border,
                       border, 0, border + centerW, border, null); // Top-center
        }
        g.drawImage(img, x + width - border, y, x + width, y + border,
                   imgW - border, 0, imgW, border, null); // Top-right
        
        // Middle row
        if (destCenterH > 0) {
            g.drawImage(img, x, y + border, x + border, y + border + destCenterH,
                       0, border, border, border + centerH, null); // Middle-left
            if (destCenterW > 0) {
                g.drawImage(img, x + border, y + border, x + border + destCenterW, y + border + destCenterH,
                           border, border, border + centerW, border + centerH, null); // Middle-center
            }
            g.drawImage(img, x + width - border, y + border, x + width, y + border + destCenterH,
                       imgW - border, border, imgW, border + centerH, null); // Middle-right
        }
        
        // Bottom row
        g.drawImage(img, x, y + height - border, x + border, y + height,
                   0, imgH - border, border, imgH, null); // Bottom-left
        if (destCenterW > 0) {
            g.drawImage(img, x + border, y + height - border, x + border + destCenterW, y + height,
                       border, imgH - border, border + centerW, imgH, null); // Bottom-center
        }
        g.drawImage(img, x + width - border, y + height - border, x + width, y + height,
                   imgW - border, imgH - border, imgW, imgH, null); // Bottom-right
    }
    
    /**
     * Create a pixel-art panel with border (matching reference style).
     * Uses Panel assets which include both background and border.
     */
    public static JPanel createPixelPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                
                // Use Panel asset (includes background and border) - matching reference style
                BufferedImage panelImg = loadImage("/kennyresources/PNG/Default/Panel/panel-000.png");
                if (panelImg != null) {
                    drawNineSlice(g2d, panelImg, 0, 0, getWidth(), getHeight());
                } else {
                    // Fallback: dark background with border
                    g2d.setColor(UITheme.BG_PANEL);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    
                    BufferedImage borderImg = loadImage("/kennyresources/PNG/Default/Border/panel-border-000.png");
                    if (borderImg != null) {
                        drawNineSlice(g2d, borderImg, 0, 0, getWidth(), getHeight());
                    } else {
                        g2d.setColor(UITheme.BORDER_NORMAL);
                        g2d.setStroke(new BasicStroke(2f));
                        g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                    }
                }
                
                g2d.dispose();
            }
        };
    }
    
    /**
     * Create a pixel-art panel using a specific panel asset.
     */
    public static JPanel createPixelPanel(String panelPath) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                
                BufferedImage panelImg = loadImage(panelPath);
                if (panelImg != null) {
                    drawNineSlice(g2d, panelImg, 0, 0, getWidth(), getHeight());
                } else {
                    // Fallback
                    g2d.setColor(UITheme.BG_PANEL);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
                
                g2d.dispose();
            }
        };
    }
}

