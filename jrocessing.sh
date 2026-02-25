#!/bin/bash
set -e

# Setup directories
mkdir -p build/classes
mkdir -p build/sketches/src
mkdir -p build/sketches/bin

# Compile Core Library
echo "Compiling Jrocessing Core..."
javac -d build/classes src/jrocessing/core/JApplet.java

# Compile Preprocessor
echo "Compiling Jrocessing Preprocessor..."
javac -d build/classes -cp build/classes src/jrocessing/preproc/Preprocessor.java

# Compile Runner
echo "Compiling Jrocessing Runner..."
javac -d build/classes -cp build/classes src/jrocessing/app/Runner.java

# Compile IDE
echo "Compiling Jrocessing IDE..."
javac -d build/classes -cp build/classes src/jrocessing/app/JrocessingIDE.java

# Run IDE if requested
if [ "$1" == "run" ]; then
    echo "Launching Jrocessing IDE..."
    java -cp build/classes jrocessing.app.JrocessingIDE
fi
