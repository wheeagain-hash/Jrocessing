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

    public static final int CLOSE = 1;

    public static final int LEFT = 37;
    public static final int RIGHT = 39;
    public static final int TOP = 101;
    public static final int BOTTOM = 102;
    public static final int BASELINE = 0;

    private int colorMode = RGB;
    private float colorMax1 = 255, colorMax2 = 255, colorMax3 = 255, colorMaxA = 255;

    private int rectMode = CORNER;
    private int ellipseMode = CENTER;

    private java.util.Stack<java.awt.geom.AffineTransform> matrixStack = new java.util.Stack<>();
    private java.awt.geom.Path2D.Float currentShape;

    public int[] pixels;
    private int imageMode = CORNER;

    private Font currentFont = new Font("SansSerif", Font.PLAIN, 12);
    private int textAlignX = LEFT;
    private int textAlignY = BASELINE;

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

    // --- Transformations ---
    public void pushMatrix() {
        matrixStack.push(g2.getTransform());
    }

    public void popMatrix() {
        if (!matrixStack.isEmpty()) {
            g2.setTransform(matrixStack.pop());
        }
    }

    public void translate(float x, float y) {
        g2.translate(x, y);
    }

    public void rotate(float angle) {
        g2.rotate(angle);
    }

    public void scale(float s) {
        g2.scale(s, s);
    }

    public void scale(float x, float y) {
        g2.scale(x, y);
    }

    public void resetMatrix() {
        g2.setTransform(new java.awt.geom.AffineTransform());
    }

    // --- Images ---
    public void imageMode(int mode) {
        this.imageMode = mode;
    }

    public JImage loadImage(String path) {
        try {
            // Check if it's a file or a resource? Processing usually looks in 'data' folder
            java.io.File file = new java.io.File(path);
            if (!file.exists()) {
                file = new java.io.File("data", path);
            }
            java.awt.image.BufferedImage bi = javax.imageio.ImageIO.read(file);
            return new JImage(bi);
        } catch (java.io.IOException e) {
            System.err.println("Could not load image: " + path);
            return null;
        }
    }

    public void image(JImage img, float x, float y) {
        if (img == null) return;
        image(img, x, y, img.width, img.height);
    }

    public void image(JImage img, float x, float y, float w, float h) {
        if (img == null) return;
        float ax = x, ay = y;
        if (imageMode == CENTER) {
            ax = x - w / 2f;
            ay = y - h / 2f;
        }
        g2.drawImage(img.img, (int)ax, (int)ay, (int)w, (int)h, null);
    }

    public void loadPixels() {
        if (pixels == null || pixels.length != width * height) {
            pixels = new int[width * height];
        }
        canvas.getRGB(0, 0, width, height, pixels, 0, width);
    }

    public void updatePixels() {
        if (pixels != null) {
            canvas.setRGB(0, 0, width, height, pixels, 0, width);
        }
    }

    // --- Typography ---
    public void textSize(float size) {
        currentFont = currentFont.deriveFont(size);
        g2.setFont(currentFont);
    }

    public void textAlign(int x) {
        textAlign(x, BASELINE);
    }

    public void textAlign(int x, int y) {
        this.textAlignX = x;
        this.textAlignY = y;
    }

    public void textFont(Font font) {
        this.currentFont = font;
        g2.setFont(currentFont);
    }

    public Font createFont(String name, float size) {
        return new Font(name, Font.PLAIN, (int)size);
    }

    public void text(String str, float x, float y) {
        if (doFill) {
            g2.setColor(fillColor);
            FontMetrics fm = g2.getFontMetrics();
            float tx = x;
            float ty = y;

            if (textAlignX == CENTER) tx -= fm.stringWidth(str) / 2f;
            else if (textAlignX == RIGHT) tx -= fm.stringWidth(str);

            if (textAlignY == TOP) ty += fm.getAscent();
            else if (textAlignY == CENTER) ty += fm.getAscent() / 2f;
            else if (textAlignY == BOTTOM) ty -= fm.getDescent();

            g2.drawString(str, tx, ty);
        }
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

    public void arc(float x, float y, float w, float h, float start, float stop) {
        float aw = w, ah = h;
        float ax = x, ay = y;
        if (ellipseMode == CENTER) {
            ax = x - w / 2f;
            ay = y - h / 2f;
        } else if (ellipseMode == RADIUS) {
            ax = x - w;
            ay = y - h;
            aw = w * 2;
            ah = h * 2;
        } else if (ellipseMode == CORNERS) {
            aw = w - x;
            ah = h - y;
        }

        // Java Arc2D angles are counter-clockwise, Processing's are clockwise.
        // Java Arc2D uses degrees, Processing uses radians.
        float startDeg = degrees(-start);
        float extentDeg = degrees(-(stop - start));

        java.awt.geom.Arc2D.Float arc = new java.awt.geom.Arc2D.Float(ax, ay, aw, ah, startDeg, extentDeg, java.awt.geom.Arc2D.OPEN);

        if (doFill) {
            g2.setColor(fillColor);
            g2.fill(arc);
        }
        if (doStroke) {
            g2.setColor(strokeColor);
            g2.draw(arc);
        }
    }

    public void triangle(float x1, float y1, float x2, float y2, float x3, float y3) {
        beginShape();
        vertex(x1, y1);
        vertex(x2, y2);
        vertex(x3, y3);
        endShape(CLOSE);
    }

    public void quad(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
        beginShape();
        vertex(x1, y1);
        vertex(x2, y2);
        vertex(x3, y3);
        vertex(x4, y4);
        endShape(CLOSE);
    }

    public void bezier(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
        java.awt.geom.CubicCurve2D.Float curve = new java.awt.geom.CubicCurve2D.Float(x1, y1, x2, y2, x3, y3, x4, y4);
        if (doStroke) {
            g2.setColor(strokeColor);
            g2.draw(curve);
        }
    }

    public void curve(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
        float cp1x = x2 + (x3 - x1) / 6f;
        float cp1y = y2 + (y3 - y1) / 6f;
        float cp2x = x3 - (x4 - x2) / 6f;
        float cp2y = y3 - (y4 - y2) / 6f;

        java.awt.geom.CubicCurve2D.Float curve = new java.awt.geom.CubicCurve2D.Float(x2, y2, cp1x, cp1y, cp2x, cp2y, x3, y3);
        if (doStroke) {
            g2.setColor(strokeColor);
            g2.draw(curve);
        }
    }

    public void beginShape() {
        currentShape = new java.awt.geom.Path2D.Float();
    }

    public void vertex(float x, float y) {
        if (currentShape != null) {
            if (currentShape.getCurrentPoint() == null) {
                currentShape.moveTo(x, y);
            } else {
                currentShape.lineTo(x, y);
            }
        }
    }

    public void endShape() {
        endShape(0);
    }

    public void endShape(int mode) {
        if (currentShape != null) {
            if (mode == CLOSE) {
                currentShape.closePath();
            }
            if (doFill) {
                g2.setColor(fillColor);
                g2.fill(currentShape);
            }
            if (doStroke) {
                g2.setColor(strokeColor);
                g2.draw(currentShape);
            }
            currentShape = null;
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

    public static float dist(float x1, float y1, float z1, float x2, float y2, float z2) {
        return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));
    }

    public static float lerp(float start, float stop, float amt) {
        return start + (stop - start) * amt;
    }

    public static float constrain(float n, float low, float high) {
        return Math.max(Math.min(n, high), low);
    }

    public float sin(float angle) { return (float) Math.sin(angle); }
    public float cos(float angle) { return (float) Math.cos(angle); }
    public float tan(float angle) { return (float) Math.tan(angle); }
    public float asin(float value) { return (float) Math.asin(value); }
    public float acos(float value) { return (float) Math.acos(value); }
    public float atan(float value) { return (float) Math.atan(value); }
    public float atan2(float y, float x) { return (float) Math.atan2(y, x); }

    public float degrees(float radians) { return radians * 180.0f / (float) Math.PI; }
    public float radians(float degrees) { return degrees * (float) Math.PI / 180.0f; }

    public int abs(int n) { return Math.abs(n); }
    public float abs(float n) { return Math.abs(n); }
    public float sqrt(float n) { return (float) Math.sqrt(n); }
    public float pow(float n, float e) { return (float) Math.pow(n, e); }
    public float exp(float n) { return (float) Math.exp(n); }
    public float log(float n) { return (float) Math.log(n); }
    public float sq(float n) { return n * n; }
    public float floor(float n) { return (float) Math.floor(n); }
    public float ceil(float n) { return (float) Math.ceil(n); }
    public int round(float n) { return Math.round(n); }

    public float min(float a, float b) { return Math.min(a, b); }
    public float min(float a, float b, float c) { return Math.min(a, Math.min(b, c)); }
    public int min(int a, int b) { return Math.min(a, b); }
    public int min(int a, int b, int c) { return Math.min(a, Math.min(b, c)); }

    public float max(float a, float b) { return Math.max(a, b); }
    public float max(float a, float b, float c) { return Math.max(a, Math.max(b, c)); }
    public int max(int a, int b) { return Math.max(a, b); }
    public int max(int a, int b, int c) { return Math.max(a, Math.max(b, c)); }

    public float norm(float value, float start, float stop) {
        return (value - start) / (stop - start);
    }

    public float mag(float a, float b) {
        return (float) Math.sqrt(a * a + b * b);
    }

    public float mag(float a, float b, float c) {
        return (float) Math.sqrt(a * a + b * b + c * c);
    }

    // Perlin Noise
    private static final int PERLIN_YWRAPB = 4;
    private static final int PERLIN_YWRAP = 1 << PERLIN_YWRAPB;
    private static final int PERLIN_ZWRAPB = 8;
    private static final int PERLIN_ZWRAP = 1 << PERLIN_ZWRAPB;
    private static final int PERLIN_SIZE = 4095;
    private float[] perlin;

    public float noise(float x) {
        return noise(x, 0, 0);
    }

    public float noise(float x, float y) {
        return noise(x, y, 0);
    }

    public float noise(float x, float y, float z) {
        if (perlin == null) {
            perlin = new float[PERLIN_SIZE + 1];
            for (int i = 0; i < PERLIN_SIZE + 1; i++) {
                perlin[i] = (float) Math.random();
            }
        }

        if (x < 0) x = -x;
        if (y < 0) y = -y;
        if (z < 0) z = -z;

        int xi = (int) x, yi = (int) y, zi = (int) z;
        float xf = x - xi, yf = y - yi, zf = z - zi;
        float rxf, ryf;

        float r = 0;
        float ampl = 0.5f;

        float n1, n2, n3;

        for (int i = 0; i < 4; i++) {
            int of = xi + (yi << PERLIN_YWRAPB) + (zi << PERLIN_ZWRAPB);

            rxf = noise_fsc(xf);
            ryf = noise_fsc(yf);

            n1 = perlin[of & PERLIN_SIZE];
            n1 += rxf * (perlin[(of + 1) & PERLIN_SIZE] - n1);
            n2 = perlin[(of + PERLIN_YWRAP) & PERLIN_SIZE];
            n2 += rxf * (perlin[(of + PERLIN_YWRAP + 1) & PERLIN_SIZE] - n2);
            n1 += ryf * (n2 - n1);

            of += PERLIN_ZWRAP;
            n2 = perlin[of & PERLIN_SIZE];
            n2 += rxf * (perlin[(of + 1) & PERLIN_SIZE] - n2);
            n3 = perlin[(of + PERLIN_YWRAP) & PERLIN_SIZE];
            n3 += rxf * (perlin[(of + PERLIN_YWRAP + 1) & PERLIN_SIZE] - n3);
            n2 += ryf * (n3 - n2);

            n1 += noise_fsc(zf) * (n2 - n1);

            r += n1 * ampl;
            ampl *= 0.5f;
            xi <<= 1; xf *= 2;
            yi <<= 1; yf *= 2;
            zi <<= 1; zf *= 2;

            if (xf >= 1.0f) { xi++; xf--; }
            if (yf >= 1.0f) { yi++; yf--; }
            if (zf >= 1.0f) { zi++; zf--; }
        }
        return r;
    }

    private float noise_fsc(float i) {
        return 0.5f * (1.0f - (float) Math.cos(i * (float) Math.PI));
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
            frame.setResizable(true); // Ensure the window is resizable
            frame.add(sketch);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            sketch.requestFocusInWindow();
            
            // Handle window resizing
            frame.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    sketch.width = sketch.getWidth();
                    sketch.height = sketch.getHeight();
                    sketch.initCanvas(); // Re-initialize canvas with new dimensions
                }
            });

            sketch.animationThread = new Thread(sketch);
            sketch.animationThread.start();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
