package com.mygame.world;

import com.mygame.engine.entity.Entity;
import com.mygame.engine.entity.Player;
import com.mygame.engine.graphics.Renderer;
import lombok.Getter;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class World {
    private static final int VIEW_DISTANCE = 3;// сколько чанков загружаем по X и Z вокруг игрока
    private static final int MAX_CHUNKS_PER_FRAME = 4;//максимальное количество чанков, которые генерируем за один кадр
    private static final int MAX_CHUNKS_TO_UPLOAD = 10;//максимальное количество чанков в очереди для GPU
    @Getter
    private Player player; // игрок
    private final List<Entity> entities = new ArrayList<>(); // все сущности
    private final Map<ChunkPos, Chunk> chunks = new HashMap<>(); // все чанки в мире
    private final ExecutorService chunkExecutor =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());//Executor для фоновой генерации чанков
    private final ConcurrentLinkedQueue<Chunk> readyChunks = new ConcurrentLinkedQueue<>(); //очередь готовых чанков
    private final ConcurrentLinkedQueue<Chunk> chunksToUpload = new ConcurrentLinkedQueue<>();//очередь для чанков, готовых к загрузке в GPU

    public World() {
        Chunk startChunk = new Chunk(0, 0);// создаём стартовый чанк в центре
        chunks.put(new ChunkPos(0, 0), startChunk);

        player = new Player(generateSpawnPoint(startChunk));// спавним игрока над блоком стартового чанка
        entities.add(player); // добавляем игрока в список сущностей
    }

    public void update(float deltaTime) {
        // генерируем/удаляем чанки вокруг игрока
        generateChunksAround(player.getPosition());

        while (!readyChunks.isEmpty()) {
            Chunk chunk = readyChunks.poll();
            if (chunk != null) {
                chunks.put(new ChunkPos(chunk.getChunkX(), chunk.getChunkZ()), chunk);

                if (chunksToUpload.size() < MAX_CHUNKS_TO_UPLOAD)
                    chunksToUpload.add(chunk);
            }
        }
        // обновляем все сущности (движение, физика и т.д.)
        for (Entity entity : entities) {
            entity.update(deltaTime, getNearbyBlocks(entity.getPosition()));
        }
    }

    private void generateChunksAround(Vector3f playerPos) {
        int playerChunkX = worldToChunk(playerPos.x); // чанк игрока по X
        int playerChunkZ = worldToChunk(playerPos.z); // чанк игрока по Z

        int chunksScheduled = 0;

        // создаём новые чанки в радиусе VIEW_DISTANCE
        for (int dx = -VIEW_DISTANCE; dx <= VIEW_DISTANCE; dx++) {
            for (int dz = -VIEW_DISTANCE; dz <= VIEW_DISTANCE; dz++) {
                ChunkPos cp = new ChunkPos(playerChunkX + dx, playerChunkZ + dz);

                if (!chunks.containsKey(cp) && chunksScheduled < MAX_CHUNKS_PER_FRAME) {
                    chunksScheduled++;
                    chunkExecutor.submit(() -> {
                        Chunk newChunk = new Chunk(cp.x(), cp.z());
                        newChunk.buildMesh(null);

                        if (chunksToUpload.size() < MAX_CHUNKS_TO_UPLOAD)
                            readyChunks.add(newChunk);
                    });
                }
            }
        }
        // удаляем чанки, которые вышли за VIEW_DISTANCE
        Iterator<Map.Entry<ChunkPos, Chunk>> it = chunks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ChunkPos, Chunk> entry = it.next();
            ChunkPos cp = entry.getKey();
            int dx = Math.abs(cp.x() - playerChunkX);
            int dz = Math.abs(cp.z() - playerChunkZ);
            if (dx > VIEW_DISTANCE || dz > VIEW_DISTANCE) {
                it.remove(); // удаляем из карты
            }
        }
    }

    public void render(Renderer renderer, Vector3f renderPos) {
        // Рендерим сущности (игрок, мобов и т.д.)
        for (Entity entity : entities) {
            entity.render(renderer, renderPos);
        }
        //Загружаем чанки в GPU из очереди (асинхронно)
        while (!chunksToUpload.isEmpty()) {
            Chunk chunk = chunksToUpload.poll();
            if (chunk != null && !chunk.isUploaded()) {
                renderer.uploadChunk(chunk); // загружаем в GPU
            }
        }

        // Рендрим чанки
        for (Chunk chunk : chunks.values()) {
            // каждый кадр просто рисуем
            renderer.renderChunk(chunk);
        }
    }

    public List<Block> getNearbyBlocks(Vector3f pos) {
        List<Block> result = new ArrayList<>();

        int cx = worldToChunk(pos.x); // чанк по X
        int cz = worldToChunk(pos.z); // чанк по Z
        // проверяем 3x3 чанка вокруг позиции
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {

                Chunk chunk = chunks.get(new ChunkPos(cx + dx, cz + dz));
                if (chunk != null) {
                    result.addAll(chunk.getBlocks());
                }
            }
        }

        return result;
    }

    private Vector3f generateSpawnPoint(Chunk chunk) {
        float maxY = Float.NEGATIVE_INFINITY;
        Vector3f top = null;
        // ищем самый высокий блок в чанке
        for (Block block : chunk.getBlocks()) {
            if (block.position().y > maxY) {
                maxY = block.position().y;
                top = block.position();
            }
        }
        // если нашли блок — спавним игрока над ним
        if (top != null) {
            return new Vector3f(
                    top.x,
                    top.y + Chunk.BLOCK_SIZE + 0.01f, // чуть выше блока
                    top.z
            );
        }
        // fallback, если блоков нет
        return new Vector3f(0, 5, 0);
    }

    private int worldToChunk(float worldCoord) {
        // делим на размер чанка и округляем вниз
        return (int) Math.floor(worldCoord / (Chunk.SIZE * Chunk.BLOCK_SIZE));
    }

    public void shutdown() {
        chunkExecutor.shutdown();
    }
}
