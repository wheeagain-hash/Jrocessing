package jrocessing.app;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.util.regex.*;

public class JrocessingIDE extends JFrame {
    private JTextPane editor;
    private JTextArea lineNumbers;
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

        lineNumbers = new JTextArea("1");
        lineNumbers.setBackground(new Color(220, 220, 220));
        lineNumbers.setEditable(false);
        lineNumbers.setFont(new Font("Monospaced", Font.PLAIN, 14));
        lineNumbers.setMargin(new Insets(2, 5, 2, 5));

        editor.getStyledDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                int lineCount = editor.getText().split("\n", -1).length;
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i <= lineCount; i++) {
                    sb.append(i).append("\n");
                }
                SwingUtilities.invokeLater(() -> lineNumbers.setText(sb.toString()));
            }
            public void insertUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void changedUpdate(DocumentEvent e) { update(); }
        });

        // Add a simple document listener for syntax highlighting
        editor.getStyledDocument().addDocumentListener(new SyntaxHighlighter(editor));

        JScrollPane scrollPane = new JScrollPane(editor);
        scrollPane.setRowHeaderView(lineNumbers);
        add(scrollPane, BorderLayout.CENTER);

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

    private void highlightErrorLine(int line) {
        SwingUtilities.invokeLater(() -> {
            try {
                Element root = editor.getDocument().getDefaultRootElement();
                if (line > 0 && line <= root.getElementCount()) {
                    Element lineElem = root.getElement(line - 1);
                    editor.setCaretPosition(lineElem.getStartOffset());
                    editor.requestFocusInWindow();
                    editor.select(lineElem.getStartOffset(), lineElem.getEndOffset());
                }
            } catch (Exception e) {}
        });
    }

    private void runSketch() {
        if (currentProcess != null && currentProcess.isAlive()) {
            stopSketch();
        }

        String code = editor.getText();

        // Basic linting
        int braceBalance = 0;
        for (char c : code.toCharArray()) {
            if (c == '{') braceBalance++;
            if (c == '}') braceBalance--;
        }
        if (braceBalance > 0) console.setText("Warning: Mismatched braces (too many { )\n");
        else if (braceBalance < 0) console.setText("Warning: Mismatched braces (too many } )\n");
        else console.setText("");

        console.setText("Running sketch...\n");
        runButton.setEnabled(false);
        stopButton.setEnabled(true);
        
        new Thread(() -> {
            try {
                File tempDir = new File("temp");
                tempDir.mkdirs();
                File pdeFile = new File(tempDir, currentSketchName + ".pde");
                Files.writeString(pdeFile.toPath(), code);
                
                ProcessBuilder pb = new ProcessBuilder(
                    "java", "-cp", "build/classes", "jrocessing.app.Runner", currentSketchName, pdeFile.getAbsolutePath()
                );
                pb.redirectErrorStream(true);
                currentProcess = pb.start();
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(currentProcess.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    String finalLine = line;
                    SwingUtilities.invokeLater(() -> {
                        console.append(finalLine + "\n");
                        // Scroll to bottom
                        console.setCaretPosition(console.getDocument().getLength());
                    });

                    // Parse error: "Sketch.java:10: error: ..."
                    if (finalLine.contains(currentSketchName + ".java:")) {
                        Pattern errorPattern = Pattern.compile(currentSketchName + "\\.java:(\\d+):");
                        Matcher matcher = errorPattern.matcher(finalLine);
                        if (matcher.find()) {
                            int javaLine = Integer.parseInt(matcher.group(1));
                            int pdeLine = javaLine - 6; // Offset from Preprocessor (approx)
                            highlightErrorLine(pdeLine);
                        }
                    }
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
