package com.mygame.engine.graphics;

import com.mygame.world.Chunk;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;

/**
 * NEW: Класс для batch rendering чанков.
 * Здесь мы объединяем вершины нескольких чанков в один VBO для оптимизации.
 */
public class BatchMesh {
    private final List<Chunk> chunks = new ArrayList<>(); // чанки для batch
    private int vboId = -1;        // ID VBO
    private int vertexCount = 0;   // количество вершин в batch
    private boolean dirty = true;  // нужно ли пересоздавать буфер

    /**
     * Добавляем чанк в batch
     */
    public void addChunk(Chunk chunk) {
        chunks.add(chunk);
        dirty = true; // нужно обновить VBO
    }

    /**
     * Удаляем чанк из batch
     */
    public void removeChunk(Chunk chunk) {
        chunks.remove(chunk);
        dirty = true;
    }

    /**
     * Создаем или обновляем VBO
     */
    public void upload() {
        if (!dirty) return;

        // считаем общее количество вершин
        vertexCount = 0;
        for (Chunk c : chunks) {
            if (c.getMesh() != null)
                vertexCount += c.getMesh().getVertexCount();
        }

        // создаем FloatBuffer для всех вершин
        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertexCount * 6); // 6 float на вершину
        for (Chunk c : chunks) {
            if (c.getMesh() != null)
                buffer.put(c.getMesh().getVertices(), 0, c.getMesh().getVertexCount() * 6);
        }
        buffer.flip();

        if (vboId == -1) {
            vboId = glGenBuffers();
        }

        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        dirty = false; // VBO обновлен
    }

    /**
     * Рисуем batch
     */
    public void render() {
        if (vertexCount == 0) return;

        glBindBuffer(GL_ARRAY_BUFFER, vboId);

        glEnableClientState(GL_VERTEX_ARRAY);
        glVertexPointer(3, GL_FLOAT, 6 * Float.BYTES, 0);

        glEnableClientState(GL_COLOR_ARRAY);
        glColorPointer(3, GL_FLOAT, 6 * Float.BYTES, 3 * Float.BYTES);

        glDrawArrays(GL_TRIANGLES, 0, vertexCount);

        glDisableClientState(GL_COLOR_ARRAY);
        glDisableClientState(GL_VERTEX_ARRAY);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    /**
     * Удаляем буфер
     */
    public void cleanup() {
        if (vboId != -1) {
            glDeleteBuffers(vboId);
            vboId = -1;
        }
    }
}
