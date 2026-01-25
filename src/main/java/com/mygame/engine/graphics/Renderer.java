package com.mygame.engine.graphics;

import com.mygame.world.Chunk;
import com.mygame.world.ChunkMesh;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

public class Renderer {
    // Константа: шаг в байтах между вершинами в VBO
    // 3 float позиции + 3 float цвета = 6 float
    private static final int STRIDE = 6 * Float.BYTES;
    // Храним VBO каждого чанка, чтобы не пересоздавать каждый кадр
    private final Map<Chunk, Integer> vbos = new HashMap<>();

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
        if (chunk.isUploaded()) return;
        ChunkMesh mesh = chunk.getMesh(); // получаем float[] меша
        if (mesh == null) return; // если нет вершин — выходим

        // создаём буфер FloatBuffer из массива вершин
        FloatBuffer buffer = BufferUtils.createFloatBuffer(mesh.getVertices().length);
        buffer.put(mesh.getVertices()).flip(); // копируем данные и ставим позицию в 0

        int vboId = glGenBuffers();           // создаём VBO
        glBindBuffer(GL_ARRAY_BUFFER, vboId); // активируем VBO
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW); // отправляем данные в GPU

        vbos.put(chunk, vboId); // сохраняем VBO для чанка
        chunk.markUploaded();   // помечаем, что чанк загружен
        glBindBuffer(GL_ARRAY_BUFFER, 0); // отвязываем VBO
    }

    /**
     * Рисуем чанк каждый кадр
     */
    public void renderChunk(Chunk chunk) {
        ChunkMesh mesh = chunk.getMesh();
        if (mesh == null) return;

        // получаем VBO для этого чанка
        Integer vbo = vbos.get(chunk);
        if (vbo == null) return; // если VBO ещё не создан — выходим

        glBindBuffer(GL_ARRAY_BUFFER, vbo); // активируем VBO

        // включаем массивы вершин
        glEnableClientState(GL_VERTEX_ARRAY);
        glVertexPointer(3, GL_FLOAT, STRIDE, 0); // 3 float для позиции, шаг STRIDE

        glEnableClientState(GL_COLOR_ARRAY);
        glColorPointer(3, GL_FLOAT, STRIDE, 3 * Float.BYTES); // 3 float для цвета, смещение после позиции

        glDrawArrays(GL_TRIANGLES, 0, mesh.getVertexCount()); // рисуем все вершины

        // отключаем массивы после отрисовки
        glDisableClientState(GL_COLOR_ARRAY);
        glDisableClientState(GL_VERTEX_ARRAY);

        glBindBuffer(GL_ARRAY_BUFFER, 0); // отвязываем VBO
    }

    public void unloadChunk(Chunk chunk) {
        Integer vbo = vbos.remove(chunk); // удаляем из карты
        if (vbo != null) {
            glDeleteBuffers(vbo);
        }
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
