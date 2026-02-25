package jrocessing.core;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class JApplet extends JPanel implements Runnable, MouseListener, MouseMotionListener, KeyListener {
    public int width = 100;
    public int height = 100;
    
    protected Graphics2D g2;
    private BufferedImage canvas;
    private boolean looping = true;
    private float frameRate = 60;
    protected int frameCount = 0;

    // Input state
    public int mouseX, mouseY;
    public int pmouseX, pmouseY;
    public boolean mousePressed;
    public int mouseButton;
    public char key;
    public int keyCode;
    public boolean keyPressed;

    // Drawing state
    public static final int RGB = 1;
    public static final int HSB = 2;
    public static final int CORNER = 0;
    public static final int CORNERS = 1;
    public static final int CENTER = 2;
    public static final int RADIUS = 3;

    private int colorMode = RGB;
    private float colorMax1 = 255, colorMax2 = 255, colorMax3 = 255, colorMaxA = 255;

    private int rectMode = CORNER;
    private int ellipseMode = CENTER;

    private boolean doFill = true;
    private Color fillColor = Color.WHITE;
    private boolean doStroke = true;
    private Color strokeColor = Color.BLACK;
    private float strokeWeight = 1.0f;

    private JFrame frame;
    
    public JApplet() {
        setPreferredSize(new Dimension(width, height));
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        setFocusable(true);
    }

    public void setup() {
        // To be overridden by user
    }

    public void draw() {
        // To be overridden by user
    }

    public void mousePressed() {}
    public void mouseReleased() {}
    public void mouseClicked() {}
    public void mouseMoved() {}
    public void mouseDragged() {}
    public void keyPressed() {}
    public void keyReleased() {}
    public void keyTyped() {}

    public void size(int w, int h) {
        this.width = w;
        this.height = h;
        setPreferredSize(new Dimension(width, height));
        if (frame != null) {
            frame.pack();
            frame.setLocationRelativeTo(null);
        }
        initCanvas();
    }

    private void initCanvas() {
        canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2 = canvas.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Default styles
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, width, height);
        g2.setColor(Color.BLACK);
    }

    public void background(int color) {
        if ((color & 0xFF000000) != 0 || color > 255) {
            // Probably a hex color or has alpha
            Color c = new Color(color, true);
            background(c.getRed(), c.getGreen(), c.getBlue());
        } else {
            // Grayscale
            background(color, color, color);
        }
    }

    public void colorMode(int mode) {
        this.colorMode = mode;
    }

    public void colorMode(int mode, float max) {
        this.colorMode = mode;
        this.colorMax1 = max;
        this.colorMax2 = max;
        this.colorMax3 = max;
        this.colorMaxA = max;
    }

    private Color getColor(float v1, float v2, float v3) {
        if (colorMode == RGB) {
            return new Color(
                (int)constrain(map(v1, 0, colorMax1, 0, 255), 0, 255),
                (int)constrain(map(v2, 0, colorMax2, 0, 255), 0, 255),
                (int)constrain(map(v3, 0, colorMax3, 0, 255), 0, 255)
            );
        } else {
            return Color.getHSBColor(
                constrain(v1 / colorMax1, 0, 1),
                constrain(v2 / colorMax2, 0, 1),
                constrain(v3 / colorMax3, 0, 1)
            );
        }
    }

    public void background(int r, int g, int b) {
        Color oldColor = g2.getColor();
        g2.setColor(getColor(r, g, b));
        g2.fillRect(0, 0, width, height);
        g2.setColor(oldColor);
    }

    public void stroke(float gray) {
        stroke(gray, gray, gray);
    }

    public void stroke(float v1, float v2, float v3) {
        strokeColor = getColor(v1, v2, v3);
        doStroke = true;
    }

    public void noStroke() {
        doStroke = false;
    }

    public void strokeWeight(float weight) {
        strokeWeight = weight;
        g2.setStroke(new BasicStroke(weight));
    }

    public void fill(float gray) {
        fill(gray, gray, gray);
    }

    public void fill(float v1, float v2, float v3) {
        fillColor = getColor(v1, v2, v3);
        doFill = true;
    }

    public void noFill() {
        doFill = false;
    }

    public void rectMode(int mode) {
        this.rectMode = mode;
    }

    public void ellipseMode(int mode) {
        this.ellipseMode = mode;
    }

    public void line(float x1, float y1, float x2, float y2) {
        if (doStroke) {
            g2.setColor(strokeColor);
            g2.draw(new java.awt.geom.Line2D.Float(x1, y1, x2, y2));
        }
    }

    public void rect(float a, float b, float c, float d) {
        float x = a, y = b, w = c, h = d;
        if (rectMode == CORNERS) {
            w = c - a;
            h = d - b;
        } else if (rectMode == CENTER) {
            x = a - c / 2f;
            y = b - d / 2f;
        } else if (rectMode == RADIUS) {
            x = a - c;
            y = b - d;
            w = c * 2;
            h = d * 2;
        }

        if (doFill) {
            g2.setColor(fillColor);
            g2.fill(new java.awt.geom.Rectangle2D.Float(x, y, w, h));
        }
        if (doStroke) {
            g2.setColor(strokeColor);
            g2.draw(new java.awt.geom.Rectangle2D.Float(x, y, w, h));
        }
    }

    public void ellipse(float a, float b, float c, float d) {
        float x = a, y = b, w = c, h = d;
        if (ellipseMode == CORNER) {
            // x, y is corner
        } else if (ellipseMode == CORNERS) {
            w = c - a;
            h = d - b;
        } else if (ellipseMode == CENTER) {
            x = a - c / 2f;
            y = b - d / 2f;
        } else if (ellipseMode == RADIUS) {
            x = a - c;
            y = b - d;
            w = c * 2;
            h = d * 2;
        }

        if (doFill) {
            g2.setColor(fillColor);
            g2.fill(new java.awt.geom.Ellipse2D.Float(x, y, w, h));
        }
        if (doStroke) {
            g2.setColor(strokeColor);
            g2.draw(new java.awt.geom.Ellipse2D.Float(x, y, w, h));
        }
    }

    public void point(float x, float y) {
        if (doStroke) {
            g2.setColor(strokeColor);
            g2.draw(new java.awt.geom.Line2D.Float(x, y, x, y));
        }
    }

    // --- Math ---
    public float random(float max) {
        return (float) Math.random() * max;
    }

    public float random(float min, float max) {
        return min + (float) Math.random() * (max - min);
    }

    public static float map(float value, float start1, float stop1, float start2, float stop2) {
        return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
    }

    public static float dist(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    public static float lerp(float start, float stop, float amt) {
        return start + (stop - start) * amt;
    }

    public static float constrain(float n, float low, float high) {
        return Math.max(Math.min(n, high), low);
    }

    // --- Mouse Events ---
    @Override public void mousePressed(MouseEvent e) {
        mouseX = e.getX(); mouseY = e.getY();
        mousePressed = true;
        mouseButton = e.getButton();
        mousePressed();
    }
    @Override public void mouseReleased(MouseEvent e) {
        mouseX = e.getX(); mouseY = e.getY();
        mousePressed = false;
        mouseReleased();
    }
    @Override public void mouseClicked(MouseEvent e) { mouseClicked(); }
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {
        pmouseX = mouseX; pmouseY = mouseY;
        mouseX = e.getX(); mouseY = e.getY();
        mouseMoved();
    }
    @Override public void mouseDragged(MouseEvent e) {
        pmouseX = mouseX; pmouseY = mouseY;
        mouseX = e.getX(); mouseY = e.getY();
        mouseDragged();
    }

    // --- Keyboard Events ---
    @Override public void keyPressed(KeyEvent e) {
        key = e.getKeyChar();
        keyCode = e.getKeyCode();
        keyPressed = true;
        keyPressed();
    }
    @Override public void keyReleased(KeyEvent e) {
        key = e.getKeyChar();
        keyCode = e.getKeyCode();
        keyPressed = false;
        keyReleased();
    }
    @Override public void keyTyped(KeyEvent e) {
        key = e.getKeyChar();
        keyTyped();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (canvas != null) {
            g.drawImage(canvas, 0, 0, null);
        }
    }

    public void run() {
        setup();
        if (canvas == null) {
            initCanvas();
        }
        
        while (Thread.currentThread() == animationThread) {
            long startTime = System.currentTimeMillis();
            
            draw();
            repaint();
            frameCount++;
            
            long endTime = System.currentTimeMillis();
            long waitTime = (long)(1000 / frameRate) - (endTime - startTime);
            
            if (waitTime > 0) {
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    private Thread animationThread;

    public static void main(String[] args) {
        // This would be called by the runner
        if (args.length < 1) {
            System.err.println("Usage: JApplet <sketch-class-name>");
            System.exit(1);
        }
        runSketch(args);
    }

    public static void runSketch(String[] args) {
        String sketchClassName = args[0];
        try {
            Class<?> sketchClass = Class.forName(sketchClassName);
            JApplet sketch = (JApplet) sketchClass.getDeclaredConstructor().newInstance();
            
            JFrame frame = new JFrame(sketchClassName);
            sketch.frame = frame;
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(sketch);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            sketch.requestFocusInWindow();
            
            sketch.animationThread = new Thread(sketch);
            sketch.animationThread.start();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
