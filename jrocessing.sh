#!/bin/bash
set -e

# Setup directories
mkdir -p build/classes
mkdir -p build/sketches/src
mkdir -p build/sketches/bin

# Compile everything
echo "Compiling Jrocessing..."
javac -d build/classes -sourcepath src src/jrocessing/app/JrocessingIDE.java src/jrocessing/core/JApplet.java src/jrocessing/core/JVector.java src/jrocessing/core/JImage.java src/jrocessing/preproc/Preprocessor.java src/jrocessing/app/Runner.java

# Run IDE if requested
if [ "$1" == "run" ]; then
    echo "Launching Jrocessing IDE..."
    java -cp build/classes jrocessing.app.JrocessingIDE
fi
