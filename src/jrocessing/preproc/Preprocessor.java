package jrocessing.preproc;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class Preprocessor {
    public static String preprocess(String sketchName, String code) {
        StringBuilder sb = new StringBuilder();
        sb.append("package jrocessing.sketches;\n\n");
        sb.append("import jrocessing.core.*;\n");
        sb.append("import java.awt.*;\n");
        sb.append("import java.util.*;\n\n");
        sb.append("public class ").append(sketchName).append(" extends JApplet {\n");
        
        // Improved preprocessor: make sure top-level methods are public
        String[] lines = code.split("\n");
        // Simplified: any void method starting at the beginning of a line (with optional whitespace)
        Pattern p = Pattern.compile("^\\s*void\\s+([a-zA-Z0-9_]+)\\s*\\(");
        for (String line : lines) {
            Matcher m = p.matcher(line);
            if (m.find()) {
                line = line.replaceFirst("void", "public void");
            }
            sb.append("    ").append(line).append("\n");
        }
        
        sb.append("}\n");
        return sb.toString();
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.err.println("Usage: Preprocessor <sketchName> <inputFile> <outputFile>");
            System.exit(1);
        }
        String sketchName = args[0];
        String inputFile = args[1];
        String outputFile = args[2];
        
        String code = Files.readString(Paths.get(inputFile));
        String processedCode = preprocess(sketchName, code);
        
        Files.createDirectories(Paths.get(outputFile).getParent());
        Files.writeString(Paths.get(outputFile), processedCode);
    }
}
