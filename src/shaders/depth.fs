// Depth Map Shader -- Fragment Program
// Author: Justin Stoecker

#version 120

varying float depth;

void main()
{
  gl_FragColor = vec4(depth, depth, depth, 1.0);
}