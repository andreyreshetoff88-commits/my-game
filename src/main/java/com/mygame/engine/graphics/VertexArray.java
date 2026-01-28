package com.mygame.engine.graphics;

import lombok.Getter;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;


public class VertexArray {
    private final int vaoID;
    private final int vboID;
    @Getter
    private final int vertexCount;

    /**
     * Конструктор VertexArray
     *
     * @param vertices массив вершинных данных [x,y,z,r,g,b,x,y,z,r,g,b,...]
     *                 где каждые 6 значений = одна вершина (3 позиции + 3 цвета)
     */
    public VertexArray(float[] vertices) {
        // Вычисляем количество вершин: общее количество элементов / 6 (3 позиции + 3 цвета)
        this.vertexCount = vertices.length / 6;

        // ====== Создание VAO ======
        // glGenVertexArrays() создает один или несколько VAO и возвращает их ID
        vaoID = glGenVertexArrays();
        // glBindVertexArray() делает VAO текущим (активным) для последующих операций
        glBindVertexArray(vaoID);

        // ====== Создание VBO ======
        // glGenBuffers() создает буфер и возвращает его ID
        vboID = glGenBuffers();
        // glBindBuffer() делает буфер текущим для указанного типа
        // GL_ARRAY_BUFFER - тип буфера для данных вершин
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        // glBufferData() копирует данные из массива в видеопамять
        // GL_ARRAY_BUFFER - тип буфера
        // vertices - массив данных
        // GL_STATIC_DRAW - данные редко изменяются,主要用于 рисования
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        // ====== Настройка атрибутов вершин ======
        // glVertexAttribPointer настраивает как OpenGL должен интерпретировать данные

        // Атрибут 0: позиция вершины (x, y, z)
        // glVertexAttribPointer(
        //   0,                  // индекс атрибута (location = 0 в шейдере)
        //   3,                  // количество компонентов (x,y,z = 3 компонента)
        //   GL_FLOAT,           // тип данных
        //   false,              // нормализовать данные? (false = нет)
        //   6 * Float.BYTES,    // stride - расстояние между вершинами в байтах
        //   0                   // offset - смещение до начала данных
        // )
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.BYTES, 0);
        // glEnableVertexAttribArray включает использование атрибута
        glEnableVertexAttribArray(0);

        // Атрибут 1: цвет вершины (r, g, b)
        // Аналогично, но с другим смещением (3 * Float.BYTES = пропускаем x,y,z)
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        // Отвязываем буфер и VAO, чтобы случайно не изменить их
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    /**
     * Делает VAO текущим для использования
     */
    public void bind() {
        glBindVertexArray(vaoID);
    }

    /**
     * Отвязывает VAO
     */
    public void unbind() {
        glBindVertexArray(0);
    }

    /**
     * Рендерит объект используя данные из VAO
     */
    public void render() {
        bind(); // Активируем VAO
        // glDrawArrays указывает OpenGL нарисовать примитивы из массива вершин
        // GL_TRIANGLES - тип примитива (треугольники)
        // 0 - начальный индекс в массиве
        // vertexCount - количество вершин для рисования
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        unbind(); // Отвязываем VAO
    }

    /**
     * Освобождает ресурсы VAO и VBO
     */
    public void cleanup() {
        // glDeleteVertexArrays удаляет VAO из видеопамяти
        glDeleteVertexArrays(vaoID);
        // glDeleteBuffers удаляет VBO из видеопамяти
        glDeleteBuffers(vboID);
    }

}
