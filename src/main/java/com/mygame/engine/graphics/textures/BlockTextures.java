package com.mygame.engine.graphics.textures;

public record BlockTextures(Texture top, Texture side, Texture bottom) {
    public BlockTextures(String top, String side, String bottom) {
        this(new Texture(top), new Texture(side), new Texture(bottom));
    }

    public void cleanup() {
        top.cleanup();
        side.cleanup();
        bottom.cleanup();
    }
}
