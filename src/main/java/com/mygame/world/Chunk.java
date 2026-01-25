package com.mygame.world;

import com.mygame.noise.OpenSimplexNoise;
import lombok.Getter;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Chunk {
    public static final int SIZE = 16;
    public static final float BLOCK_SIZE = 0.5f;
    private static final double FREQUENCY = 0.05;
    private static final double MAX_HEIGHT = 50;

    // сколько бит выделяем под каждую координату
    private static final int X_BITS = 11; // для X
    private static final int Y_BITS = 10; // для Y
    private static final int Z_BITS = 11; // для Z
    // смещения битов
    private static final int Z_SHIFT = 0;
    private static final int Y_SHIFT = Z_BITS;
    private static final int X_SHIFT = Y_BITS + Z_BITS;
    // маски (оставляют ТОЛЬКО нужное количество бит)
    private static final int X_MASK = (1 << X_BITS) - 1;
    private static final int Y_MASK = (1 << Y_BITS) - 1;
    private static final int Z_MASK = (1 << Z_BITS) - 1;
    // смещения, чтобы отрицательные координаты стали положительными
    private static final int X_OFFSET = 1 << (X_BITS - 1); // 1024
    private static final int Z_OFFSET = 1 << (Z_BITS - 1); // 1024

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
    private static final OpenSimplexNoise noise = new OpenSimplexNoise(123456);

    public Chunk(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        generateBlocks();
        buildMesh(null);
    }

    private void generateBlocks() {
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                int worldX = chunkX * SIZE + x;
                int worldZ = chunkZ * SIZE + z;
                double nx = worldX * FREQUENCY;
                double ny = 0;               // фиксированное значение, т.к. мы хотим высоту по x/z
                double nz = worldZ * FREQUENCY;

                double value = noise.eval(nx, ny, nz);  // возвращает -1..1
                int height = (int) ((value + 1) / 2 * MAX_HEIGHT);
                for (int y = 0; y <= height; y++) {
                    Vector3f pos = new Vector3f(worldX * BLOCK_SIZE, y * BLOCK_SIZE, worldZ * BLOCK_SIZE);
                    Block block = new Block(pos);
                    blocks.add(block);

                    int key = pack(x, y, z);
                    blockMap.put(key, block);
                }
            }
        }
    }

    private int pack(int x, int y, int z) {
        //Смещаем отрицательные координаты в положительный диапазон
        int px = x + X_OFFSET; // X: -1024..1023 → 0..2047
        int py = y;            // Y: 0..1023
        int pz = z + Z_OFFSET; // Z: -1024..1023 → 0..2047

        //Обрезаем лишние биты (ОЧЕНЬ ВАЖНО)
        px = px & X_MASK;
        py = py & Y_MASK;
        pz = pz & Z_MASK;

        //Сдвигаем каждый компонент в своё место
        return (px << X_SHIFT) | (py << Y_SHIFT) | (pz << Z_SHIFT);
    }

    public boolean isBlockAt(int x, int y, int z, Map<Long, Chunk> neighborChunks) {
        int key = pack(x, y, z);
        if (blockMap.containsKey(key)) return true; // текущий чанк

        if (neighborChunks != null) {
            // преобразуем x,z в координаты соседнего чанка
            int chunkOffsetX = (x < 0) ? -1 : (x >= SIZE) ? 1 : 0;
            int chunkOffsetZ = (z < 0) ? -1 : (z >= SIZE) ? 1 : 0;

            if (chunkOffsetX != 0 || chunkOffsetZ != 0) {
                long neighborKey = (((long) (chunkX + chunkOffsetX)) << 32) | ((chunkZ + chunkOffsetZ) & 0xFFFFFFFFL);
                Chunk neighbor = neighborChunks.get(neighborKey);
                if (neighbor != null) {
                    int nx = x - chunkOffsetX * SIZE;
                    int nz = z - chunkOffsetZ * SIZE;
                    return neighbor.blockMap.containsKey(pack(nx, y, nz));
                }
            }
        }
        return false;
    }

    public void buildMesh(Map<Long, Chunk> neighborChunks) {
        int visibleFaces = 0;
        for (Block block : blocks) {
            int bx = (int) ((block.position().x / BLOCK_SIZE) - chunkX * SIZE);
            int by = (int) (block.position().y / BLOCK_SIZE);
            int bz = (int) ((block.position().z / BLOCK_SIZE) - chunkZ * SIZE);

            if (!isBlockAt(bx, by + 1, bz, neighborChunks)) visibleFaces++;
            if (!isBlockAt(bx, by - 1, bz, neighborChunks)) visibleFaces++;
            if (!isBlockAt(bx, by, bz + 1, neighborChunks)) visibleFaces++;
            if (!isBlockAt(bx, by, bz - 1, neighborChunks)) visibleFaces++;
            if (!isBlockAt(bx - 1, by, bz, neighborChunks)) visibleFaces++;
            if (!isBlockAt(bx + 1, by, bz, neighborChunks)) visibleFaces++;
        }

        float[] vertices = new float[visibleFaces * 6 /*вершины*/ * 6 /*float на вершину*/];
        int index = 0;

        float s = BLOCK_SIZE / 2f;
        for (Block block : blocks) {
            int bx = (int) ((block.position().x / BLOCK_SIZE) - chunkX * SIZE);
            int by = (int) (block.position().y / BLOCK_SIZE);
            int bz = (int) ((block.position().z / BLOCK_SIZE) - chunkZ * SIZE);

            boolean top = !isBlockAt(bx, by + 1, bz, neighborChunks);
            boolean bottom = !isBlockAt(bx, by - 1, bz, neighborChunks);
            boolean front = !isBlockAt(bx, by, bz + 1, neighborChunks);
            boolean back = !isBlockAt(bx, by, bz - 1, neighborChunks);
            boolean left = !isBlockAt(bx - 1, by, bz, neighborChunks);
            boolean right = !isBlockAt(bx + 1, by, bz, neighborChunks);

            index = addCube(vertices, index, block.position().x, block.position().y, block.position().z, s,
                    top, bottom, front, back, left, right);
        }

        mesh = new ChunkMesh(vertices);
    }

    private int addCube(float[] v, int index, float x, float y, float z, float s,
                        boolean top, boolean bottom, boolean front,
                        boolean back, boolean left, boolean right) {

        float[][] colors = {
                {0.3f, 0.8f, 0.3f}, // верх
                {0.5f, 0.25f, 0.1f}, // низ
                {0.5f, 0.25f, 0.1f}, // перед
                {0.5f, 0.25f, 0.1f}, // зад
                {0.5f, 0.25f, 0.1f}, // лево
                {0.5f, 0.25f, 0.1f}  // право
        };
        float[][][] faces = {
                {{-s, +s, -s}, {+s, +s, -s}, {+s, +s, +s}, {+s, +s, +s}, {-s, +s, +s}, {-s, +s, -s}},
                {{-s, -s, -s}, {+s, -s, -s}, {+s, -s, +s}, {+s, -s, +s}, {-s, -s, +s}, {-s, -s, -s}},
                {{-s, -s, +s}, {+s, -s, +s}, {+s, +s, +s}, {+s, +s, +s}, {-s, +s, +s}, {-s, -s, +s}},
                {{-s, -s, -s}, {+s, -s, -s}, {+s, +s, -s}, {+s, +s, -s}, {-s, +s, -s}, {-s, -s, -s}},
                {{-s, -s, -s}, {-s, -s, +s}, {-s, +s, +s}, {-s, +s, +s}, {-s, +s, -s}, {-s, -s, -s}},
                {{+s, -s, -s}, {+s, -s, +s}, {+s, +s, +s}, {+s, +s, +s}, {+s, +s, -s}, {+s, -s, -s}}
        };

        boolean[] visible = {top, bottom, front, back, left, right};

        for (int f = 0; f < 6; f++) {
            if (!visible[f]) continue;
            for (int i = 0; i < 6; i++) {
                v[index++] = x + faces[f][i][0];
                v[index++] = y + faces[f][i][1];
                v[index++] = z + faces[f][i][2];
                v[index++] = colors[f][0];
                v[index++] = colors[f][1];
                v[index++] = colors[f][2];
            }
        }
        return index;
    }

    public void markUploaded() {
        uploaded = true;
    }
}
