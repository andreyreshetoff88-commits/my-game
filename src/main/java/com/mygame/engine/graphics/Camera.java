package com.mygame.engine.graphics;

import com.mygame.engine.entity.Player;
import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Камера - определяет точку обзора игрока в 3D мире
 * В современном OpenGL камера реализуется через матрицы трансформации
 */
public class Camera {
    // Позиция камеры в мире (обычно следует за глазами игрока)
    @Setter
    @Getter
    private Vector3f position = new Vector3f();

    // Вектор направления взгляда камеры
    private final Vector3f front = new Vector3f();
    // Вектор "вверх" (определяет ориентацию камеры)
    private final Vector3f up = new Vector3f(0, 1, 0);
    // Вектор "вправо" (перпендикулярен front и up)
    private final Vector3f right = new Vector3f();

    // Углы поворота камеры (_yaw - поворот по горизонтали, pitch - по вертикали)
    private float yaw;
    private float pitch;

    // Матрица вида - преобразует мировые координаты в координаты камеры
    private final Matrix4f viewMatrix = new Matrix4f();

    /**
     * Обновляет состояние камеры на основе данных игрока
     * @param player объект игрока, от которого берутся данные поворота
     */
    public void update(Player player) {
        // Копируем углы поворота от игрока
        yaw = player.getYaw();
        pitch = player.getPitch();
        // Обновляем векторы направления камеры
        updateVectors();
    }

    /**
     * Вычисляет векторы направления камеры на основе углов yaw и pitch
     * Это стандартная формула сферических координат
     */
    private void updateVectors() {
        // Вычисляем вектор направления взгляда (front) из углов поворота
        // X компонент: cos(yaw) * cos(pitch)
        front.x = (float) Math.cos(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));
        // Y компонент: sin(pitch) - отвечает за вертикальный поворот
        front.y = (float) Math.sin(Math.toRadians(pitch));
        // Z компонент: sin(yaw) * cos(pitch)
        front.z = (float) Math.sin(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));
        // Нормализуем вектор (делаем его длиной 1)
        front.normalize();

        // Вычисляем вектор "вправо" как векторное произведение front и up
        // Векторное произведение дает вектор, перпендикулярный обоим исходным векторам
        right.set(front).cross(up).normalize();
        // front.cross(up, right) тоже самое, но более явно
    }

    /**
     * Возвращает матрицу вида камеры
     * Эта матрица преобразует мировые координаты в координаты относительно камеры
     * @return матрица вида 4x4
     */
    public Matrix4f getViewMatrix() {
        // Вычисляем точку, на которую смотрит камера
        // Это позиция камеры + направление взгляда
        Vector3f center = new Vector3f(position).add(front);

        // setLookAt создает матрицу вида по трем точкам:
        // 1. eye - позиция камеры
        // 2. center - точка, на которую смотрит камера
        // 3. up - вектор "вверх" для ориентации
        return viewMatrix.setLookAt(position, center, up);
    }

    /*
    // Старый код с фиксированным конвейером - ЗАКОМЕНТИРОВАН
    public void applyView() {
        Vector3f center = new Vector3f(position).add(front);
        gluLookAt(position, center, up);
    }

    private void gluLookAt(Vector3f eye, Vector3f center, Vector3f up) {
        Vector3f f = new Vector3f(center).sub(eye).normalize();
        Vector3f s = new Vector3f(f).cross(up).normalize();
        Vector3f u = new Vector3f(s).cross(f);

        float[] m = new float[16];
        m[0] = s.x;
        m[4] = s.y;
        m[8] = s.z;
        m[12] = 0;
        m[1] = u.x;
        m[5] = u.y;
        m[9] = u.z;
        m[13] = 0;
        m[2] = -f.x;
        m[6] = -f.y;
        m[10] = -f.z;
        m[14] = 0;
        m[3] = 0;
        m[7] = 0;
        m[11] = 0;
        m[15] = 1;

        glMultMatrixf(m);
        glTranslatef(-eye.x, -eye.y, -eye.z);
    }
    */
}
