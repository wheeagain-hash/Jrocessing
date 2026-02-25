# Jrocessing TODO (Future Features)

Items to be implemented later to achieve full parity with Jrocessing or beyond:

- **Advanced Pre-processor Features:**
  - Support for `#color` hex literals (e.g., `int c = #FF0000;`).
  - Automatic conversion of all classes into inner classes of the main sketch.
  - Automatic `import` additions.
  - Better handling of `int()` and other Jrocessing-specific type conversions.
- **3D Rendering:**
  - P3D renderer (OpenGL-based, using JOGL or LWJGL).
  - Shaders support (`JShader`).
  - Lights and camera.
- **Advanced Shapes:**
  - `JShape` for retained mode graphics.
  - `beginShape()`, `endShape()`, and vertex handling.
  - Curve and Bezier implementations.
- **Typography:**
  - `JFont` and better text rendering.
- **Images:**
  - `JImage`, `loadImage()`, and pixel manipulation (`pixels[]`).
- **Additional Libraries:**
  - `Table`, `XML`, `JSONObject/JSONArray` support.
  - Networking and Serial communication.
- **IDE Enhancements:**
  - Linting (real-time error underlining).
  - Code auto-formatting.
  - Better project management (multiple tabs).
  - Export as Application.
