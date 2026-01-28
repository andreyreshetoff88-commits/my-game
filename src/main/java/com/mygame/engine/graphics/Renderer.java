package com.mygame.engine.graphics;

import com.mygame.engine.graphics.shader.Shader;
import com.mygame.engine.graphics.shader.ShaderManager;
import com.mygame.world.Chunk;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

/**
 * Renderer - главный класс для рендеринга всех объектов в игре
 * В современном OpenGL он использует шейдеры и VAO/VBO для рендеринга
 */
public class Renderer {
    // Шейдерная программа для рендеринга
    private final Shader shader;
    // Объект проекции для управления перспективой
    private final Projection projection;
    // Модельная матрица - для трансформации отдельных объектов
    private final Matrix4f modelMatrix = new Matrix4f();
    // Список VAO для чанков (чтобы освобождать ресурсы позже)
    private final List<VertexArray> chunkVAOs = new ArrayList<>();

    /**
     * Конструктор рендера
     * Инициализирует шейдеры и проекцию
     */
    public Renderer() {
        // Создаем шейдер из файлов вершинного и фрагментного шейдеров
        shader = new Shader("src/main/resources/shaders/basic.vert",
                "src/main/resources/shaders/basic.frag");
        // Добавляем шейдер в менеджер для централизованного управления
        ShaderManager.getInstance().addShader("basic", shader);
        // Создаем проекцию с параметрами: FOV=60°, near=0.1, far=100.0
        projection = new Projection(60.0f, 0.1f, 100.0f);
    }

    /**
     * Начинает новый кадр рендеринга
     * Подготавливает OpenGL к рисованию
     *
     * @param camera текущая камера
     * @param width  ширина окна
     * @param height высота окна
     */
    public void beginScene(Camera camera, int width, int height) {
        // Очищаем экран цветом неба (темно-синий)
        glClearColor(0.2f, 0.3f, 0.4f, 1.0f); // RGBA: красный=0.2, зеленый=0.3, синий=0.4, альфа=1.0
        // Очищаем буфер цвета (GL_COLOR_BUFFER_BIT) и буфер глубины (GL_DEPTH_BUFFER_BIT)
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        // Включаем тест глубины - объекты ближе к камере перекрывают дальние
        glEnable(GL_DEPTH_TEST);

        // Обновляем проекционную матрицу с текущими размерами окна
        projection.update(width, height);

        // Активируем шейдерную программу - все последующие рендер-операции будут использовать этот шейдер
        shader.bind();

        // Передаем матрицы в шейдер через uniform переменные:
        // uProjection - проекционная матрица (перспектива)
        shader.setUniform("uProjection", projection.getProjectionMatrix());
        // uView - матрица вида (камера)
        shader.setUniform("uView", camera.getViewMatrix());
        // uModel - модельная матрица (трансформации объекта) - пока единичная матрица
        shader.setUniform("uModel", modelMatrix.identity());
    }

    /**
     * Загружает чанк в GPU для рендеринга
     * Создает VAO для вершин чанка и сохраняет его
     *
     * @param chunk чанк для загрузки
     */
    public void uploadChunk(Chunk chunk) {
        // Проверяем, что у чанка есть меш и вершины
        if (chunk.getMesh() != null && chunk.getMesh().getVertices() != null) {
            // Создаем VertexArray (VAO + VBO) из вершин чанка
            VertexArray vao = new VertexArray(chunk.getMesh().getVertices());
            // Сохраняем VAO в списке для последующего рендеринга и очистки
            chunkVAOs.add(vao);
            // Помечаем чанк как загруженный в GPU
            chunk.markUploaded();
        }
    }

    /**
     * Рендерит все загруженные чанки
     */
    public void renderChunk() {
        // Проходим по всем VAO чанков и рендерим их
        for (VertexArray vao : chunkVAOs) {
            vao.render(); // Рендерим каждый чанк
        }
    }

    /**
     * Рендерит игрока как куб
     *
     * @param radius        радиус игрока (ширина/глубина)
     * @param currentHeight высота игрока
     * @param yaw           угол поворота игрока (для ориентации)
     * @param renderPos     позиция для рендеринга (с интерполяцией)
     */
    public void renderPlayer(float radius, float currentHeight, float yaw, Vector3f renderPos) {
        // Создаем вершины для куба игрока
        float[] playerVertices = createPlayerVertices(radius, currentHeight);
        // Создаем временный VAO для игрока
        VertexArray playerVAO = new VertexArray(playerVertices);

        // Создаем модельную матрицу для игрока:
        // 1. translate(renderPos) - перемещаем в позицию игрока
        // 2. rotate(...) - поворачиваем по оси Y на угол yaw
        Matrix4f playerModel = new Matrix4f()
                .translate(renderPos)                    // перемещение в позицию
                .rotate((float) Math.toRadians(-yaw), 0, 1, 0); // поворот вокруг оси Y

        // Передаем модельную матрицу игрока в шейдер
        shader.setUniform("uModel", playerModel);
        // Рендерим игрока
        playerVAO.render();
        // Освобождаем ресурсы VAO сразу после использования
        playerVAO.cleanup();

        // Восстанавливаем единичную матрицу для других объектов
        shader.setUniform("uModel", modelMatrix.identity());
    }

