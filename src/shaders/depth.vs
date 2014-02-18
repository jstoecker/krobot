// Depth Map Shader -- Vertex Program
// Author: Justin Stoecker

#version 120

varying float depth;

void main()
{
  vec4 position_cs = ftransform();
  depth = (position_cs.z / position_cs.w) * 0.5 + 0.5; // depth in [0,1] where 0 = near and 1 = far
  gl_Position = position_cs;
}