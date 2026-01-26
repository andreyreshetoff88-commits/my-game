package com.mygame.engine.graphics;

import com.mygame.world.Chunk;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;

public class Renderer {
    private final BatchMesh chunkBatch = new BatchMesh();

    /**
     * Установка сцены (камера + проекция)
     */
    public void beginScene(Camera camera, int width, int height) {
        // очистка экрана цветом
        glClearColor(0.2f, 0.3f, 0.4f, 1.0f); // RGBA
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // очищаем буферы цвета и глубины
        glEnable(GL_DEPTH_TEST); // включаем тест глубины

        // настройка проекции
        glMatrixMode(GL_PROJECTION); // выбираем матрицу проекции
        glLoadIdentity();            // сбрасываем матрицу
        float near = 0.1f;
        float fov = 60f;
        float top = (float) Math.tan(Math.toRadians(fov / 2)) * near;
        float aspect = (float) width / height;
        float right = top * aspect;
        float far = 100f;
        glFrustum(-right, right, -top, top, near, far); // создаём перспективную проекцию

        // настройка модели (камера)
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        camera.applyView(); // перемещаем и поворачиваем камеру
    }

    /**
     * Загружаем меш чанка в GPU
     * Один раз для каждого чанка
     */
    public void uploadChunk(Chunk chunk) {
        if (chunk.getMesh() == null) return;
        chunkBatch.addChunk(chunk);
        chunk.markUploaded();
    }

    /**
     * Рисуем чанк каждый кадр
     */
    public void renderChunk() {
        chunkBatch.upload(); // если dirty — пересоберёт VBO
        chunkBatch.render(); // один glDrawArrays
    }

    public void cleanup() {
        chunkBatch.cleanup();
    }

    /**
     * Рисуем игрока (куб)
     */
    public void renderPlayer(float radius, float currentHeight, float yaw, Vector3f renderPos) {
        float halfWidth = radius / 2;
        float height = currentHeight - radius * 2;

        glPushMatrix(); // сохраняем текущую матрицу

        // переносим в позицию игрока
        glTranslatef(renderPos.x, renderPos.y, renderPos.z);
        glRotatef(-yaw, 0, 1, 0); // поворот по Y

        glBegin(GL_QUADS);

        // передняя грань
        glVertex3f(-halfWidth, 0, halfWidth);
        glVertex3f(halfWidth, 0, halfWidth);
        glVertex3f(halfWidth, height, halfWidth);
        glVertex3f(-halfWidth, height, halfWidth);

        // задняя
        glVertex3f(-halfWidth, 0, -halfWidth);
        glVertex3f(halfWidth, 0, -halfWidth);
        glVertex3f(halfWidth, height, -halfWidth);
        glVertex3f(-halfWidth, height, -halfWidth);

        // левая
        glVertex3f(-halfWidth, 0, -halfWidth);
        glVertex3f(-halfWidth, 0, halfWidth);
        glVertex3f(-halfWidth, height, halfWidth);
        glVertex3f(-halfWidth, height, -halfWidth);

        // правая
        glVertex3f(halfWidth, 0, -halfWidth);
        glVertex3f(halfWidth, 0, halfWidth);
        glVertex3f(halfWidth, height, halfWidth);
        glVertex3f(halfWidth, height, -halfWidth);

        // верхняя грань
        glVertex3f(-halfWidth, height, -halfWidth);
        glVertex3f(halfWidth, height, -halfWidth);
        glVertex3f(halfWidth, height, halfWidth);
        glVertex3f(-halfWidth, height, halfWidth);

        glEnd();

        glPopMatrix(); // возвращаем матрицу
    }
}
