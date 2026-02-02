package com.mygame.world;

import com.mygame.noise.OpenSimplexNoise;
import com.mygame.world.block.*;
import com.mygame.world.chunk.Chunk;
import org.joml.Vector3f;

public class WorldGeneration {
    private static final double FREQUENCY = 0.05;
    private static final double MAX_HEIGHT = 20;
    private static final int COAL_VEINS = 18;
    private static final int COAL_MIN = 6;
    private static final int COAL_MAX = 20;
    private final OpenSimplexNoise noise;

    public WorldGeneration() {
        this.noise = new OpenSimplexNoise(123456789);
    }

    public void generateChunk(Chunk chunk) {
        generateTerrain(chunk);
        generateCoal(chunk);
    }

    private void generateTerrain(Chunk chunk) {
        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int z = 0; z < Chunk.SIZE; z++) {

                int worldX = chunk.getChunkX() * Chunk.SIZE + x;
                int worldZ = chunk.getChunkZ() * Chunk.SIZE + z;

                double h = noise.eval(
                        worldX * FREQUENCY,
                        0,
                        worldZ * FREQUENCY
                );

                int height = (int) ((h + 1) / 2 * MAX_HEIGHT);

                for (int y = 0; y <= height; y++) {

                    Vector3f pos = new Vector3f(
                            worldX * Chunk.BLOCK_SIZE,
                            y * Chunk.BLOCK_SIZE,
                            worldZ * Chunk.BLOCK_SIZE
                    );

                    Block block;
                    if (y == height) {
                        block = new GrassBlock(pos);
                    } else if (y > height - 3) {
                        block = new DirtBlock(pos);
                    } else {
                        block = new StoneBlock(pos);
                    }

                    chunk.putBlock(x, y, z, block);
                }
            }
        }
    }

    private void generateCoal(Chunk chunk) {

        for (int i = 0; i < COAL_VEINS; i++) {

            int x = (int) (Math.random() * Chunk.SIZE);
            int z = (int) (Math.random() * Chunk.SIZE);
            int y = (int) (Math.random() * MAX_HEIGHT);

            Block start = chunk.getBlockLocal(x, y, z);
            if (!(start instanceof StoneBlock)) continue;

            int size = COAL_MIN +
                    (int) (Math.random() * (COAL_MAX - COAL_MIN));

            growCoal(chunk, x, y, z, size);
        }
    }

    private void growCoal(Chunk chunk, int x, int y, int z, int size) {

        int cx = x, cy = y, cz = z;

        for (int i = 0; i < size; i++) {

            Block block = chunk.getBlockLocal(cx, cy, cz);
            if (block instanceof StoneBlock) {

                Vector3f pos = block.getPosition();
                chunk.putBlock(cx, cy, cz, new CoalOreBlock(pos));
            }

            cx += (int) (Math.random() * 3) - 1;
            cy += (int) (Math.random() * 3) - 1;
            cz += (int) (Math.random() * 3) - 1;
        }
    }
}

