package renderer;

import geometries.Cylinder;
import geometries.Plane;
import geometries.Polygon;
import geometries.Sphere;
import lighting.AmbientLight;
import lighting.PointLight;
import org.junit.jupiter.api.Test;
import primitives.*;
import scene.Scene;

public class SoftShadowsTests {
    /** Default constructor to satisfy JavaDoc generator */
    SoftShadowsTests() { /* to satisfy JavaDoc generator */ }

    /** Scene for the tests */
    private final Scene scene         = new Scene("Test scene");
    /** Camera builder for the tests with triangles */
    private final Camera.Builder cameraBuilder = Camera.getBuilder()     //
            .setRayTracer(scene, RayTracerType.SIMPLE);


    @Test
    void boxSphereCylinderSoftShadow() {
        // Add floor (plane)
        scene.geometries.add(
                new Plane(new Point(0, 0, 0), new Vector(0, 0, 1))
                        .setEmission(new Color(70, 70, 70))
                        .setMaterial(new Material().setKD(0.8).setKS(0.2).setShininess(20))
        );

        // Add sphere
        scene.geometries.add(
                new Sphere(new Point(-40, -60, 50), 40)
                        .setEmission(new Color(80, 80, 80))
                        .setMaterial(new Material().setKD(0.7).setKS(0.3).setShininess(40))
        );

        // Add box (cube, 6 polygons)
        double cubeSize = 70;
        Point cubeBase = new Point(40, 30, cubeSize / 2);
        scene.geometries.add(
                // Front face
                new Polygon(
                        cubeBase.add(new Vector(-cubeSize / 2, -cubeSize / 2, -cubeSize / 2)),
                        cubeBase.add(new Vector(cubeSize / 2, -cubeSize / 2, -cubeSize / 2)),
                        cubeBase.add(new Vector(cubeSize / 2, cubeSize / 2, -cubeSize / 2)),
                        cubeBase.add(new Vector(-cubeSize / 2, cubeSize / 2, -cubeSize / 2))
                ).setEmission(new Color(80, 80, 80)).setMaterial(new Material().setKD(0.7).setKS(0.3).setShininess(40)),
                // Back face
                new Polygon(
                        cubeBase.add(new Vector(-cubeSize / 2, -cubeSize / 2, cubeSize / 2)),
                        cubeBase.add(new Vector(cubeSize / 2, -cubeSize / 2, cubeSize / 2)),
                        cubeBase.add(new Vector(cubeSize / 2, cubeSize / 2, cubeSize / 2)),
                        cubeBase.add(new Vector(-cubeSize / 2, cubeSize / 2, cubeSize / 2))
                ).setEmission(new Color(80, 80, 80)).setMaterial(new Material().setKD(0.7).setKS(0.3).setShininess(40)),
                // 4 sides
                new Polygon(
                        cubeBase.add(new Vector(-cubeSize / 2, -cubeSize / 2, -cubeSize / 2)),
                        cubeBase.add(new Vector(cubeSize / 2, -cubeSize / 2, -cubeSize / 2)),
                        cubeBase.add(new Vector(cubeSize / 2, -cubeSize / 2, cubeSize / 2)),
                        cubeBase.add(new Vector(-cubeSize / 2, -cubeSize / 2, cubeSize / 2))
                ).setEmission(new Color(80, 80, 80)).setMaterial(new Material().setKD(0.7).setKS(0.3).setShininess(40)),
                new Polygon(
                        cubeBase.add(new Vector(-cubeSize / 2, cubeSize / 2, -cubeSize / 2)),
                        cubeBase.add(new Vector(cubeSize / 2, cubeSize / 2, -cubeSize / 2)),
                        cubeBase.add(new Vector(cubeSize / 2, cubeSize / 2, cubeSize / 2)),
                        cubeBase.add(new Vector(-cubeSize / 2, cubeSize / 2, cubeSize / 2))
                ).setEmission(new Color(80, 80, 80)).setMaterial(new Material().setKD(0.7).setKS(0.3).setShininess(40)),
                new Polygon(
                        cubeBase.add(new Vector(-cubeSize / 2, -cubeSize / 2, -cubeSize / 2)),
                        cubeBase.add(new Vector(-cubeSize / 2, cubeSize / 2, -cubeSize / 2)),
                        cubeBase.add(new Vector(-cubeSize / 2, cubeSize / 2, cubeSize / 2)),
                        cubeBase.add(new Vector(-cubeSize / 2, -cubeSize / 2, cubeSize / 2))
                ).setEmission(new Color(80, 80, 80)).setMaterial(new Material().setKD(0.7).setKS(0.3).setShininess(40)),
                new Polygon(
                        cubeBase.add(new Vector(cubeSize / 2, -cubeSize / 2, -cubeSize / 2)),
                        cubeBase.add(new Vector(cubeSize / 2, cubeSize / 2, -cubeSize / 2)),
                        cubeBase.add(new Vector(cubeSize / 2, cubeSize / 2, cubeSize / 2)),
                        cubeBase.add(new Vector(cubeSize / 2, -cubeSize / 2, cubeSize / 2))
                ).setEmission(new Color(80, 80, 80)).setMaterial(new Material().setKD(0.7).setKS(0.3).setShininess(40))
        );

        // Add cylinder
        scene.geometries.add(
                new Cylinder(
                        110, new Ray(new Point(130, -30, 0), new Vector(0, 0, 1)), 25
                ).setEmission(new Color(80, 80, 80))
                        .setMaterial(new Material().setKD(0.7).setKS(0.3).setShininess(40))
        );

        // Light source with soft shadow
        scene.lights.add(
                new PointLight(new Color(500, 500, 500), new Point(100, 150, 200))
                        .setRadius(50) // soft shadow radius
                        .setKl(0.0008).setKq(0.0001)
        );

        // Ambient light
        scene.setAmbientLight(new AmbientLight(new Color(30, 30, 30).scale(0.03)));

        Camera camera = cameraBuilder
                .setSoftShadows(true)
                .setGridResolution(5)
                .setLocation(new Point(70, 80, 320))
                .setDirection(new Point(0, 0, 0), new Vector(0, 0, 1))
                .setVpDistance(330)
                .setMultithreading(-2)
                .setVpSize(350, 350)
                .setResolution(500, 500)
                .build()
                .renderImage()
                .writeToImage("SoftShadowBoxSphereCylinder");
    }

}
