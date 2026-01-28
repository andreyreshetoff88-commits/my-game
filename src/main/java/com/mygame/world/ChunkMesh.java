package com.mygame.world;

import lombok.Getter;

/**
 * ChunkMesh - представляет собой меш (геометрию) одного чанка
 * В современном OpenGL меш хранит вершины в массиве для передачи в VBO
 */
@Getter
public class ChunkMesh {
    // Массив вершин в формате [x,y,z,r,g,b,x,y,z,r,g,b,...]
    // где каждые 6 значений представляют одну вершину (3 позиции + 3 цвета)
    private float[] vertices;
    // Количество вершин (общее количество элементов / 6)
    private int vertexCount;
    // ID VBO в OpenGL (будет использоваться позже)
    private final int vboId = -1;
    // Флаг, указывающий что меш изменился и нуждается в обновлении
    private boolean dirty = false;

    /**
     * Конструктор меша чанка
     *
     * @param vertices массив вершин [x,y,z,r,g,b,x,y,z,r,g,b,...]
     */
    public ChunkMesh(float[] vertices) {
        this.vertices = vertices;
        // Вычисляем количество вершин: каждая вершина состоит из 6 компонентов
        this.vertexCount = vertices.length / 6; // 6 float на вершину (x,y,z + r,g,b)
        // Помечаем меш как "грязный" - нуждается в загрузке в GPU
        this.dirty = true;
    }

    /**
     * Обновляет вершины меша новыми данными
     *
     * @param newVertices новый массив вершин
     */
    public void updateVertices(float[] newVertices) {
        // Заменяем старый массив вершин новым
        this.vertices = newVertices;
        // Пересчитываем количество вершин
        this.vertexCount = newVertices.length / 6;
        // Помечаем меш как "грязный" - требуется обновление в GPU
        this.dirty = true;
    }

    /**
     * Помечает меш как "грязный" - нуждается в обновлении
     */
    public void markDirty() {
        dirty = true;
    }
}
