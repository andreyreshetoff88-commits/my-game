package com.mygame.world.chunk;

import lombok.Getter;

@Getter
public class ChunkMesh {
    private float[] vertices;
    private int vertexCount;
    private boolean dirty;

    public ChunkMesh(float[] vertices) {
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