    /**
     * Создает вершины куба для отображения игрока
     *
     * @param radius радиус (половина ширины)
     * @param height высота игрока
     * @return массив вершин [x,y,z,r,g,b,x,y,z,r,g,b,...]
     */
    private float[] createPlayerVertices(float radius, float height) {
        float halfWidth = radius / 2;
        float cubeHeight = height - radius * 2;

        List<Float> verticesList = new ArrayList<>();

        // ПРАВИЛО: Все грани должны быть заданы ПРОТИВ часовой стрелки
        // когда смотришь НА ГРАНЬ СНАРУЖИ

        // Верхняя грань (Y+) - смотрим СНИЗУ ВВЕРХ
        // Порядок: задний левый -> задний правый -> передний правый -> передний левый
        addFaceWithWinding(verticesList,
                new float[]{-halfWidth, cubeHeight, -halfWidth}, // задний левый
                new float[]{halfWidth, cubeHeight, -halfWidth},  // задний правый
                new float[]{halfWidth, cubeHeight, halfWidth},   // передний правый
                new float[]{-halfWidth, cubeHeight, halfWidth},  // передний левый
                new float[]{1.0f, 0.0f, 1.0f} // пурпурный
        );

        // Нижняя грань (Y-) - смотрим СВЕРХУ ВНИЗ
        // Порядок: передний левый -> передний правый -> задний правый -> задний левый
        addFaceWithWinding(verticesList,
                new float[]{-halfWidth, 0, halfWidth},   // передний левый
                new float[]{halfWidth, 0, halfWidth},    // передний правый
                new float[]{halfWidth, 0, -halfWidth},   // задний правый
                new float[]{-halfWidth, 0, -halfWidth},  // задний левый
                new float[]{0.5f, 0.5f, 0.5f} // серый
        );

        // Передняя грань (Z+) - смотрим спереди
        // Порядок: левый нижний -> левый верхний -> правый верхний -> правый нижний
        addFaceWithWinding(verticesList,
                new float[]{-halfWidth, 0, halfWidth},           // левый нижний
                new float[]{-halfWidth, cubeHeight, halfWidth},  // левый верхний
                new float[]{halfWidth, cubeHeight, halfWidth},   // правый верхний
                new float[]{halfWidth, 0, halfWidth},            // правый нижний
                new float[]{0.0f, 1.0f, 0.0f} // зеленый
        );

        // Задняя грань (Z-) - смотрим сзади
        // Порядок: правый нижний -> правый верхний -> левый верхний -> левый нижний
        addFaceWithWinding(verticesList,
                new float[]{halfWidth, 0, -halfWidth},           // правый нижний
                new float[]{halfWidth, cubeHeight, -halfWidth},  // правый верхний
                new float[]{-halfWidth, cubeHeight, -halfWidth}, // левый верхний
                new float[]{-halfWidth, 0, -halfWidth},          // левый нижний
                new float[]{0.0f, 0.0f, 1.0f} // синий
        );

        // Левая грань (X-) - смотрим слева
        // Порядок: задний нижний -> задний верхний -> передний верхний -> передний нижний
        addFaceWithWinding(verticesList,
                new float[]{-halfWidth, 0, -halfWidth},          // задний нижний
                new float[]{-halfWidth, cubeHeight, -halfWidth}, // задний верхний
                new float[]{-halfWidth, cubeHeight, halfWidth},  // передний верхний
                new float[]{-halfWidth, 0, halfWidth},           // передний нижний
                new float[]{1.0f, 0.0f, 0.0f} // красный
        );

        // Правая грань (X+) - смотрим справа
        // Порядок: передний нижний -> передний верхний -> задний верхний -> задний нижний
        addFaceWithWinding(verticesList,
                new float[]{halfWidth, 0, halfWidth},            // передний нижний
                new float[]{halfWidth, cubeHeight, halfWidth},   // передний верхний
                new float[]{halfWidth, cubeHeight, -halfWidth},  // задний верхний
                new float[]{halfWidth, 0, -halfWidth},           // задний нижний
                new float[]{1.0f, 1.0f, 0.0f} // желтый
        );

        float[] vertices = new float[verticesList.size()];
        for (int i = 0; i < verticesList.size(); i++) {
            vertices[i] = verticesList.get(i);
        }

        return vertices;
    }

    // Метод с явным указанием winding order
    private void addFaceWithWinding(List<Float> vertices, float[] p1, float[] p2, float[] p3, float[] p4, float[] color) {
        // Первый треугольник: p1 -> p2 -> p3 (против часовой стрелки)
        addVertex(vertices, p1, color);
        addVertex(vertices, p2, color);
        addVertex(vertices, p3, color);

        // Вторый треугольник: p1 -> p3 -> p4 (против часовой стрелки)
        addVertex(vertices, p1, color);
        addVertex(vertices, p3, color);
        addVertex(vertices, p4, color);
    }


    /**
     * Добавляет одну вершину в список
     *
     * @param vertices список вершин
     * @param position позиция вершины [x, y, z]
     * @param color    цвет вершины [r, g, b]
     */
    private void addVertex(List<Float> vertices, float[] position, float[] color) {
        // Добавляем координаты позиции
        vertices.add(position[0]); // x
        vertices.add(position[1]); // y
        vertices.add(position[2]); // z
        // Добавляем цвет
        vertices.add(color[0]);    // r
        vertices.add(color[1]);    // g
        vertices.add(color[2]);    // b
    }

    /**
     * Освобождает все ресурсы рендера
     * Вызывается при завершении программы
     */
    public void cleanup() {
        // Освобождаем ресурсы всех VAO чанков
        for (VertexArray vao : chunkVAOs) {
            vao.cleanup();
        }
        // Очищаем список
        chunkVAOs.clear();
        // Освобождаем ресурсы шейдера
        shader.cleanup();
    }
}
