package jrocessing.app;

import jrocessing.preproc.Preprocessor;
import java.io.*;
import java.nio.file.*;

public class Runner {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: Runner <sketchName> <jdeFilePath>");
            System.exit(1);
        }

        String sketchName = args[0];
        String jdeFilePath = args[1];

        try {
            // Setup directories
            Path buildPath = Paths.get("build");
            Path sketchJavaPath = buildPath.resolve("sketches/src/jrocessing/sketches");
            Path sketchClassPath = buildPath.resolve("sketches/bin");
            Path coreClassPath = buildPath.resolve("classes");
            
            Files.createDirectories(sketchJavaPath);
            Files.createDirectories(sketchClassPath);
            Files.createDirectories(coreClassPath);

            // 1. Preprocess
            String code = Files.readString(Paths.get(jdeFilePath));
            String processedCode = Preprocessor.preprocess(sketchName, code);
            
            Path javaFilePath = sketchJavaPath.resolve(sketchName + ".java");
            Files.writeString(javaFilePath, processedCode);

            // 2. Compile
            System.out.println("Compiling...");
            String classpath = coreClassPath.toString();
            ProcessBuilder compilePb = new ProcessBuilder(
                "javac", "-d", sketchClassPath.toString(), "-cp", classpath, javaFilePath.toString()
            );
            compilePb.inheritIO();
            Process compileProcess = compilePb.start();
            int exitCode = compileProcess.waitFor();
            
            if (exitCode != 0) {
                System.err.println("Compilation failed with exit code " + exitCode);
                System.exit(exitCode);
            }

            // 3. Run
            System.out.println("Launching...");
            String runClasspath = sketchClassPath.toString() + File.pathSeparator + coreClassPath.toString();
            ProcessBuilder runPb = new ProcessBuilder(
                "java", "-cp", runClasspath, "jrocessing.core.JApplet", "jrocessing.sketches." + sketchName
            );
            // We want to capture the output of the sketch and print it to our own stdout/stderr
            // so the IDE can read it.
            runPb.inheritIO(); 
            Process runProcess = runPb.start();
            
            // Wait for the process to finish
            System.exit(runProcess.waitFor());

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
