package com.mygame.world.block;

import lombok.Getter;

@Getter
public enum BlockType {
    GRASS("dirt_podzol_top", "dirt_podzol_side", "dirt"),
    DIRT("dirt", "dirt", "dirt"),
    STONE("stone", "stone", "stone"),
    WOOD("oak_top", "oak_side", "oak_top"),
    COAL_ORE("coal_ore", "coal_ore", "coal_ore"),
    IRON_ORE("iron_ore", "iron_ore", "iron_ore"),
    BEDROCK("bedrock", "bedrock", "bedrock"),
    LEAVES_OAK("leaves_oak", "leaves_oak", "leaves_oak"),;

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