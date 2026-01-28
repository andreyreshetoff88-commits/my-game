package com.mygame.engine.physics;

import com.mygame.engine.entity.Entity;
import com.mygame.world.Block;
import org.joml.Vector3f;

import java.util.List;

/**
 * PhysicsSystem - система физики для обработки гравитации, коллизий и движения
 * Отвечает за реалистичное поведение объектов в мире
 */
public class PhysicsSystem {
    // Константа гравитации (ускорение свободного падения)
    private static final float GRAVITY = -9.8f;
    // Высота шага - максимальная высота, на которую игрок может "заступить"
    private static final float STEP_HEIGHT = 0.25f;

    /**
     * Обновляет физическое состояние сущности
     *
     * @param deltaTime время, прошедшее с последнего обновления (в секундах)
     * @param entity    сущность для обновления
     * @param blocks    список блоков для проверки коллизий
     */
    public void update(float deltaTime, Entity entity, List<Block> blocks) {
        // Сохраняем предыдущую позицию для интерполяции
        entity.getPrevPosition().set(entity.getPosition());
        // Применяем гравитацию к сущности
        applyGravity(deltaTime, entity, blocks);
        // Проверяем вертикальные коллизии (столкновения сверху/снизу)
        verticalCollision(entity, blocks);
        // Обрабатываем возможность "заступить" на блоки
        handleStep(entity, blocks);
    }

    /**
     * Применяет гравитацию к сущности
     *
     * @param deltaTime время с последнего обновления
     * @param entity    сущность, к которой применяется гравитация
     * @param blocks    блоки для проверки коллизий
     */
    public void applyGravity(float deltaTime, Entity entity, List<Block> blocks) {
        // Увеличиваем вертикальную скорость за счет гравитации
        // F = m * a, где a = GRAVITY (ускорение свободного падения)
        entity.getVelocity().y += GRAVITY * deltaTime;

        // Вычисляем новую Y-координату с учетом текущей скорости
        float newY = entity.getPosition().y + entity.getVelocity().y * deltaTime;
        // Находим максимальную Y-координату, на которую можно встать (высота блоков под ногами)
        float maxY = getMaxY(blocks, entity);

        // Проверяем, не уперлась ли сущность в блок под ногами
        if (newY <= maxY) {
            // Если да, то устанавливаем позицию точно на блок
            entity.getPosition().y = maxY;
            // Обнуляем вертикальную скорость (сущность стоит на чем-то)
            entity.getVelocity().y = 0;
            // Помечаем, что сущность на земле
            entity.setOnGround(true);
        } else {
            // Если не уперлась, обновляем позицию
            entity.getPosition().y = newY;
            // Помечаем, что сущность в воздухе
            entity.setOnGround(false);
        }
    }

    /**
     * Проверяет вертикальные коллизии (столкновения с блоками сверху)
     *
     * @param entity сущность для проверки
     * @param blocks блоки для проверки коллизий
     */
    public void verticalCollision(Entity entity, List<Block> blocks) {
        // Проверяем только если сущность движется вверх (velocity.y > 0)
        if (entity.getVelocity().y > 0) {
            // Проходим по всем блокам
            for (Block block : blocks) {
                // Проверяем, находится ли блок над сущностью
                if (block.position().y - entity.getHeight() >= entity.getPosition().y + entity.getRadius()) {
                    // Проверяем горизонтальное пересечение по X
                    boolean insideX = entity.getPosition().x + entity.getRadius() >=
                            block.position().x - entity.getRadius() &&
                            entity.getPosition().x - entity.getRadius() <= block.position().x + entity.getRadius();
                    // Проверяем горизонтальное пересечение по Z
                    boolean insideZ = entity.getPosition().z + entity.getRadius() >=
                            block.position().z - entity.getRadius() &&
                            entity.getPosition().z - entity.getRadius() <= block.position().z + entity.getRadius();

                    // Если нет пересечения по X или Z, переходим к следующему блоку
                    if (!insideX || !insideZ) {
                        continue;
                    }

                    // Проверяем вертикальное пересечение (сущность почти касается блока сверху)
                    if (entity.getPosition().y + entity.getHeight() + 0.25f >= block.position().y - 0.25f) {
                        // Обнуляем вертикальную скорость (сущность ударилась головой о блок)
                        entity.getVelocity().y = 0;
                    }
                }
            }
        }
    }

    /**
     * Обрабатывает горизонтальное движение с проверкой коллизий
     *
     * @param dx     изменение по оси X
     * @param dz     изменение по оси Z
     * @param entity сущность для перемещения
     * @param blocks блоки для проверки коллизий
     */
    public void moveHorizontal(float dx, float dz, Entity entity, List<Block> blocks) {
        // Перемещаем сущность по оси X
        entity.getPosition().x += dx;
        // Проверяем коллизии после перемещения
        if (collides(entity, blocks)) {
            // Если есть коллизия, откатываем перемещение
            entity.getPosition().x -= dx;
        }

        // Перемещаем сущность по оси Z
        entity.getPosition().z += dz;
        // Проверяем коллизии после перемещения
        if (collides(entity, blocks)) {
            // Если есть коллизия, откатываем перемещение
            entity.getPosition().z -= dz;
        }
    }

