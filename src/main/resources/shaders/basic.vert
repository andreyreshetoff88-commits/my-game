#version 330 core

layout (location = 0) in vec3 aPosition;
layout (location = 1) in vec3 aColor;
layout (location = 2) in vec2 aTexCoord;
layout (location = 3) in float aTextureType;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;

out vec3 fragColor;
out vec2 texCoord;
out float textureType;

void main() {
    fragColor = aColor;
    texCoord = aTexCoord;
    textureType = aTextureType;
    gl_Position = uProjection * uView * uModel * vec4(aPosition, 1.0);
}
