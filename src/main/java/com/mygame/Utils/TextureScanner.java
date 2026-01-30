package com.mygame.Utils;

import java.io.File;
import java.util.*;

public class TextureScanner {
    public static Map<String, String> scanFolder() {
        File file = new File("src/main/resources/textures");
        Map<String, String> textureMap = new HashMap<>();
        Queue<File> fileTree = new PriorityQueue<>();

        Collections.addAll(fileTree, Objects.requireNonNull(file.listFiles()));

        while (!fileTree.isEmpty()) {
            File f = fileTree.remove();
            if (f.isDirectory()) {
                Collections.addAll(fileTree, Objects.requireNonNull(f.listFiles()));
            } else {
                textureMap.put(getFileNameWithoutExtension(f), getResourcePath(f.getAbsolutePath()).replace("\\", "/"));
            }
        }

        return textureMap;
    }

    private static String getFileNameWithoutExtension(File file) {
        return file.getName().substring(0, file.getName().lastIndexOf("."));
    }

    private static String getResourcePath(String file) {
        return file.substring(file.lastIndexOf("src"));
    }
}
