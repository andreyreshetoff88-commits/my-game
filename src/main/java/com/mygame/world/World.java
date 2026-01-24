package com.mygame.world;

import com.mygame.engine.entity.Entity;
import com.mygame.engine.entity.Player;
import com.mygame.engine.graphics.Renderer;
import lombok.Getter;
import org.joml.Vector3f;

import java.util.*;

public class World {

    // ★ сколько чанков загружаем по X и Z вокруг игрока
    private static final int VIEW_DISTANCE = 3;

    @Getter
    private Player player; // игрок

    private final List<Entity> entities = new ArrayList<>(); // все сущности
    private final Map<ChunkPos, Chunk> chunks = new HashMap<>(); // все чанки в мире

    // ===============================
    // 1️⃣ Конструктор
    // ===============================
    public World(Renderer renderer) {

        // создаём стартовый чанк в центре
        Chunk startChunk = new Chunk(0, 0);
        chunks.put(new ChunkPos(0, 0), startChunk);

        // спавним игрока над блоком стартового чанка
        player = new Player(generateSpawnPoint(startChunk));
        entities.add(player); // добавляем игрока в список сущностей
    }

    // ===============================
    // 2️⃣ Обновление мира
    // ===============================
    public void update(float deltaTime, Renderer renderer) {

        // генерируем/удаляем чанки вокруг игрока
        generateChunksAround(player.getPosition(), renderer);

        // обновляем все сущности (движение, физика и т.д.)
        for (Entity entity : entities) {
            entity.update(deltaTime, getNearbyBlocks(entity.getPosition()));
        }
    }

    // ===============================
    // 3️⃣ Генерация чанков вокруг игрока
    // ===============================
    private void generateChunksAround(Vector3f playerPos, Renderer renderer) {

        int playerChunkX = worldToChunk(playerPos.x); // чанк игрока по X
        int playerChunkZ = worldToChunk(playerPos.z); // чанк игрока по Z

        // 🔹 создаём новые чанки в радиусе VIEW_DISTANCE
        for (int dx = -VIEW_DISTANCE; dx <= VIEW_DISTANCE; dx++) {
            for (int dz = -VIEW_DISTANCE; dz <= VIEW_DISTANCE; dz++) {

                ChunkPos cp = new ChunkPos(playerChunkX + dx, playerChunkZ + dz);

                if (!chunks.containsKey(cp)) {
                    Chunk newChunk = new Chunk(cp.x(), cp.z());
                    chunks.put(cp, newChunk); // добавляем в мир
                }
            }
        }

        // 🔹 удаляем чанки, которые вышли за VIEW_DISTANCE
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

    // ===============================
    // 4️⃣ Рендер мира
    // ===============================
    public void render(Renderer renderer, Vector3f renderPos) {

        // сначала рендерим сущности (игрок, мобов и т.д.)
        for (Entity entity : entities) {
            entity.render(renderer, renderPos);
        }

        // потом чанки
        for (Chunk chunk : chunks.values()) {

            // если чанк ещё не загружен в GPU, загружаем
            if (!chunk.isUploaded()) {
                renderer.uploadChunk(chunk);
            }

            // каждый кадр просто рисуем
            renderer.renderChunk(chunk);
        }
    }

    // ===============================
    // 5️⃣ Получение соседних блоков (для физики/столкновений)
    // ===============================
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

    // ===============================
    // 6️⃣ Генерация точки спавна игрока
    // ===============================
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

    // ===============================
    // 7️⃣ Конвертация мировых координат в координаты чанка
    // ===============================
    private int worldToChunk(float worldCoord) {
        // делим на размер чанка и округляем вниз
        return (int) Math.floor(worldCoord / (Chunk.SIZE * Chunk.BLOCK_SIZE));
    }
}
