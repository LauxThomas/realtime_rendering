import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWVidMode;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glClearColor;
import static org.lwjgl.opengl.GL20.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FLOAT;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_TRIANGLES;
import static org.lwjgl.opengl.GL20.GL_TRUE;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDisable;
import static org.lwjgl.opengl.GL20.glDrawArrays;
import static org.lwjgl.opengl.GL20.glEnable;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGenBuffers;
import static org.lwjgl.opengl.GL20.glGetAttribLocation;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.GL_RASTERIZER_DISCARD;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;
import static org.lwjgl.opengl.GL40.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL40.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL40.*;
import static org.lwjgl.opengl.GL40.GL_STATIC_READ;
import static org.lwjgl.opengl.GL40.glBufferSubData;
import static org.lwjgl.opengl.GL40.glClear;
import static org.lwjgl.opengl.GL40.glFlush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {

    private long window;
    private int renderProgram;
    private int constructionProgram;
    private Matrix4f mvpModel;
    private Matrix4f proj;
    private Matrix4f view;
    private int vertexArr;
    private int myBufferTriangle;
    private int renderVertexShader;
    private int geoVertexShader;
    private int geoGeometyShader;
    private int renderFragmentShader;
    private Vector4f[] model;
    private int numberOfIterations = 10;
    private int numberOfVertices = 0;
    private int mybufferFeedback;
    private int renderVertex;
    private int currentConstructionVertexArray;
    private int currentConstructionBuffer;
    private int lastVertexArray;
    private int lastFeedbackBuffer;
    private int swapVertexArray;
    private int swapFeedbackBuffer;
    private double rotatorX = 265;
    private double rotatorY = 0;
    private double rotatorZ = 0;
    private float translationX = 0;
    private float translationY = -2.3f;
    private float translationZ = 0;
    private boolean rotateZ = true;


    private Main() {
        initializeWindow();
        printLegend();
        initApplication();
    }

    private void initApplication() {
        createAndCompileShaders();
        createProgrammAndLinkShaders();
        createModel();
        calculateNumberOfVertices(numberOfIterations);
        createArrayBuffer();
        createVertexArrayObject();
        createVertexAttributesAndPointers(constructionProgram);
        createTransformFeedbackBuffer();
        createSecondVertexArrayObject();
        createVertexAttributesAndPointers(renderProgram);
        setArrayAndBufferPointer();
        constructionLoop();
        gameLoop();
        terminateApplication();
    }

    private void printLegend() {
        System.out.println("Controls: \n" +
                "Move: " + "Arrow Keys\n" +
                "Zoom: " + "+ / - (US LAYOUT: \"/\" / \"]\" or Numpad + / -)\n" +
                "Rotating X: " + "W / S\n" +
                "Rotating Y: " + "Pause: E / Unpause: Q\n" +
                "Rotating Z: " + "A / D\n" +
                "" + "\n" +
                "Tree creation:" + "\n" +
                "Change #Iterations: Keys 1 - 6 \n" +
                "Reset so Default: Backspace");
    }


    private void gameLoop() {
        while (!glfwWindowShouldClose(window)) {
            clearDisplay();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glBindVertexArray(renderVertex);
            glUseProgram(renderProgram);
            glDrawArrays(GL_TRIANGLES, 0, numberOfVertices);
            glfwPollEvents();
            checkInputs();
            updateMatrices();
            calculateModel();
            calculateView();
            calculateProjection();
            glfwSwapBuffers(window);
        }

    }


    private void checkInputs() {
        if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS)
            glfwSetWindowShouldClose(window, true);
        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
            updateRotator("X", -1f);
        }
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            updateRotator("X", 1f);
        }
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            updateRotator("Y", -1f);
        }
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            updateRotator("Y", 1f);
        }
        if (glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS) {
            rotateZ = false;
        }
        if (glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS) {
            rotateZ = true;
        }

        if (rotateZ) {
            updateRotator("Z", -0.3f);
        }

        if (glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS) {
            updateTranslation("X", 0.1f);
        }
        if (glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS) {
            updateTranslation("X", -0.1f);
        }
        if (glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS) {
            updateTranslation("Y", 0.1f);
        }
        if (glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS) {
            updateTranslation("Y", -0.1f);
        }
        if (glfwGetKey(window, GLFW_KEY_KP_ADD) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_RIGHT_BRACKET) == GLFW_PRESS) {
            updateTranslation("Z", 0.1f);
        }
        if (glfwGetKey(window, GLFW_KEY_KP_SUBTRACT) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_SLASH) == GLFW_PRESS) {
            updateTranslation("Z", -0.1f);
        }
        if (glfwGetKey(window, GLFW_KEY_1) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_KP_1) == GLFW_PRESS) {
            numberOfIterations = 2;
            initApplication();
        }
        if (glfwGetKey(window, GLFW_KEY_2) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_KP_2) == GLFW_PRESS) {
            numberOfIterations = 4;
            initApplication();
        }
        if (glfwGetKey(window, GLFW_KEY_3) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_KP_3) == GLFW_PRESS) {
            numberOfIterations = 6;
            initApplication();
        }
        if (glfwGetKey(window, GLFW_KEY_4) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_KP_4) == GLFW_PRESS) {
            numberOfIterations = 8;
            initApplication();
        }
        if (glfwGetKey(window, GLFW_KEY_5) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_KP_5) == GLFW_PRESS) {
            numberOfIterations = 10;
            initApplication();
        }
        if (glfwGetKey(window, GLFW_KEY_6) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_KP_6) == GLFW_PRESS) {
            numberOfIterations = 12;
            initApplication();
        }
        if (glfwGetKey(window, GLFW_KEY_6) == GLFW_PRESS) {
            numberOfIterations = 12;
            initApplication();
        }
        if (glfwGetKey(window, GLFW_KEY_BACKSPACE) == GLFW_PRESS) {
            numberOfIterations = 10;
            rotatorX = 265;
            rotatorY = 0;
            rotatorZ = 0;
            translationX = 0;
            translationY = -2.3f;
            translationZ = 0;
            initApplication();
        }
    }

    private void updateRotator(String axis, float amount) {
        switch (axis) {
            case "X":
                rotatorX += amount;
                rotatorX %= 360;
                break;
            case "Y":
                rotatorY += amount;
                rotatorY %= 360;
                break;
            case "Z":
                rotatorZ += amount;
                rotatorZ %= 360;
                break;
            default:
                break;

        }
    }

    private void updateTranslation(String axis, float amount) {
        switch (axis) {
            case "X":
                translationX += amount;
                break;
            case "Y":
                translationY += amount;
                break;
            case "Z":
                translationZ += amount;
                break;
            default:
                break;
        }
    }

    private void terminateApplication() {
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        // Terminate GLFW and free the error callback
        glfwTerminate();
        if (glfwSetErrorCallback(null) != null) {
            Objects.requireNonNull(glfwSetErrorCallback(null)).free();
        }
        System.exit(1);
    }

    private void updateMatrices() {
        mvpModel = new Matrix4f().identity();
        view = new Matrix4f().identity();
        proj = new Matrix4f().identity();
    }

    private void constructionLoop() {
        swapVertexArray = glGenVertexArrays();
        swapFeedbackBuffer = glGenBuffers();
        for (int i = 0; i < numberOfIterations; ++i) {
            glBindVertexArray(currentConstructionVertexArray);
            glUseProgram(constructionProgram);
            glBindBuffer(GL_ARRAY_BUFFER, currentConstructionBuffer);
            glEnable(GL_RASTERIZER_DISCARD);
            glBindBufferBase(GL_TRANSFORM_FEEDBACK_BUFFER, 0, currentConstructionBuffer);
            glBeginTransformFeedback(GL_TRIANGLES);
            glDrawArrays(GL_TRIANGLES, 0, numberOfVertices);
            glEndTransformFeedback();
            glDisable(GL_RASTERIZER_DISCARD);
            glFlush();

            swapVertexArrayAndBuffers();
        }
    }


    private void swapVertexArrayAndBuffers() {
        swapVertexArray = currentConstructionVertexArray;
        currentConstructionVertexArray = lastVertexArray;
        lastVertexArray = swapVertexArray;

        swapFeedbackBuffer = currentConstructionBuffer;
        currentConstructionBuffer = lastFeedbackBuffer;
        lastFeedbackBuffer = swapFeedbackBuffer;

    }

    private void setArrayAndBufferPointer() {
        currentConstructionVertexArray = glGenVertexArrays();
        currentConstructionVertexArray = vertexArr;
        currentConstructionBuffer = glGenBuffers();
        currentConstructionBuffer = mybufferFeedback;

        lastVertexArray = glGenVertexArrays();
        lastVertexArray = renderVertex;
        lastFeedbackBuffer = glGenBuffers();
        lastFeedbackBuffer = myBufferTriangle;


    }

    private void createSecondVertexArrayObject() {
        renderVertex = glGenVertexArrays();
        glBindVertexArray(renderVertex);
        glBindBuffer(GL_ARRAY_BUFFER, mybufferFeedback);

    }

    private void createTransformFeedbackBuffer() {
        mybufferFeedback = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, mybufferFeedback);
        glBufferData(GL_ARRAY_BUFFER, model.length * numberOfVertices * 50, GL_STATIC_READ);
    }

    private void createVertexArrayObject() {
        vertexArr = glGenVertexArrays();
        glBindVertexArray(vertexArr);
        glBindBuffer(GL_ARRAY_BUFFER, myBufferTriangle);
    }

    private void createArrayBuffer() {
        myBufferTriangle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, myBufferTriangle);
        glBufferData(GL_ARRAY_BUFFER, model.length * numberOfVertices * 50, GL_STATIC_DRAW);

        FloatBuffer verticeBuffer = BufferUtils.createFloatBuffer(model.length * numberOfVertices);
        float[] temp = new float[]{
                model[0].x, model[0].y, model[0].z, model[0].w, 0.0f, 1.0f, 0.0f,
                model[1].x, model[1].y, model[1].z, model[1].w, 0.0f, 1.0f, 0.0f,
                model[2].x, model[2].y, model[2].z, model[2].w, 0.0f, 1.0f, 0.0f
        };
        verticeBuffer.put(temp).flip();
        glBufferSubData(GL_ARRAY_BUFFER, 0, verticeBuffer);
    }

    private void calculateNumberOfVertices(int iterations) {
        numberOfVertices = (int) (4 * Math.pow(3, iterations) - 3) * 3;
        System.out.println("Number of Vertices: " + numberOfVertices);
    }


    private void initializeWindow() {

        if (!glfwInit()) {
            return;
        }

        int windowWidth = 1024;
        int windowHeight = 768;
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 0);

        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        window = glfwCreateWindow(windowWidth, windowHeight, "Rekursiver Pythagoras Baum - Real-Time Rendering", NULL, NULL);
        if (window == NULL) {
            return;
        }
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(window, (vidmode != null ? vidmode.width() : 0) / 2 - windowWidth / 2, (vidmode != null ? vidmode.height() : 0) / 2 - windowHeight / 2);
        glfwMakeContextCurrent(window);

        createCapabilities();
        glfwShowWindow(window);
    }

    private void createModel() {
        model = new Vector4f[]{
                new Vector4f(0.0f, 0.5f, -0.5f, 1.0f),
                new Vector4f(-0.5f, -0.5f, -0.5f, 1.0f),
                new Vector4f(0.5f, -0.5f, -0.5f, 1.0f)
        };

    }


    private void createAndCompileShaders() {

//        String renderVertexShaderSrc = createShader("renderShader.vert");
//        String renderFragmentShaderSrc = createShader("renderShader.frag");
//        String geoVertexShaderSrc = createShader("constructionShader.vert");
//        String geoGeometyShaderSrc = createShader("constructionShader.geom");
        String renderVertexShaderSrc = "#version 420\n" +
                "layout(location = 0) in vec3 position;\n" +
                "layout(location = 1) in float length;\n" +
                "uniform mat4 view;\n" +
                "uniform mat4 proj;\n" +
                "uniform mat4 model;\n" +
                "out float renderAlpha;\n" +
                "void main ()\n" +
                "{\n" +
                "    renderAlpha = length;\n" +
                "    gl_Position = proj * view * model * vec4 (position, 1.0);\n" +
                "}";
        String renderFragmentShaderSrc = "#version 420\n" +
                "in float renderAlpha;\n" +
                "out vec4 FragColor;\n" +
                "void main ()\n" +
                "{\n" +
                "    FragColor  = vec4 (0.1f, 0.1f, 0.1f, renderAlpha);\n" +
                "}";
        String constructionVertexShaderSrc = "#version 420\n" +
                "\n" +
                "layout(location = 0) in vec3 position;\n" +
                "layout(location = 1) in float length;\n" +
                "layout(location = 2) in vec3 normal;\n" +
                "\n" +
                "out Vertex\n" +
                "{\n" +
                "    vec3 vertexPosition;\n" +
                "    float vertexLength;\n" +
                "    vec3 vertexNormal;\n" +
                "}vertex;\n" +
                "\n" +
                "\n" +
                "void main()\n" +
                "{\n" +
                "    vertex.vertexPosition = position;\n" +
                "    vertex.vertexLength = length;\n" +
                "    vertex.vertexNormal = normal;\n" +
                "}\n";
        String constructionGeometyShaderSrc = "#version 420\n" +
                "\n" +
                "layout(triangles) in;\n" +
                "layout(triangle_strip, max_vertices = 27) out;\n" +
                "\n" +
                "in Vertex{\n" +
                "    vec3 vertexPosition;\n" +
                "    float vertexLength;\n" +
                "    vec3 vertexNormal;\n" +
                "}v[];\n" +
                "\n" +
                "out vec3 out_position;\n" +
                "out float out_length;\n" +
                "out vec3 out_normal;\n" +
                "\n" +
                "//shrinkfactor\n" +
                "float shrinkFactor = 0.7f;\n" +
                "\n" +
                "void calculateTriangle(vec3 vertex1, vec3 vertex2, vec3 vertex3, float extrudeLength);\n" +
                "void emitTriangle(vec3 vertex1, vec3 vertex2, vec3 vertex3, float extrudeLength, vec3 normal);\n" +
                "\n" +
                "void main() {\n" +
                "\n" +
                "    vec3 triangleVertices[3];\n" +
                "    for (int i = 0; i < 3; i++) {\n" +
                "        triangleVertices[i] = v[i].vertexPosition;\n" +
                "    }\n" +
                "    float extrudeLength = v[0].vertexLength;\n" +
                "\n" +
                "    if (extrudeLength == 0.0f) {\n" +
                "        //do not extrude:\n" +
                "        emitTriangle(triangleVertices[0], triangleVertices[1], triangleVertices[2], 0.0f, v[0].vertexNormal);\n" +
                "    }\n" +
                "    else {\n" +
                "        vec3 shrunkenTriangle[3];\n" +
                "        vec3 tipOfTetraeder;\n" +
                "\n" +
                "        vec3 kath1 = triangleVertices[1] - triangleVertices[0];\n" +
                "        vec3 kath2 = triangleVertices[2] - triangleVertices[0];\n" +
                "        vec3 normal = normalize(cross(kath1, kath2));\n" +
                "        vec3 heigth = normal * extrudeLength;\n" +
                "        vec3 center = (triangleVertices[0]+triangleVertices[1]+triangleVertices[2])/3;\n" +
                "\n" +
                "        for (int i = 0; i < 3; i++) {\n" +
                "            shrunkenTriangle[i] = center +  heigth + ((triangleVertices[i] - center) * shrinkFactor);\n" +
                "        }\n" +
                "\n" +
                "        float tetraederAngle = shrinkFactor/3*length(kath1);\n" +
                "        tipOfTetraeder = center + heigth + normal * tetraederAngle;\n" +
                "\n" +
                "        for (int i = 0; i < 3; i++) {\n" +
                "            calculateTriangle(triangleVertices[i], shrunkenTriangle[(i + 1) % 3], shrunkenTriangle[i], 0.0f);\n" +
                "            calculateTriangle(triangleVertices[i], triangleVertices[(i + 1) % 3], shrunkenTriangle[(i + 1) % 3], 0.0f);\n" +
                "        }\n" +
                "\n" +
                "        for (int i = 0; i < 3; i++) {\n" +
                "            calculateTriangle(shrunkenTriangle[i], shrunkenTriangle[(i + 1) % 3], tipOfTetraeder, extrudeLength * shrinkFactor);\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "void calculateTriangle(vec3 vertex1, vec3 vertex2, vec3 vertex3, float extrudeLength) {\n" +
                "    emitTriangle(vertex1, vertex2, vertex3, extrudeLength, normalize(cross(vertex2-vertex1, vertex3-vertex1)));\n" +
                "}\n" +
                "\n" +
                "void emitTriangle(vec3 vertex1, vec3 vertex2, vec3 vertex3, float extrudeLength, vec3 normal) {\n" +
                "\n" +
                "    out_length = extrudeLength;\n" +
                "    out_normal = normal;\n" +
                "    out_position = vertex1;\n" +
                "    EmitVertex();\n" +
                "\n" +
                "    out_position = vertex2;\n" +
                "    EmitVertex();\n" +
                "\n" +
                "    out_position = vertex3;\n" +
                "    EmitVertex();\n" +
                "\n" +
                "    EndPrimitive();\n" +
                "}\n" +
                "\n";

        //Compile renderVertexShader:
        renderVertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(renderVertexShader, renderVertexShaderSrc);
        glCompileShader(renderVertexShader);
        if (glGetShaderi(renderVertexShader, GL_COMPILE_STATUS) != GL_TRUE) {
            System.err.println("couldn't compile renderVertexShader:\n" + glGetShaderInfoLog(renderVertexShader, 512));
        }
        //Compile renderFragmentShader:
        renderFragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(renderFragmentShader, renderFragmentShaderSrc);
        glCompileShader(renderFragmentShader);
        if (glGetShaderi(renderFragmentShader, GL_COMPILE_STATUS) != GL_TRUE) {
            System.err.println("couldn't compile renderFragmentShader: \n" + glGetShaderInfoLog(renderFragmentShader, 512));
        }
        //Compile renderVertexShader:
        geoVertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(geoVertexShader, constructionVertexShaderSrc);
        glCompileShader(geoVertexShader);
        if (glGetShaderi(geoVertexShader, GL_COMPILE_STATUS) != GL_TRUE) {
            System.err.println("couldn't compile geoVertexShader: \n" + glGetShaderInfoLog(geoVertexShader, 512));
        }
        //Compile geoVertexShader:
        geoGeometyShader = glCreateShader(GL_GEOMETRY_SHADER);
        glShaderSource(geoGeometyShader, constructionGeometyShaderSrc);
        glCompileShader(geoGeometyShader);
        if (glGetShaderi(geoGeometyShader, GL_COMPILE_STATUS) != GL_TRUE) {
            System.err.println("couldn't compile geoVertexShader: \n" + glGetShaderInfoLog(geoGeometyShader, 512));
        }
    }

    private String createShader(String shaderName) {
//        Path pathToFile = Paths.get(shaderName);
//        System.out.println(pathToFile);
        try {
            System.out.println("src/main/java/" + shaderName);
            return new String(Files.readAllBytes(Paths.get("src/main/resources/" + shaderName)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("couldn't create Shader " + shaderName);
            return null;
        }
    }

    private void createProgrammAndLinkShaders() {
        renderProgram = glCreateProgram();
        glAttachShader(renderProgram, renderVertexShader);
        glAttachShader(renderProgram, renderFragmentShader);
        glLinkProgram(renderProgram);

        constructionProgram = glCreateProgram();
        glAttachShader(constructionProgram, geoVertexShader);
        glAttachShader(constructionProgram, geoGeometyShader);
        CharSequence[] varyings = new CharSequence[]{"out_position", "out_length", "out_normal"};
        glTransformFeedbackVaryings(constructionProgram, varyings, GL_INTERLEAVED_ATTRIBS);
        glLinkProgram(constructionProgram);

    }

    private void createVertexAttributesAndPointers(int program) {
        int pos = glGetAttribLocation(program, "position");
//        System.out.println("pos: " + pos);
        glEnableVertexAttribArray(pos);
        int sizeOfFloat = 4;
        glVertexAttribPointer(pos, 3, GL_FLOAT, false, 7 * sizeOfFloat, 0);

        int length = glGetAttribLocation(program, "length");
//        System.out.println("length: " + length);
        glEnableVertexAttribArray(length);
        glVertexAttribPointer(length, 1, GL_FLOAT, false, 7 * sizeOfFloat, 3 * sizeOfFloat);

        int normal = glGetAttribLocation(program, "normal");
//        System.out.println("normal:" + normal);
        glEnableVertexAttribArray(normal);
        glVertexAttribPointer(normal, 3, GL_FLOAT, false, 7 * sizeOfFloat, 4 * sizeOfFloat);
    }


    private void calculateModel() {
        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        mvpModel.setTranslation(translationX, translationY, translationZ);
        mvpModel.rotate((float) Math.toRadians(rotatorX), 1f, 0f, 0f);
        mvpModel.rotate((float) Math.toRadians(rotatorY), 0f, 1f, 0f);
        mvpModel.rotate((float) Math.toRadians(rotatorZ), 0f, 0f, 1f);

        mvpModel.get(fb);

        int uniTrans = glGetUniformLocation(renderProgram, "model");
        glUniformMatrix4fv(uniTrans, false, fb);
    }

    private void calculateView() {
        view = new Matrix4f().lookAt(
                new Vector3f(0.0f, 0.0f, 7f),       //eye
                new Vector3f(0, 0, 0),            //center
                new Vector3f(0.0f, 2f, 0f)    //up
        );

        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        view.get(fb);
        int uniTrans = glGetUniformLocation(renderProgram, "view");
        glUniformMatrix4fv(uniTrans, false, fb);

    }


    private void calculateProjection() {
        proj = new Matrix4f().perspective(1, 1, 3, -3);

        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        proj.get(fb);
        int uniTrans = glGetUniformLocation(renderProgram, "proj");
        glUniformMatrix4fv(uniTrans, false, fb);
    }

    private void clearDisplay() {
        glEnable(GL_DEPTH_TEST);
        glClearColor(0.78f, 0.86f, 0.83f, 1.0f);
    }

    public static void main(String[] args) {
        new Main();
    }

}