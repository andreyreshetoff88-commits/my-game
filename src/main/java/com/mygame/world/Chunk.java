package com.mygame.world;

import com.mygame.engine.graphics.Renderer;
import com.mygame.noise.OpenSimplexNoise;
import lombok.Getter;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Chunk {
    public static final int SIZE = 16;
    public static final float BLOCK_SIZE = 0.5f;

    private final int chunkX;
    private final int chunkZ;
    @Getter
    private final List<Block> blocks = new ArrayList<>();
    private ChunkMesh chunkMesh;

    public Chunk(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        generate();
    }

    private void generate() {
        OpenSimplexNoise noise = new OpenSimplexNoise(12345L);
        float scale = 0.1f;
        int maxHeight = 8;

        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                int worldX = chunkX * SIZE + x;
                int worldZ = chunkZ * SIZE + z;

                float n = (float) noise.noise(worldX * scale, worldZ * scale);
                int height = Math.round((n + 1) / 2 * maxHeight); // нормализуем шум [-1,1] → [0,maxHeight]

                for (int y = 0; y <= height; y++) {
                    Vector3f pos = new Vector3f(
                            worldX * BLOCK_SIZE,
                            y * BLOCK_SIZE,
                            worldZ * BLOCK_SIZE
                    );

                    // Можно задавать цвет блока по высоте
                    Vector3f color;
                    if (y == height) {
                        color = new Vector3f(0.1f, 0.8f, 0.1f); // травяной верх
                    } else {
                        color = new Vector3f(0.6f, 0.4f, 0.2f); // земля
                    }

                    blocks.add(new Block(pos, color));
                }
            }
        }

        // создаём mesh после генерации блоков
        chunkMesh = new ChunkMesh(blocks);
    }

    public void render(Renderer renderer) {
        if (chunkMesh != null) {
            renderer.renderChunkMesh(chunkMesh);
        }
    }

    public void cleanup(Renderer renderer) {
        if (chunkMesh != null) {
            renderer.cleanupChunkMesh(chunkMesh);
        }
    }
}
