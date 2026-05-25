# Java Ray Tracer

A Java-based 3D rendering engine that generates photorealistic images from complex scenes using ray tracing. Built with a modular object-oriented architecture, physics-based lighting, and a multi-stage optimization pipeline delivering up to **40× speedup** on dense scenes.

---

## Gallery

### Soft Shadows
<img src="images/SoftShadowBoxSphereCylinder.png" width="500"/>

*Area light sampling — natural penumbra instead of hard shadow edges*

### Reflections & Refraction
<img src="images/reflectionTwoSpheresMirrored.png" width="370"/> <img src="images/refractionTwoSpheres.png" width="370"/>

### BVH Optimization
<img src="images/mimip_02_full_optimization.png" width="400"/>

*Same scene (500+ objects, 700×700px) rendered with full optimization — BVH + multithreading brings render time from ~30 minutes down to ~45 seconds*

<details>
<summary>More renders</summary>

<img src="images/refractionShadow.png" width="370"/> <img src="images/shadowTrianglesSphere.png" width="370"/>
<img src="images/lightCombined1.png" width="370"/> <img src="images/lightCombined2.png" width="370"/>

</details>

---

## Architecture

The engine is organized into five decoupled modules:

```
primitives/    Point, Vector, Ray, Color, Material — math and data foundation
geometries/    Sphere, Plane, Triangle, Polygon, Cylinder, Tube + BVH acceleration
lighting/      AmbientLight, DirectionalLight, PointLight, SpotLight
renderer/      Camera, RayTracer, ImageWriter, PixelManager (multithreading)
scene/         Scene — aggregates geometry and lighting into a renderable unit
```

**Design patterns applied:**
- **Builder** — `Camera.getBuilder()...build()` produces validated, immutable camera objects
- **Template Method / NVI** — `Intersectable` defines the intersection contract; subclasses implement geometry-specific logic without exposing internals
- **Composite** — `Geometries` handles arbitrarily nested collections with a uniform interface
- **Strategy** — pluggable ray tracer implementations via `RayTracerType`

---

## Rendering

Each pixel traces a ray into the scene, finds the closest intersection via the BVH tree, and evaluates the Phong lighting model across all active light sources. Shadow rays determine occlusion, and reflected/refracted rays are spawned recursively — producing mirror surfaces, transparent materials, and soft shadow transitions in a single unified pass.

---

## Lighting

```java
new AmbientLight(color)                                      // uniform base illumination
new DirectionalLight(color, direction)                       // parallel rays, no falloff
new PointLight(color, position).setKl(1e-4).setKq(1e-6)     // quadratic distance falloff
new SpotLight(color, position, direction).setKl(1e-4)        // directional cone
```

---

## Soft Shadows

Each light source exposes a configurable surface area. For every intersection point, the renderer distributes shadow rays across the light's area using a uniform grid and averages the results — producing smooth penumbra transitions.

```java
camera.setSoftShadows(true)
      .setGridResolution(9);  // 9×9 = 81 shadow rays per point
```

The super-sampling infrastructure is shared across soft shadows, anti-aliasing, and glossy surface effects — no duplicated logic.

---

## Performance Optimizations

### AABB Bounding Boxes
Every object gets an Axis-Aligned Bounding Box computed at build time. Ray-box intersection is O(1) and skips expensive geometry math on misses.

### BVH — Bounding Volume Hierarchy
Objects are organized into a binary tree by spatial proximity. Each internal node stores the combined bounding box of its subtree, allowing entire branches to be culled in a single test.

### SAH — Surface Area Heuristic
`BVHBuilder` constructs the tree automatically using the Surface Area Heuristic — selecting the split plane that minimizes expected traversal cost, keeping the tree balanced on uneven spatial distributions.

### Multithreading
`PixelManager` distributes rows across worker threads with lock-minimized synchronization and configurable thread count (including auto-detect).

**Results on a 500+ object scene at 700×700:**

| Configuration | Time | Speedup |
|---|---|---|
| No optimization | ~30 min | baseline |
| Multithreading only | ~13 min | 2.3× |
| BVH only | ~3 min | 10× |
| BVH + Multithreading | ~45 sec | **40×** |

---

## Usage

```java
Scene scene = new Scene("Demo")
    .setBackground(new Color(20, 20, 40))
    .setAmbientLight(new AmbientLight(new Color(15, 15, 15)));

scene.geometries.add(
    new Sphere(new Point(0, 0, -100), 50)
        .setEmission(new Color(30, 0, 0))
        .setMaterial(new Material()
            .setKD(0.5).setKS(0.5).setShininess(60).setKR(0.3))
);

scene.lights.add(
    new SpotLight(new Color(500, 400, 400), new Point(-50, 50, -30), new Vector(1, -1, 4))
        .setKl(1E-5).setKq(1.5E-7)
);

Camera.getBuilder()
    .setLocation(new Point(0, 0, 0))
    .setDirection(new Point(0, 0, -1), new Vector(0, 1, 0))
    .setVpDistance(100).setVpSize(500, 500)
    .setResolution(800, 800)
    .setRayTracer(scene, RayTracerType.SIMPLE)
    .setMultithreading(-1)
    .setSoftShadows(true).setGridResolution(5)
    .build()
    .renderImage()
    .writeToImage("output");
// → images/output.png
```

---

## Tech Stack

**Language:** Java 21 &nbsp;|&nbsp; **Build:** Maven &nbsp;|&nbsp; **Testing:** JUnit 5
