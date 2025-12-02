package com.defendersofsolara.ui;

import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Utility class for consistent font rendering across the application.
 * 
 * Ensures all text is rendered with proper anti-aliasing and quality settings
 * for maximum readability.
 */
public class FontRenderingUtil {
    
    /**
     * Applies optimal rendering hints for text rendering to a Graphics2D object.
     * This ensures fonts are crisp, clear, and readable.
     * 
     * @param g2d The Graphics2D object to configure
     */
    public static void applyTextRenderingHints(Graphics2D g2d) {
        if (g2d == null) {
            return;
        }
        
        // Text anti-aliasing - CRITICAL for readable fonts
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
                             RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // General anti-aliasing for smooth graphics
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                             RenderingHints.VALUE_ANTIALIAS_ON);
        
        // High-quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
                             RenderingHints.VALUE_RENDER_QUALITY);
        
        // Use fractional metrics for better text positioning
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, 
                             RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        
        // For pixel art sprites, use nearest neighbor; for text, use bilinear
        // We'll use bilinear for text rendering
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                             RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }
    
    /**
     * Applies rendering hints optimized for pixel art sprites.
     * Use this when drawing sprites, not text.
     * 
     * @param g2d The Graphics2D object to configure
     */
    public static void applySpriteRenderingHints(Graphics2D g2d) {
        if (g2d == null) {
            return;
        }
        
        // Nearest neighbor for pixel-perfect sprite rendering
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                             RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        
        // Still use anti-aliasing for smooth edges if needed
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                             RenderingHints.VALUE_ANTIALIAS_ON);
        
        // High-quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
                             RenderingHints.VALUE_RENDER_QUALITY);
    }
    
    /**
     * Applies both text and sprite rendering hints.
     * Use this when drawing both text and sprites in the same component.
     * 
     * @param g2d The Graphics2D object to configure
     */
    public static void applyMixedRenderingHints(Graphics2D g2d) {
        if (g2d == null) {
            return;
        }
        
        // Text anti-aliasing for readable fonts
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
                             RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // General anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                             RenderingHints.VALUE_ANTIALIAS_ON);
        
        // High-quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
                             RenderingHints.VALUE_RENDER_QUALITY);
        
        // Fractional metrics for text
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, 
                             RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        
        // Use bilinear for mixed content (can be overridden per-draw if needed)
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                             RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }
}

