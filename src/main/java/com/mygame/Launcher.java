package com.mygame;

import com.mygame.engine.Engine;
import com.mygame.game.SandboxGame;

public class Launcher {
    public static void main(String[] args) {
        new Engine(new SandboxGame()).start();
    }
}
