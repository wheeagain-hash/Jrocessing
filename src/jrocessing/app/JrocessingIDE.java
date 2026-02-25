package jrocessing.app;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.util.regex.*;

public class JrocessingIDE extends JFrame {
    private JTextPane editor;
    private JTextArea console;
    private JButton runButton;
    private JButton stopButton;
    private Process currentProcess;
    private String currentSketchName = "Sketch";

    public JrocessingIDE() {
        setTitle("Jrocessing IDE");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        runButton = new JButton("Run");
        runButton.addActionListener(e -> runSketch());
        toolbar.add(runButton);

        stopButton = new JButton("Stop");
        stopButton.setEnabled(false);
        stopButton.addActionListener(e -> stopSketch());
        toolbar.add(stopButton);

        add(toolbar, BorderLayout.NORTH);

        // Editor
        editor = new JTextPane();
        editor.setFont(new Font("Monospaced", Font.PLAIN, 14));
        // Add a simple document listener for syntax highlighting
        editor.getStyledDocument().addDocumentListener(new SyntaxHighlighter(editor));
        add(new JScrollPane(editor), BorderLayout.CENTER);

        // Console
        console = new JTextArea(10, 80);
        console.setEditable(false);
        console.setBackground(new Color(30, 30, 30));
        console.setForeground(Color.WHITE);
        console.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(new JScrollPane(console), BorderLayout.SOUTH);

        setLocationRelativeTo(null);
    }

    private void stopSketch() {
        if (currentProcess != null && currentProcess.isAlive()) {
            currentProcess.destroy();
            console.append("\nSketch stopped.\n");
        }
        stopButton.setEnabled(false);
        runButton.setEnabled(true);
    }

    private void runSketch() {
        if (currentProcess != null && currentProcess.isAlive()) {
            stopSketch();
        }

        String code = editor.getText();
        console.setText("Running sketch...\n");
        runButton.setEnabled(false);
        stopButton.setEnabled(true);
        
        new Thread(() -> {
            try {
                File tempDir = new File("temp");
                tempDir.mkdirs();
                File jdeFile = new File(tempDir, currentSketchName + ".jde");
                Files.writeString(jdeFile.toPath(), code);
                
                ProcessBuilder pb = new ProcessBuilder(
                    "java", "-cp", "build/classes", "jrocessing.app.Runner", currentSketchName, jdeFile.getAbsolutePath()
                );
                pb.redirectErrorStream(true);
                currentProcess = pb.start();
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(currentProcess.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    String finalLine = line;
                    SwingUtilities.invokeLater(() -> console.append(finalLine + "\n"));
                }
                
                currentProcess.waitFor();
                SwingUtilities.invokeLater(() -> {
                    stopButton.setEnabled(false);
                    runButton.setEnabled(true);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    console.append("Error: " + ex.getMessage() + "\n");
                    stopButton.setEnabled(false);
                    runButton.setEnabled(true);
                });
            }
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new JrocessingIDE().setVisible(true);
        });
    }

    // Inner class for basic syntax highlighting
    private static class SyntaxHighlighter implements javax.swing.event.DocumentListener {
        private JTextPane editor;
        private Style keywordStyle;
        private Style defaultStyle;
        private Style commentStyle;

        private static final Pattern KEYWORD_PATTERN;
        private static final Pattern COMMENT_PATTERN = Pattern.compile("//.*");

        static {
            String[] keywords = {"void", "setup", "draw", "int", "float", "if", "else", "for", "while", "size", "background", "fill", "stroke", "rect", "ellipse", "line", "point"};
            StringBuilder sb = new StringBuilder("\\b(");
            for (int i = 0; i < keywords.length; i++) {
                sb.append(keywords[i]);
                if (i < keywords.length - 1) sb.append("|");
            }
            sb.append(")\\b");
            KEYWORD_PATTERN = Pattern.compile(sb.toString());
        }

        public SyntaxHighlighter(JTextPane editor) {
            this.editor = editor;
            defaultStyle = editor.addStyle("default", null);
            StyleConstants.setForeground(defaultStyle, Color.BLACK);

            keywordStyle = editor.addStyle("keyword", null);
            StyleConstants.setForeground(keywordStyle, new Color(215, 58, 73)); // Reddish
            StyleConstants.setBold(keywordStyle, true);

            commentStyle = editor.addStyle("comment", null);
            StyleConstants.setForeground(commentStyle, new Color(106, 115, 125)); // Gray
        }

        private void highlight() {
            SwingUtilities.invokeLater(() -> {
                StyledDocument doc = editor.getStyledDocument();
                String text = "";
                try {
                    text = doc.getText(0, doc.getLength());
                } catch (BadLocationException e) {
                    return;
                }

                doc.setCharacterAttributes(0, text.length(), defaultStyle, true);

                // Keywords
                Matcher m = KEYWORD_PATTERN.matcher(text);
                while (m.find()) {
                    doc.setCharacterAttributes(m.start(), m.end() - m.start(), keywordStyle, true);
                }

                // Comments
                Matcher cm = COMMENT_PATTERN.matcher(text);
                while (cm.find()) {
                    doc.setCharacterAttributes(cm.start(), cm.end() - cm.start(), commentStyle, true);
                }
            });
        }

        public void insertUpdate(javax.swing.event.DocumentEvent e) { highlight(); }
        public void removeUpdate(javax.swing.event.DocumentEvent e) { highlight(); }
        public void changedUpdate(javax.swing.event.DocumentEvent e) {}
    }
}
