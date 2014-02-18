/*
 * Per-pixel diffuse lighting using gl_Color property instead of materials.
 * Justin Stoecker
 */

#version 120

uniform sampler2D tex;

varying vec2 texCoords;
varying vec3 normal;
varying vec4 m_color;
varying vec3 lightDir;
varying vec3 halfVec;
varying vec3 vertexColor;

void main()
{
  vec3 n = normalize(normal);

  float k_diffuse = max(dot(n,lightDir),0.0);
  float k_specular = max(pow(dot(n,halfVec), gl_FrontMaterial.shininess),0.0);
  float k_ambient = gl_LightModel.ambient.x;
  
  vec3 c_diffuse = max(k_diffuse, k_ambient) * m_color.xyz * vertexColor;
  vec3 c_specular = gl_FrontMaterial.specular.xyz * k_specular;
  vec3 c_ambient = m_color.xyz * gl_LightModel.ambient.xyz;
  vec4 c_texture = texture2D(tex, texCoords);
  vec4 c_light = vec4(c_ambient + c_diffuse + c_specular, 1.0);
  
  gl_FragColor = c_light * ((1.0 - c_texture.a) + c_texture);
}