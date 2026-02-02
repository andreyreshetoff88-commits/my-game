package com.mygame.world;

import com.mygame.noise.OpenSimplexNoise;
import com.mygame.world.block.*;
import com.mygame.world.chunk.Chunk;
import org.joml.Vector3f;

public class WorldGeneration {
    private static final double FREQUENCY = 0.05;
    private static final double MAX_HEIGHT = 50;
    private final OpenSimplexNoise noise;

    public WorldGeneration() {
        this.noise = new OpenSimplexNoise(123456789);
    }

    public void generateChunk(Chunk chunk) {
        generateTerrain(chunk);
        generateOre(chunk, 18, 6, 20, 5, 45, CoalOreBlock.class);
        generateOre(chunk, 12, 5, 20, 5, 40, IronOreBlock.class);
        generateTrees(chunk,50);
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
                    if (y <= 1) {
                        block = new BadRockBlock(pos);
                    } else if (y == height) {
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

    private void generateOre(Chunk chunk, int veins, int minSize, int maxSize, int minY, int maxY, Class<? extends Block> oreClass) {
        for (int i = 0; i < veins; i++) {
            int x = (int) (Math.random() * Chunk.SIZE);
            int z = (int) (Math.random() * Chunk.SIZE);
            int y = minY + (int) (Math.random() * (maxY - minY));

            Block start = chunk.getBlockLocal(x, y, z);
            if (!(start instanceof StoneBlock)) continue;

            int size = minSize + (int) (Math.random() * (maxSize - minSize));
            growOre(chunk, x, y, z, size, oreClass);
        }
    }

    private void growOre(Chunk chunk, int x, int y, int z, int size, Class<? extends Block> oreClass) {
        int cx = x, cy = y, cz = z;

        for (int i = 0; i < size; i++) {
            Block block = chunk.getBlockLocal(cx, cy, cz);
            if (block instanceof StoneBlock) {
                try {
                    Block ore = oreClass.getConstructor(Vector3f.class).newInstance(block.getPosition());
                    chunk.putBlock(cx, cy, cz, ore);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Случайное смещение в пределах 1 блока (GregTech style)
            int dx = (int) (Math.random() * 3) - 1;
            int dy = (int) (Math.random() * 3) - 1;
            int dz = (int) (Math.random() * 3) - 1;

            // Небольшая вероятность для "ветвления"
            if (Math.random() < 0.3) dx *= 2;
            if (Math.random() < 0.3) dy *= 2;
            if (Math.random() < 0.3) dz *= 2;

            cx = Math.max(0, Math.min(Chunk.SIZE - 1, cx + dx));
            cy = (int) Math.max(2, Math.min(MAX_HEIGHT - 1, cy + dy)); // не ниже бедрока
            cz = Math.max(0, Math.min(Chunk.SIZE - 1, cz + dz));
        }
    }

    private void generateTrees(Chunk chunk, int treeChanel) {
        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int z = 0; z < Chunk.SIZE; z++) {
                // Ищем верхний блок на позиции x,z
                int topY = -1;
                for (int y = (int) MAX_HEIGHT; y >= 0; y--) {
                    Block block = chunk.getBlockLocal(x, y, z);
                    if (block instanceof GrassBlock) {
                        topY = y;
                        break;
                    }
                }

                if (topY == -1) continue; // нет земли, дерево не растёт

                // Шанс сгенерировать дерево
                if ((int) (Math.random() * treeChanel) == 0) {
                    generateTreeAt(chunk, x, topY + 1, z);
                }
            }
        }
    }

    private void generateTreeAt(Chunk chunk, int x, int y, int z) {
        int height = 4 + (int) (Math.random() * 3); // высота ствола 4-6 блоков

        // Ствол
        for (int i = 0; i < height; i++) {
            Vector3f pos = new Vector3f(
                    (chunk.getChunkX() * Chunk.SIZE + x) * Chunk.BLOCK_SIZE,
                    (y + i) * Chunk.BLOCK_SIZE,
                    (chunk.getChunkZ() * Chunk.SIZE + z) * Chunk.BLOCK_SIZE
            );
            chunk.putBlock(x, y + i, z, new WoodBlock(pos));
        }
        // Листья (куб 3x3x3 вокруг верхушки)
        int leafStart = y + height - 1;
        for (int lx = -1; lx <= 1; lx++) {
            for (int ly = 0; ly <= 2; ly++) {
                for (int lz = -1; lz <= 1; lz++) {
                    int bx = x + lx;
                    int by = leafStart + ly;
                    int bz = z + lz;

                    // проверка границ чанка
                    if (bx < 0 || bx >= Chunk.SIZE || bz < 0 || bz >= Chunk.SIZE) continue;

                    Block existing = chunk.getBlockLocal(bx, by, bz);
                    if (existing == null) { // не перекрывать другие блоки
                        Vector3f pos = new Vector3f(
                                (chunk.getChunkX() * Chunk.SIZE + bx) * Chunk.BLOCK_SIZE,
                                by * Chunk.BLOCK_SIZE,
                                (chunk.getChunkZ() * Chunk.SIZE + bz) * Chunk.BLOCK_SIZE
                        );
                        chunk.putBlock(bx, by, bz, new LeavesOakBlock(pos));
                    }
                }
            }
        }
    }
}


