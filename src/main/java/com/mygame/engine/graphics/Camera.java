package com.mygame.engine.graphics;

import com.mygame.engine.entity.Player;
import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f; // NEW
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.glMultMatrixf;
import static org.lwjgl.opengl.GL11.glTranslatef;

/**
 * Камера — это "глаза" игрока
 * НЕ содержит физики и коллизий
 */
public class Camera {
    @Setter @Getter
    private Vector3f position = new Vector3f();
    private final Vector3f front = new Vector3f();
    private final Vector3f up = new Vector3f(0, 1, 0);
    private final Vector3f right = new Vector3f();
    private float yaw;
    private float pitch;
    private static final float EYE_HEIGHT = 1.6f;

    // NEW: для frustum
    private final Matrix4f viewMatrix = new Matrix4f();
    private final Matrix4f projectionMatrix = new Matrix4f();
    private final Plane[] frustumPlanes = new Plane[6];

    public Camera() {
        for (int i = 0; i < 6; i++) frustumPlanes[i] = new Plane();
    }

    public void update(Player player) {
        yaw = player.getYaw();
        pitch = player.getPitch();
        updateVectors();
    }

    private void updateVectors() {
        front.x = (float) Math.cos(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));
        front.y = (float) Math.sin(Math.toRadians(pitch));
        front.z = (float) Math.sin(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));
        front.normalize();
        right.set(front).cross(up).normalize();
    }

    public void applyView() {
        Vector3f center = new Vector3f(position).add(front);
        gluLookAt(position, center, up);
    }

    /**
     * NEW: обновленный gluLookAt под frustum
     */
    private void gluLookAt(Vector3f eye, Vector3f center, Vector3f up) {
        // NEW: вычисляем фронт, боковое и вертикальное направление
        Vector3f f = new Vector3f(center).sub(eye).normalize();
        Vector3f s = new Vector3f(f).cross(up).normalize();
        Vector3f u = new Vector3f(s).cross(f);

        // NEW: создаем матрицу вида для OpenGL
        float[] m = new float[16];
        m[0] = s.x;  m[4] = s.y;  m[8]  = s.z;  m[12] = 0;
        m[1] = u.x;  m[5] = u.y;  m[9]  = u.z;  m[13] = 0;
        m[2] = -f.x; m[6] = -f.y; m[10] = -f.z; m[14] = 0;
        m[3] = 0;    m[7] = 0;    m[11] = 0;    m[15] = 1;

        glMultMatrixf(m);
        glTranslatef(-eye.x, -eye.y, -eye.z);

        // NEW: обновляем viewMatrix для frustum culling
        viewMatrix.identity().lookAt(eye, center, up);

        // NEW: сразу обновляем плоскости frustum
        Matrix4f clip = new Matrix4f();
        projectionMatrix.mul(viewMatrix, clip);
        float[] arr = new float[16];
        clip.get(arr);

        // LEFT
        frustumPlanes[0].set(arr[3] + arr[0], arr[7] + arr[4], arr[11] + arr[8], arr[15] + arr[12]);
        // RIGHT
        frustumPlanes[1].set(arr[3] - arr[0], arr[7] - arr[4], arr[11] - arr[8], arr[15] - arr[12]);
        // BOTTOM
        frustumPlanes[2].set(arr[3] + arr[1], arr[7] + arr[5], arr[11] + arr[9], arr[15] + arr[13]);
        // TOP
        frustumPlanes[3].set(arr[3] - arr[1], arr[7] - arr[5], arr[11] - arr[9], arr[15] - arr[13]);
        // NEAR
        frustumPlanes[4].set(arr[3] + arr[2], arr[7] + arr[6], arr[11] + arr[10], arr[15] + arr[14]);
        // FAR
        frustumPlanes[5].set(arr[3] - arr[2], arr[7] - arr[6], arr[11] - arr[10], arr[15] - arr[14]);

        for (Plane p : frustumPlanes) p.normalize();
    }

    public boolean isSphereInFrustum(Vector3f center, float radius) {
        for (Plane p : frustumPlanes) {
            if (p.distance(center) < -radius) return false;
        }
        return true;
    }

    public void setProjection(Matrix4f projection) {
        this.projectionMatrix.set(projection);
    }

    private static class Plane {
        private float a, b, c, d;

        void set(float a, float b, float c, float d) {
            this.a = a; this.b = b; this.c = c; this.d = d;
        }

        void normalize() {
            float length = (float) Math.sqrt(a * a + b * b + c * c);
            a /= length; b /= length; c /= length; d /= length;
        }

        float distance(Vector3f point) {
            return a * point.x + b * point.y + c * point.z + d;
        }
    }
}
