package com.mygame.engine.entity;

import com.mygame.engine.graphics.Renderer;
import com.mygame.engine.physics.PhysicsSystem;
import com.mygame.world.Block;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;

import java.util.List;

/**
 * Базовый класс для всех игровых сущностей:
 * Игрок, NPC, предметы и т.д.
 */
@Getter
public abstract class Entity {
    protected Vector3f position = new Vector3f();// Позиция сущности в мире (нижняя точка)
    protected Vector3f prevPosition = new Vector3f();// Позиция сущности в мире (нижняя точка)
    protected Vector3f velocity = new Vector3f();// Вектор скорости (по XYZ)
    protected Vector3f front = new Vector3f(0, 0, -1);// Вектор направления взгляда (если сущность смотрит куда-то)
    protected Vector3f up = new Vector3f(0, 1, 0);// Вектор "вверх"
    protected Vector3f right = new Vector3f(1, 0, 0);// Вектор "право", для поворотов
    protected PhysicsSystem physicsSystem = new PhysicsSystem();

    protected float yaw = -90.0f;
    protected float pitch = 0.0f;

    // Физические параметры сущности
    protected float height = 1.8f; // рост сущности
    protected float radius = 0.05f; // радиус (для коллизий)
    @Setter
    protected boolean onGround = false;

    /**
     * Обновление состояния сущности каждый кадр
     */
    public abstract void update(float deltaTime, List<Block> nearbyBlocks);

    /**
     * Рендер сущности
     */
    public abstract void render(Renderer renderer, Vector3f renderPos);
}
