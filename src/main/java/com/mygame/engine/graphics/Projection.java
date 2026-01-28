package com.mygame.engine.graphics;

import lombok.Getter;
import org.joml.Matrix4f;

/**
 * Класс для управления проекционной матрицей
 * Проекционная матрица определяет как 3D мир проецируется на 2D экран
 */
public class Projection {
    // Матрица проекции - преобразует 3D координаты в 2D экранные координаты
    @Getter
    private Matrix4f projectionMatrix = new Matrix4f();

    // Параметры проекции:
    private final float fov;    // угол обзора (field of view) в градусах
    private final float near;   // расстояние до ближней плоскости отсечения
    private final float far;    // расстояние до дальней плоскости отсечения

    // Размеры окна для вычисления соотношения сторон
    private int width;
    private int height;

    /**
     * Конструктор проекции
     *
     * @param fov  угол обзора в градусах (обычно 60-90)
     * @param near расстояние до ближней плоскости отсечения
     * @param far  расстояние до дальней плоскости отсечения
     */
    public Projection(float fov, float near, float far) {
        this.fov = fov;
        this.near = near;
        this.far = far;
    }

    /**
     * Обновляет проекционную матрицу при изменении размеров окна
     *
     * @param width  новая ширина окна
     * @param height новая высота окна
     */
    public void update(int width, int height) {
        // Проверяем, изменились ли размеры окна
        if (this.width != width || this.height != height) {
            // Сохраняем новые размеры
            this.width = width;
            this.height = height;

            // Создаем перспективную проекционную матрицу
            // setPerspective принимает:
            // 1. fovy - угол обзора в радианах (преобразуем градусы в радианы)
            // 2. aspect - соотношение сторон (ширина/высота)
            // 3. near - расстояние до ближней плоскости
            // 4. far - расстояние до дальней плоскости
            projectionMatrix.setPerspective(
                    (float) Math.toRadians(fov),  // преобразуем градусы в радианы
                    (float) width / height,       // соотношение сторон
                    near,                         // ближняя плоскость
                    far                           // дальняя плоскость
            );
        }
    }
}
