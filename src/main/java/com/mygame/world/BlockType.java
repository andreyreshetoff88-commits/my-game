package com.mygame.world;

import lombok.Getter;

@Getter
public enum BlockType {
    // Тип блока ТРАВА
    GRASS("dirt_podzol_top", "dirt_podzol_side", "dirt"),
    // Тип блока ЗЕМЛЯ
    DIRT("dirt", "dirt",  "dirt"),
    // Тип блока КАМЕНЬ
    STONE("stone", "stone", "stone"),
    // Тип блока ДЕРЕВО
    WOOD("oak_top", "oak_side", "oak_top");

    // Поля для хранения имён текстур для каждой стороны
    private final String topTexture;     // Имя текстуры для верхней стороны
    private final String sideTexture;    // Имя текстуры для боковых сторон
    private final String bottomTexture;  // Имя текстуры для нижней стороны

    BlockType(String top, String side, String bottom) {
        this.topTexture = top;       // Сохраняем имя верхней текстуры
        this.sideTexture = side;     // Сохраняем имя боковой текстуры
        this.bottomTexture = bottom; // Сохраняем имя нижней текстуры
    }

}