package com.mygame.world;

import lombok.Getter;

@Getter
public class ChunkMesh {
    private float[] vertices;
    private int vertexCount;
    private int vboId = -1;
    private boolean dirty = false;

    public ChunkMesh(float[] vertices) {
        this.vertices = vertices;
        this.vertexCount = vertices.length / 6; // 6 float на вершину
        this.dirty = true;
    }

    public void updateVertices(float[] newVertices) {
        this.vertices = newVertices;               // заменяем старый массив
        this.vertexCount = newVertices.length / 6;
        this.dirty = true;                         // отмечаем, что буфер устарел
    }

    public void markDirty() {
        dirty = true;
    }
}
