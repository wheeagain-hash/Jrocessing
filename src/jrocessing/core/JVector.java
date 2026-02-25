package jrocessing.core;

import java.util.Random;

public class JVector {
    public float x, y, z;
    private static Random random = new Random();

    public JVector() {
        this(0, 0, 0);
    }

    public JVector(float x, float y) {
        this(x, y, 0);
    }

    public JVector(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void set(JVector v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    public void set(float[] source) {
        if (source.length >= 2) {
            x = source[0];
            y = source[1];
        }
        if (source.length >= 3) {
            z = source[2];
        }
    }

    public JVector copy() {
        return new JVector(x, y, z);
    }

    @Deprecated
    public JVector get() {
        return copy();
    }

    public float[] get(float[] target) {
        if (target == null) {
            return new float[] { x, y, z };
        }
        if (target.length >= 2) {
            target[0] = x;
            target[1] = y;
        }
        if (target.length >= 3) {
            target[2] = z;
        }
        return target;
    }

    public void add(JVector v) {
        x += v.x;
        y += v.y;
        z += v.z;
    }

    public void add(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
    }

    public static JVector add(JVector v1, JVector v2) {
        return add(v1, v2, null);
    }

    public static JVector add(JVector v1, JVector v2, JVector target) {
        if (target == null) target = new JVector();
        target.set(v1.x + v2.x, v1.y + v2.y, v1.z + v2.z);
        return target;
    }

    public void sub(JVector v) {
        x -= v.x;
        y -= v.y;
        z -= v.z;
    }

    public void sub(float x, float y, float z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
    }

    public static JVector sub(JVector v1, JVector v2) {
        return sub(v1, v2, null);
    }

    public static JVector sub(JVector v1, JVector v2, JVector target) {
        if (target == null) target = new JVector();
        target.set(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
        return target;
    }

    public void mult(float n) {
        x *= n;
        y *= n;
        z *= n;
    }

    public static JVector mult(JVector v, float n) {
        return mult(v, n, null);
    }

    public static JVector mult(JVector v, float n, JVector target) {
        if (target == null) target = new JVector();
        target.set(v.x * n, v.y * n, v.z * n);
        return target;
    }

    public void div(float n) {
        x /= n;
        y /= n;
        z /= n;
    }

    public static JVector div(JVector v, float n) {
        return div(v, n, null);
    }

    public static JVector div(JVector v, float n, JVector target) {
        if (target == null) target = new JVector();
        target.set(v.x / n, v.y / n, v.z / n);
        return target;
    }

    public float mag() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public float magSq() {
        return x * x + y * y + z * z;
    }

    public float dot(JVector v) {
        return x * v.x + y * v.y + z * v.z;
    }

    public float dot(float x, float y, float z) {
        return this.x * x + this.y * y + this.z * z;
    }

    public static float dot(JVector v1, JVector v2) {
        return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
    }

    public JVector cross(JVector v) {
        return cross(v, null);
    }

    public JVector cross(JVector v, JVector target) {
        if (target == null) target = new JVector();
        float x_ = y * v.z - v.y * z;
        float y_ = z * v.x - v.z * x;
        float z_ = x * v.y - v.x * y;
        target.set(x_, y_, z_);
        return target;
    }

    public static JVector cross(JVector v1, JVector v2, JVector target) {
        if (target == null) target = new JVector();
        float x_ = v1.y * v2.z - v2.y * v1.z;
        float y_ = v1.z * v2.x - v2.z * v1.x;
        float z_ = v1.x * v2.y - v2.x * v1.y;
        target.set(x_, y_, z_);
        return target;
    }

    public float dist(JVector v) {
        return dist(this, v);
    }

    public static float dist(JVector v1, JVector v2) {
        float dx = v1.x - v2.x;
        float dy = v1.y - v2.y;
        float dz = v1.z - v2.z;
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public void normalize() {
        float m = mag();
        if (m != 0 && m != 1) {
            div(m);
        }
    }

    public JVector normalize(JVector target) {
        if (target == null) target = new JVector();
        float m = mag();
        if (m > 0) {
            target.set(x / m, y / m, z / m);
        } else {
            target.set(x, y, z);
        }
        return target;
    }

    public void limit(float max) {
        if (magSq() > max * max) {
            normalize();
            mult(max);
        }
    }

    public void setMag(float len) {
        normalize();
        mult(len);
    }

    public float heading() {
        return (float) Math.atan2(y, x);
    }

    public void rotate(float theta) {
        float temp = x;
        x = x * (float) Math.cos(theta) - y * (float) Math.sin(theta);
        y = temp * (float) Math.sin(theta) + y * (float) Math.cos(theta);
    }

    public static JVector lerp(JVector v1, JVector v2, float amt) {
        JVector v = v1.copy();
        v.lerp(v2, amt);
        return v;
    }

    public void lerp(JVector v, float amt) {
        x = x + (v.x - x) * amt;
        y = y + (v.y - y) * amt;
        z = z + (v.z - z) * amt;
    }

    public void lerp(float x, float y, float z, float amt) {
        this.x = this.x + (x - this.x) * amt;
        this.y = this.y + (y - this.y) * amt;
        this.z = this.z + (z - this.z) * amt;
    }

    public static float angleBetween(JVector v1, JVector v2) {
        double dot = v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
        double v1mag = Math.sqrt(v1.x * v1.x + v1.y * v1.y + v1.z * v1.z);
        double v2mag = Math.sqrt(v2.x * v2.x + v2.y * v2.y + v2.z * v2.z);
        double amt = dot / (v1mag * v2mag);
        if (amt <= -1) {
            return (float) Math.PI;
        } else if (amt >= 1) {
            return 0;
        }
        return (float) Math.acos(amt);
    }

    public static JVector random2D() {
        return fromAngle(random.nextFloat() * (float) Math.PI * 2);
    }

    public static JVector fromAngle(float angle) {
        return fromAngle(angle, null);
    }

    public static JVector fromAngle(float angle, JVector target) {
        if (target == null) target = new JVector();
        target.set((float) Math.cos(angle), (float) Math.sin(angle), 0);
        return target;
    }

    public static JVector random3D() {
        return random3D(null);
    }

    public static JVector random3D(JVector target) {
        float angle = random.nextFloat() * (float) Math.PI * 2;
        float vz = random.nextFloat() * 2 - 1;
        float vzBase = (float) Math.sqrt(1 - vz * vz);
        float vx = vzBase * (float) Math.cos(angle);
        float vy = vzBase * (float) Math.sin(angle);
        if (target == null) target = new JVector();
        target.set(vx, vy, vz);
        return target;
    }

    @Override
    public String toString() {
        return "[ " + x + ", " + y + ", " + z + " ]";
    }

    public float[] array() {
        return new float[] { x, y, z };
    }
}
