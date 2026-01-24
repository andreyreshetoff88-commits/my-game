package com.mygame.engine.graphics;

import com.mygame.engine.entity.Player;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.glMultMatrixf;
import static org.lwjgl.opengl.GL11.glTranslatef;

/**
 * Камера — это "глаза" игрока
 * НЕ содержит физики и коллизий
 */

public class Camera {
    // Итоговая позиция камеры (глаза)
    @Setter
    @Getter
    private Vector3f position = new Vector3f();
    // Направления
    private final Vector3f front = new Vector3f();
    private final Vector3f up = new Vector3f(0, 1, 0);
    private final Vector3f right = new Vector3f();
    // Углы
    private float yaw;
    private float pitch;
    // Высота глаз относительно тела игрока
    private static final float EYE_HEIGHT = 1.6f;

    /**
     * Обновление камеры из игрока
     */
    public void update(Player player) {
        // Углы берем у игрока
        yaw = player.getYaw();
        pitch = player.getPitch();

        updateVectors();
    }

    /**
     * Обновляем front/right по yaw/pitch
     */
    private void updateVectors() {
        front.x = (float) Math.cos(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));
        front.y = (float) Math.sin(Math.toRadians(pitch));
        front.z = (float) Math.sin(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));
        front.normalize();

        right.set(front).cross(up).normalize();
    }

    /**
     * Применяем камеру в OpenGL
     */
    public void applyView() {
        Vector3f center = new Vector3f(position).add(front);
        gluLookAt(position, center, up);
    }

    /**
     * Реализация gluLookAt (без GLU)
     */
    private void gluLookAt(Vector3f eye, Vector3f center, Vector3f up) {
        Vector3f f = new Vector3f(center).sub(eye).normalize();
        Vector3f s = new Vector3f(f).cross(up).normalize();
        Vector3f u = new Vector3f(s).cross(f);

        float[] m = new float[16];

        m[0] = s.x;  m[4] = s.y;  m[8]  = s.z;  m[12] = 0;
        m[1] = u.x;  m[5] = u.y;  m[9]  = u.z;  m[13] = 0;
        m[2] = -f.x; m[6] = -f.y; m[10] = -f.z; m[14] = 0;
        m[3] = 0;    m[7] = 0;    m[11] = 0;    m[15] = 1;

        glMultMatrixf(m);
        glTranslatef(-eye.x, -eye.y, -eye.z);
    }
}
