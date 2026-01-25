package com.mygame.world;

import lombok.Getter;

import static org.lwjgl.opengl.GL15.*;

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

    public void uploadToGPU() {
        if (!dirty) return; // если ничего не изменилось, ничего не делаем

        if (vboId == -1) vboId = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        dirty = false;
    }

    public void markDirty() {
        dirty = true;
    }
}