    /**
     * Проверяет, сталкивается ли сущность с каким-либо блоком
     *
     * @param entity сущность для проверки
     * @param blocks список блоков
     * @return true если есть коллизия, false если нет
     */
    private boolean collides(Entity entity, List<Block> blocks) {
        // Проходим по всем блокам
        for (Block block : blocks) {
            // Проверяем пересечение с каждым блоком
            if (intersects(block, entity)) {
                return true; // Найдено пересечение
            }
        }
        return false; // Нет пересечений
    }

    /**
     * Проверяет пересечение блока и сущности
     *
     * @param block  блок для проверки
     * @param entity сущность для проверки
     * @return true если блок и сущность пересекаются
     */
    private boolean intersects(Block block, Entity entity) {
        // Получаем позицию блока
        Vector3f bp = block.position();

        // Проверяем пересечение по оси X
        boolean overlapX = entity.getPosition().x + entity.getRadius() > bp.x - 0.25f &&
                entity.getPosition().x - entity.getRadius() < bp.x + 0.25f;

        // Проверяем пересечение по оси Z
        boolean overlapZ = entity.getPosition().z + entity.getRadius() > bp.z - 0.25f &&
                entity.getPosition().z - entity.getRadius() < bp.z + 0.25f;

        // Проверяем пересечение по оси Y
        boolean overlapY = entity.getPosition().y < bp.y + 0.25f &&
                entity.getPosition().y + entity.getHeight() > bp.y;

        // Возвращаем true если есть пересечение по всем трем осям
        return overlapX && overlapZ && overlapY;
    }

    /**
     * Обрабатывает возможность "заступить" на блоки
     *
     * @param entity сущность для обработки
     * @param blocks блоки для проверки
     */
    public void handleStep(Entity entity, List<Block> blocks) {
        // Рассчитываем потенциальное новое положение
        Vector3f moveDelta = new Vector3f(entity.getVelocity());
        moveDelta.y = 0; // Игнорируем вертикальное перемещение для расчета шага
        Vector3f newPos = new Vector3f(entity.getPosition()).add(moveDelta);

        // Получаем текущую высоту ног сущности
        float playerFeet = entity.getPosition().y;

        // Проходим по всем блокам
        for (Block block : blocks) {
            Vector3f bp = block.position();

            // Проверяем горизонтальное пересечение X/Z
            boolean overlapX = newPos.x + entity.getRadius() > bp.x - 0.25f &&
                    newPos.x - entity.getRadius() < bp.x + 0.25f;
            boolean overlapZ = newPos.z + entity.getRadius() > bp.z - 0.25f &&
                    newPos.z - entity.getRadius() < bp.z + 0.25f;

            // Если нет горизонтального пересечения, переходим к следующему блоку
            if (!overlapX || !overlapZ) continue;

            // Блок находится перед игроком
            float blockTop = bp.y + 0.25f; // верх блока
            float stepDiff = blockTop - playerFeet; // разница высот

            // Если блок не слишком высок и достаточно близко к ногам
            if (stepDiff > 0 && stepDiff <= STEP_HEIGHT) {
                // Поднимаем игрока на блок (реализация "заступания")
                entity.getPosition().y += stepDiff;
                entity.setOnGround(true);
            } else if (stepDiff > STEP_HEIGHT) {
                // Если блок слишком высок, запрещаем движение в эту сторону
                newPos.x = entity.getPosition().x;
                newPos.z = entity.getPosition().z;
            }
        }

        // Применяем смещение после проверки всех блоков
        entity.getPosition().x = newPos.x;
        entity.getPosition().z = newPos.z;
    }

    /**
     * Находит максимальную Y-координату блока под сущностью
     *
     * @param blocks список блоков для проверки
     * @param entity сущность для которой ищем
     * @return максимальная Y-координата блока под сущностью
     */
    private static float getMaxY(List<Block> blocks, Entity entity) {
        // Начинаем с минимально возможного значения
        float maxY = Float.NEGATIVE_INFINITY;

        // Проходим по всем блокам
        for (Block block : blocks) {
            Vector3f bp = block.position();

            // Проверяем, находится ли блок под сущностью (по X и Z)
            if (entity.getPosition().x + entity.getRadius() >=
                    bp.x - 0.25f && entity.getPosition().x - 0.25f <= bp.x + 0.25f &&
                    entity.getPosition().z + entity.getRadius() >=
                            bp.z - 0.25f && entity.getPosition().z - 0.25f <= bp.z + 0.25f) {

                // Вычисляем верхнюю границу блока
                float topY = bp.y + 0.25f;
                // Учитываем только блоки ниже "головы" сущности
                // и выбираем самый высокий из них
                if (topY <= entity.getPosition().y + 0.1f && topY > maxY) {
                    maxY = topY;
                }
            }
        }
        return maxY;
    }
}
