package com.mygame.world;

import lombok.Getter;

@Getter
public class ChunkMesh {

    // [x, y, z, r, g, b, ...]
    private final float[] vertices;

    // количество ВЕРШИН (не float!)
    private final int vertexCount;

    public ChunkMesh(float[] vertices) {
        this.vertices = vertices;
        this.vertexCount = vertices.length / 6; // 6 float на вершину
    }

}
