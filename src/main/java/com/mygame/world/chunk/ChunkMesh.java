package com.mygame.world.chunk;

import lombok.Getter;

@Getter
public class ChunkMesh {
    private final int id;
    private float[] vertices;
    private int vertexCount;
    private boolean dirty;

    public ChunkMesh(int id, float[] vertices) {
        this.id = id;
        this.vertices = vertices;
        this.vertexCount = vertices.length / 8;
        this.dirty = true;
    }

    public void updateVertices(float[] newVertices) {
        this.vertices = newVertices;
        this.vertexCount = newVertices.length / 6;
        this.dirty = true;
    }

    public void markDirty() {
        dirty = false;
    }
}
