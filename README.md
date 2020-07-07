Evolution
=========
A project created by awesomelemonade. First versions created in 2012 (Not uploaded to github). First commit in this version dates back to 2015. This project is a playground for experimenting with various ideas I have learned throughout my experience with computer graphics.

Features
--------
* Event system via Java's reflection (@Subscribe annotation)
* Signed Distance Field Font Rendering: Implemented a technique shown in [this Valve paper](https://steamcdn-a.akamaihd.net/apps/valve/2007/SIGGRAPH2007_AlphaTestedMagnification.pdf) by Chris Green
* OpenGL 3+ Rendering (Vertex Arrays, Vertex Buffers, Shaders, Shader Programs, GLSL, Uniform Variables, etc)
* TimeSync using lwjgl's implementation of sync() to sync frame rates - [discussion here](http://forum.lwjgl.org/index.php?topic=5653.0)
* Multithreading (Splitting of update() and render(), Calculating Perlin Noise)
* Cubemap rendering technique for skyboxes
* Terrain generation using [perlin noise](https://web.archive.org/web/20160325134143/http://freespace.virgin.net/hugo.elias/models/m_perlin.htm)
* Raytracing with Moller Trumbore Intersection algorithm
* Linear Algebra library (Matrix, Vector)
* 3D continuous collision detection and response with [Kasper Fauerby's paper](http://www.peroxide.dk/papers/collision/collision.pdf)
* Window & Input Handling with GLFW + LWJGL 3
* Animations & Interpolations using Bezier Curves
* Cantor & [Szudzik Pairing](http://szudzik.com/ElegantPairing.pdf) function implementations ([3D variant](https://dmauro.com/post/77011214305/a-hashing-function-for-x-y-z-coordinates))
* MurMur hash implementation
* Java lambda expressions for convenient binding and unbinding of various OpenGL objects
* Particle System using OpenGL's instanced rendering with glDrawElementsInstanced and glVertexAttribDivisor
* Marching Cube - [0fps](https://0fps.net/2012/07/12/smooth-voxel-terrain-part-2/), [paulbourke](http://paulbourke.net/geometry/polygonise/), [Stanford-mdfisher](https://graphics.stanford.edu/~mdfisher/MarchingCubes.html)
* Phong (Ambient + Diffuse + Specular) lighting
* Object model loading (ObjLoader) using .obj files for 3D rendering
* Infinite Terrain generation with overhangs and caves - [stackoverflow](https://stackoverflow.com/questions/39695764/generating-voxel-overhangs-with-3d-noise), [sourceforge](http://accidentalnoise.sourceforge.net/minecraftworlds.html)
* Multithread terrain generation on the fly with ThreadPoolExecutor and ThreadLocal
* Explosions (Carvings) in destructible terrain
* RAII-styled object pools for memory conservation (ObjectPool, VectorPool, MatrixPool)

Plans
-----
* Efficient collision detection & response on marching cube mesh
* MarchingCube normal calculation for terrain coloring

Ideas
-----
* Multitexturing for terrain
* Normal mapping for textures
* Remove magic values from creation of vertexArray and vertexBuffer in favor of linking them with ShaderProgram
* Icosphere generation
* Compute shaders for marching cube
* Improvement to Marching Cube/Tetrahedra - [caltech paper](http://www.geometry.caltech.edu/pubs/ACTD07.pdf)
* Shadows - [Shadow Mapping](https://learnopengl.com/Advanced-Lighting/Shadows/Shadow-Mapping)
* Deferred lighting scheme
* Dual Contouring alternative to marching cube
* Audio with OpenAL

Other
-----
* LWJGL Version: 3.2.3 release (Downloaded 27 Nov 19)
* Requires Java 11
* Semantic Versioning 2.0.0: https://semver.org
