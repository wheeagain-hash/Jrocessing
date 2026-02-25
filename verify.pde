float t = 0;
ArrayList<Ball> balls = new ArrayList<Ball>();

void setup() {
  size(600, 400);
  for (int i = 0; i < 10; i++) {
    balls.add(new Ball());
  }
}

void draw() {
  background(200, 220, 255);

  // Perlin noise landscape
  stroke(0, 100, 0);
  fill(100, 200, 100);
  beginShape();
  vertex(0, height);
  for (int x = 0; x <= width; x += 10) {
    float y = map(noise(x * 0.01f, t), 0, 1, height/2, height);
    vertex(x, y);
  }
  vertex(width, height);
  endShape(CLOSE);

  // Moving objects
  for (Ball b : balls) {
    b.update();
    b.display();
  }

  // Rotating text
  pushMatrix();
  translate(width/2, 100);
  rotate(t);
  textAlign(CENTER, CENTER);
  textSize(32);
  fill(0);
  text("Jrocessing", 0, 0);
  popMatrix();

  t += 0.05f;
}

class Ball {
  JVector pos;
  JVector vel;
  float r;

  Ball() {
    pos = new JVector(random(width), random(height/2));
    vel = JVector.random2D();
    vel.mult(random(2, 5));
    r = random(10, 20);
  }

  void update() {
    pos.add(vel);
    if (pos.x < 0 || pos.x > width) vel.x *= -1;
    if (pos.y < 0 || pos.y > height) vel.y *= -1;
  }

  void display() {
    fill(255, 100, 100);
    noStroke();
    ellipse(pos.x, pos.y, r*2, r*2);
  }
}
