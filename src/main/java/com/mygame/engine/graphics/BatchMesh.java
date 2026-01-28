package com.mygame.engine.graphics;

import com.mygame.world.Chunk;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;

/**
 * BatchMesh - класс для batch рендеринга множества чанков
 * Объединяет вершины нескольких чанков в один VBO для оптимизации рендеринга
 * <p>
 * Современный подход: вместо множества маленьких draw call'ов делаем один большой
 */
public class BatchMesh {
    // Список чанков для batch рендеринга
    private final List<Chunk> chunks = new ArrayList<>();
    // ID Vertex Buffer Object - буфера вершин в видеопамяти
    private int vboId = -1;
    // Общее количество вершин во всех чанках batch'а
    private int vertexCount = 0;
    // Флаг, указывающий что нужно пересоздать буфер (данные изменились)
    private boolean dirty = true;

    /**
     * Добавляет чанк в batch для рендеринга
     *
     * @param chunk чанк для добавления
     */
    public void addChunk(Chunk chunk) {
        // Добавляем чанк в список
        chunks.add(chunk);
        // Помечаем batch как "грязный" - нужно обновить VBO
        dirty = true;
    }

    /**
     * Удаляет чанк из batch'а
     *
     * @param chunk чанк для удаления
     */
    public void removeChunk(Chunk chunk) {
        // Удаляем чанк из списка
        chunks.remove(chunk);
        // Помечаем batch как "грязный" - нужно обновить VBO
        dirty = true;
    }

    /**
     * Создает или обновляет VBO с вершинами всех чанков
     * Вызывается перед рендерингом, если данные изменились
     */
    public void upload() {
        // Если данные не изменились, ничего не делаем
        if (!dirty) return;

        // ====== Подсчет общего количества вершин ======
        // Проходим по всем чанкам и суммируем количество вершин
        vertexCount = 0;
        for (Chunk c : chunks) {
            // Проверяем, что у чанка есть меш и он не null
            if (c.getMesh() != null)
                // Добавляем количество вершин этого чанка к общему счетчику
                vertexCount += c.getMesh().getVertexCount();
        }

        // ====== Создание буфера вершин ======
        // Создаем FloatBuffer для хранения всех вершин всех чанков
        // Размер буфера: vertexCount * 6 (6 float'ов на вершину: x,y,z,r,g,b)
        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertexCount * 6);

        // Копируем вершины всех чанков в общий буфер
        for (Chunk c : chunks) {
            // Проверяем, что у чанка есть меш
            if (c.getMesh() != null) {
                // Копируем вершины чанка в общий буфер
                // getVertices() - массив вершин чанка
                // 0 - начальный индекс в массиве чанка
                // c.getMesh().getVertexCount() * 6 - количество элементов для копирования
                buffer.put(c.getMesh().getVertices(), 0, c.getMesh().getVertexCount() * 6);
            }
        }
        // Переводим буфер в режим чтения (подготавливаем к отправке в GPU)
        buffer.flip();

        // ====== Создание или обновление VBO ======
        // Если VBO еще не создан (-1 означает "не создан")
        if (vboId == -1) {
            // glGenBuffers создает новый буфер и возвращает его ID
            vboId = glGenBuffers();
        }

        // Привязываем буфер к цели GL_ARRAY_BUFFER
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        // Отправляем данные из буфера в видеопамять
        // GL_ARRAY_BUFFER - тип буфера (буфер вершин)
        // buffer - источник данных
        // GL_STATIC_DRAW - данные редко изменяются,主要用于 рисования
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        // Отвязываем буфер
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        // Помечаем batch как "чистый" - данные обновлены
        dirty = false;
    }

    /**
     * Рендерит все чанки в batch'е одним вызовом
     * Это основное преимущество batch рендеринга - минимизация draw call'ов
     */
    public void render() {
        // Если нет вершин для рендеринга, выходим
        if (vertexCount == 0) return;

        // Привязываем VBO для использования
        glBindBuffer(GL_ARRAY_BUFFER, vboId);

        // ====== Настройка атрибутов вершин ======
        // Включаем использование массива вершинных атрибутов
        glEnableClientState(GL_VERTEX_ARRAY);
        // Указываем, как читать позиции вершин из буфера
        // 3 - количество компонентов (x,y,z)
        // GL_FLOAT - тип данных
        // 6 * Float.BYTES - stride (расстояние между вершинами)
        // 0 - offset (смещение до начала данных позиции)
        glVertexPointer(3, GL_FLOAT, 6 * Float.BYTES, 0);

        // Включаем использование массива цветовых атрибутов
        glEnableClientState(GL_COLOR_ARRAY);
        // Указываем, как читать цвета вершин из буфера
        // 3 - количество компонентов (r,g,b)
        // GL_FLOAT - тип данных
        // 6 * Float.BYTES - stride (тот же, что и для позиции)
        // 3 * Float.BYTES - offset (смещение до начала цветовых данных)
        glColorPointer(3, GL_FLOAT, 6 * Float.BYTES, 3 * Float.BYTES);

        // ====== Выполняем рендеринг ======
        // glDrawArrays рисует примитивы из массива вершин
        // GL_TRIANGLES - тип примитива (треугольники)
        // 0 - начальный индекс в массиве
        // vertexCount - количество вершин для рисования
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);

        // ====== Отключение атрибутов ======
        // Отключаем массив цветовых атрибутов
        glDisableClientState(GL_COLOR_ARRAY);
        // Отключаем массив вершинных атрибутов
        glDisableClientState(GL_VERTEX_ARRAY);

        // Отвязываем VBO
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    /**
     * Освобождает ресурсы VBO
     * Вызывается при завершении программы
     */
    public void cleanup() {
        // Если VBO был создан (ID не равен -1)
        if (vboId != -1) {
            // Удаляем VBO из видеопамяти
            glDeleteBuffers(vboId);
            // Помечаем как "не создан"
            vboId = -1;
        }
    }
}
