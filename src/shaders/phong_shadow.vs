#version 120

uniform mat4 lightViewProjBias;
uniform mat4 modelMatrix;

varying vec2 diffuseTexCoords;
varying vec2 depthTexCoords;
varying float depth;

varying vec2 texCoords;
varying vec3 normal;
varying vec4 vertexColor;

void main()
{
  vec4 position_light = lightViewProjBias * modelMatrix * gl_Vertex;
  position_light /= position_light.w; 
  depthTexCoords = position_light.xy;
  depth = position_light.z;
  diffuseTexCoords = gl_MultiTexCoord0.xy;
  normal = normalize(gl_NormalMatrix * gl_Normal);
  vertexColor = gl_Color;
  
  gl_Position = ftransform();
}