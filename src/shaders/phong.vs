/*
 * Per-pixel diffuse lighting using gl_Color property instead of materials.
 * Justin Stoecker
 */

#version 120

varying vec2 texCoords;
varying vec3 normal;
varying vec4 m_color;
varying vec3 lightDir;
varying vec3 halfVec;
varying vec3 vertexColor;

void main()
{
	normal = normalize(gl_NormalMatrix * gl_Normal);

	texCoords = gl_MultiTexCoord0.xy;
	lightDir = normalize(vec3(gl_LightSource[0].position));
	halfVec = gl_LightSource[0].halfVector.xyz;
	m_color = gl_FrontMaterial.diffuse;
	m_color.a = gl_Color.a;
	vertexColor = gl_Color.rgb;

	gl_Position = ftransform();
} 
