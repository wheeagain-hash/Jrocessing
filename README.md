# Jrocessing

A from-scratch, minimal Java implementation of the Processing ecosystem.

## Features
- **JApplet**: Core class for sketches, providing the lifecycle (`setup`, `draw`) and drawing API.
- **Jrocessing IDE**: A Swing-based development environment with syntax highlighting and integrated console.
- **Preprocessor**: Automatically wraps `.jde` sketch files into valid Java.
- **Runner**: Launches sketches in a separate JVM process for stability.

## How to Run
1. Ensure you have Java 21+ installed.
2. Run the build script:
   ```bash
   ./jrocessing.sh
   ```
3. To launch the IDE directly:
   ```bash
   ./jrocessing.sh run
   ```

## Using your own GitHub
To push this code to your new repository, run these commands from this directory:
```bash
# If you haven't initialized git yet
git init
git add .
git commit -m "Initial Jrocessing commit"

# Replace with your repository URL
git remote add origin <YOUR_GITHUB_REPO_URL>
git branch -M main
git push -u origin main
```
