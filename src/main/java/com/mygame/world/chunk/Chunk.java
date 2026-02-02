package com.mygame.world.chunk;

import com.mygame.world.WorldGeneration;
import com.mygame.world.block.Block;
import com.mygame.world.block.BlockType;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Chunk {
    public static final int SIZE = 16;
    public static final float BLOCK_SIZE = 0.5f;

    private static final int X_BITS = 11;
    private static final int Y_BITS = 10;
    private static final int Z_BITS = 11;
    private static final int Z_SHIFT = 0;
    private static final int Y_SHIFT = Z_BITS;
    private static final int X_SHIFT = Y_BITS + Z_BITS;
    private static final int X_MASK = (1 << X_BITS) - 1;
    private static final int Y_MASK = (1 << Y_BITS) - 1;
    private static final int Z_MASK = (1 << Z_BITS) - 1;
    private static final int X_OFFSET = 1 << (X_BITS - 1);
    private static final int Z_OFFSET = 1 << (Z_BITS - 1);

    @Getter
    private final List<Block> blocks = new ArrayList<>();
    private final Map<Integer, Block> blockMap = new HashMap<>();
    @Getter
    private ChunkMesh mesh;
    @Getter
    private final int chunkX;
    @Getter
    private final int chunkZ;
    @Getter
    private boolean uploaded = false;

    public Chunk(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        WorldGeneration worldGeneration = new WorldGeneration();
        worldGeneration.generateChunk(this);
        uploaded = false;
    }

    private int pack(int x, int y, int z) {
        int px = x + X_OFFSET;
        int py = y;
        int pz = z + Z_OFFSET;

        px = px & X_MASK;
        py = py & Y_MASK;
        pz = pz & Z_MASK;

        return (px << X_SHIFT) | (py << Y_SHIFT) | (pz << Z_SHIFT);
    }

    public boolean isBlockAt(int x, int y, int z, Map<Long, Chunk> neighborChunks) {
        int key = pack(x, y, z);
        if (blockMap.containsKey(key)) return false;

        if (neighborChunks != null) {
            int chunkOffsetX = (x < 0) ? -1 : (x >= SIZE) ? 1 : 0;
            int chunkOffsetZ = (z < 0) ? -1 : (z >= SIZE) ? 1 : 0;

            if (chunkOffsetX != 0 || chunkOffsetZ != 0) {
                long neighborKey = (((long) (chunkX + chunkOffsetX)) << 32) | ((chunkZ + chunkOffsetZ) & 0xFFFFFFFFL);
                Chunk neighbor = neighborChunks.get(neighborKey);
                if (neighbor != null) {
                    int nx = x - chunkOffsetX * SIZE;
                    int nz = z - chunkOffsetZ * SIZE;
                    return !neighbor.blockMap.containsKey(pack(nx, y, nz));
                }
            }
        }
        return true;
    }

    public void buildMesh(Map<Long, Chunk> neighborChunks) {
        List<Float> verticesList = new ArrayList<>();
        float s = BLOCK_SIZE / 2f;

        for (Block block : blocks) {
            int bx = (int) ((block.getPosition().x / BLOCK_SIZE) - chunkX * SIZE);
            int by = (int) (block.getPosition().y / BLOCK_SIZE);
            int bz = (int) ((block.getPosition().z / BLOCK_SIZE) - chunkZ * SIZE);

            boolean top = isBlockAt(bx, by + 1, bz, neighborChunks);
            boolean bottom = isBlockAt(bx, by - 1, bz, neighborChunks);
            boolean front = isBlockAt(bx, by, bz + 1, neighborChunks);
            boolean back = isBlockAt(bx, by, bz - 1, neighborChunks);
            boolean left = isBlockAt(bx - 1, by, bz, neighborChunks);
            boolean right = isBlockAt(bx + 1, by, bz, neighborChunks);

            addCube(verticesList, block.getPosition().x, block.getPosition().y, block.getPosition().z, s,
                    top, bottom, front, back, left, right, block.getBlockType());
        }

        float[] vertices = new float[verticesList.size()];
        for (int i = 0; i < verticesList.size(); i++) {
            vertices[i] = verticesList.get(i);
        }

        if (mesh == null)
            mesh = new ChunkMesh(vertices);
        else {
            mesh.updateVertices(vertices);
        }
    }

    private void addCube(List<Float> vertices, float x, float y, float z, float s,
                         boolean top, boolean bottom, boolean front,
                         boolean back, boolean left, boolean right, BlockType blockType) {

        float[] whiteColor = {1.0f, 1.0f, 1.0f};

        float[][][] faces = {
                // face 0: Верхняя грань
                {{-s, +s, -s}, {+s, +s, -s}, {+s, +s, +s}, {+s, +s, +s}, {-s, +s, +s}, {-s, +s, -s}},
                // face 1: Нижняя грань
                {{-s, -s, -s}, {-s, -s, +s}, {+s, -s, +s}, {+s, -s, +s}, {+s, -s, -s}, {-s, -s, -s}},
                // face 2: Передняя грань
                {{+s, -s, +s}, {-s, -s, +s}, {-s, +s, +s}, {-s, +s, +s}, {+s, +s, +s}, {+s, -s, +s}},
                // face 3: Задняя грань
                {{-s, -s, -s}, {+s, -s, -s}, {+s, +s, -s}, {+s, +s, -s}, {-s, +s, -s}, {-s, -s, -s}},
                // face 4: Левая грань
                {{-s, -s, +s}, {-s, -s, -s}, {-s, +s, -s}, {-s, +s, -s}, {-s, +s, +s}, {-s, -s, +s}},
                // face 5: Правая грань
                {{+s, -s, -s}, {+s, -s, +s}, {+s, +s, +s}, {+s, +s, +s}, {+s, +s, -s}, {+s, -s, -s}}
        };

        boolean[] visible = {top, bottom, front, back, left, right};

        float[] uvCoords = {
                0, 0,
                1, 0,
                1, 1,
                1, 1,
                0, 1,
                0, 0
        };

        for (int faceIndex = 0; faceIndex < 6; faceIndex++) {
            if (!visible[faceIndex]) continue;
            float textureType = getUnitForeTexture(blockType, faceIndex);

            for (int vertexIndex = 0; vertexIndex < 6; vertexIndex++) {
                float vx = x + faces[faceIndex][vertexIndex][0];
                float vy = y + faces[faceIndex][vertexIndex][1];
                float vz = z + faces[faceIndex][vertexIndex][2];

                vertices.add(vx);
                vertices.add(vy);
                vertices.add(vz);
                vertices.add(whiteColor[0]);
                vertices.add(whiteColor[1]);
                vertices.add(whiteColor[2]);
                vertices.add(uvCoords[vertexIndex * 2]);
                vertices.add(uvCoords[vertexIndex * 2 + 1]);
                vertices.add(textureType);
            }
        }

    }

    public void destroyBlock(Block block) {
        blocks.remove(block);
        blockMap.values().remove(block);
        uploaded = false;
    }

    private float getUnitForeTexture(BlockType blockType, int faceIndex) {
        final float GRASS_TOP_TEX = 0.0f;
        final float GRASS_SIDE_TEX = 1.0f;
        final float DIRT_TEX = 2.0f;
        final float STONE_TEX = 3.0f;
        final float WOOD_TOP_TEX = 4.0f;
        final float WOOD_SIDE_TEX = 5.0f;
        final float COAL_ORE = 7.0f;

        switch (blockType) {
            case GRASS -> {
                if (faceIndex == 0) {
                    return GRASS_TOP_TEX;
                } else if (faceIndex == 1) {
                    return DIRT_TEX;
                } else {
                    return GRASS_SIDE_TEX;
                }
            }
            case DIRT -> {
                return DIRT_TEX;
            }
            case STONE -> {
                return STONE_TEX;
            }
            case WOOD -> {
                if (faceIndex == 0 || faceIndex == 1) { // Верх и низ
                    return WOOD_TOP_TEX;
                } else {
                    return WOOD_SIDE_TEX;
                }
            }
            case COAL_ORE -> {
                return COAL_ORE;
            }
            default -> {
                return 1.0f;
            }
        }
    }

    public void putBlock(int x, int y, int z, Block block) {
        int key = pack(x, y, z);

        // Если блок уже есть — удаляем
        Block old = blockMap.get(key);
        if (old != null) {
            blocks.remove(old);
        }

        blocks.add(block);
        blockMap.put(key, block);
        uploaded = false;
    }

    public Block getBlockLocal(int x, int y, int z) {
        return blockMap.get(pack(x, y, z));
    }

    public void markUploaded() {
        uploaded = true;
    }
}
