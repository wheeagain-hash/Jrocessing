package jrocessing.core;

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

public class JImage {
    public int width, height;
    public int[] pixels;
    public BufferedImage img;

    public JImage(int w, int h) {
        this.width = w;
        this.height = h;
        this.img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        this.pixels = new int[w * h];
    }

    public JImage(BufferedImage img) {
        this.img = img;
        this.width = img.getWidth();
        this.height = img.getHeight();
        this.pixels = new int[width * height];
        loadPixels();
    }

    public void loadPixels() {
        img.getRGB(0, 0, width, height, pixels, 0, width);
    }

    public void updatePixels() {
        img.setRGB(0, 0, width, height, pixels, 0, width);
    }

    public JImage get() {
        return copy();
    }

    public JImage copy() {
        JImage newImg = new JImage(width, height);
        System.arraycopy(pixels, 0, newImg.pixels, 0, pixels.length);
        newImg.updatePixels();
        return newImg;
    }

    public void resize(int w, int h) {
        if (w == 0 && h == 0) return;
        if (w == 0) w = (int)(width * ((float)h / height));
        if (h == 0) h = (int)(height * ((float)w / width));

        BufferedImage resized = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();
        g.drawImage(img, 0, 0, w, h, null);
        g.dispose();
        this.img = resized;
        this.width = w;
        this.height = h;
        this.pixels = new int[w * h];
        loadPixels();
    }
}
