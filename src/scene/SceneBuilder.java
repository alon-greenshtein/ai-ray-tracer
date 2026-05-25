package scene;

import com.google.gson.*;
import geometries.*;
import lighting.*;
import primitives.*;

public class SceneBuilder {

    public static Scene build(JsonObject root) {
        Scene scene = new Scene("scene");
        applyBackground(scene, root);
        applyAmbientLight(scene, root);
        if (root.has("lights")) {
            parseLights(scene, root.getAsJsonArray("lights"));
        } else {
            System.err.println("[SceneBuilder] WARNING: Scene has no light sources. Render may appear dark (only ambient light will be visible).");
        }
        if (!root.has("objects")) {
            throw new IllegalArgumentException("Scene JSON must contain an 'objects' array.");
        }
        parseObjects(scene, root.getAsJsonArray("objects"));
        return scene;
    }

    // ── scene-level ──────────────────────────────────────────────────

    private static void applyBackground(Scene scene, JsonObject root) {
        if (root.has("background")) {
            scene.setBackground(toColor(root.getAsJsonArray("background")));
        }
    }

    private static void applyAmbientLight(Scene scene, JsonObject root) {
        if (root.has("ambientLight")) {
            JsonArray c = root.getAsJsonObject("ambientLight").getAsJsonArray("color");
            scene.setAmbientLight(new AmbientLight(toColor(c)));
        } else {
            scene.setAmbientLight(new AmbientLight(new Color(10, 10, 10)));
        }
    }

    // ── lights ───────────────────────────────────────────────────────

    private static void parseLights(Scene scene, JsonArray lightsArr) {
        for (JsonElement el : lightsArr) {
            scene.addLight(parseLight(el.getAsJsonObject()));
        }
        if (scene.lights.isEmpty()) {
            System.err.println("[SceneBuilder] WARNING: Scene has no light sources. Render may appear dark (only ambient light will be visible).");
        }
    }

    private static LightSource parseLight(JsonObject obj) {
        String type = obj.get("type").getAsString();
        Color color = toColor(obj.getAsJsonArray("color"));

        return switch (type) {
            case "point" -> {
                PointLight light = new PointLight(color, toPoint(obj.getAsJsonArray("position")))
                        .setKl(obj.has("kl") ? obj.get("kl").getAsDouble() : 0)
                        .setKq(obj.has("kq") ? obj.get("kq").getAsDouble() : 0);
                if (obj.has("radius")) light.setRadius(obj.get("radius").getAsDouble());
                yield light;
            }
            case "spot" -> {
                SpotLight light = new SpotLight(color,
                        toPoint(obj.getAsJsonArray("position")),
                        toVector(obj.getAsJsonArray("direction")))
                        .setKl(obj.has("kl") ? obj.get("kl").getAsDouble() : 0)
                        .setKq(obj.has("kq") ? obj.get("kq").getAsDouble() : 0);
                if (obj.has("radius")) light.setRadius(obj.get("radius").getAsDouble());
                yield light;
            }
            case "directional" -> new DirectionalLight(color, toVector(obj.getAsJsonArray("direction")));
            default -> throw new IllegalArgumentException("Unknown light type: '" + type + "'. Supported: point, spot, directional");
        };
    }

    // ── objects ──────────────────────────────────────────────────────

    private static void parseObjects(Scene scene, JsonArray objectsArr) {
        for (JsonElement el : objectsArr) {
            scene.geometries.add(parseGeometry(el.getAsJsonObject()));
        }
    }

    private static Geometry parseGeometry(JsonObject obj) {
        String type = obj.get("type").getAsString();
        Material material = obj.has("material") ? parseMaterial(obj.getAsJsonObject("material")) : new Material();
        Color emission = obj.has("emission") ? toColor(obj.getAsJsonArray("emission")) : Color.BLACK;

        return switch (type) {
            case "sphere" -> new Sphere(
                    toPoint(obj.getAsJsonArray("center")),
                    obj.get("radius").getAsDouble())
                    .setEmission(emission)
                    .setMaterial(material);

            case "plane" -> new Plane(
                    toPoint(obj.getAsJsonArray("point")),
                    toVector(obj.getAsJsonArray("normal")))
                    .setEmission(emission)
                    .setMaterial(material);

            case "triangle" -> new Triangle(
                    toPoint(obj.getAsJsonArray("v0")),
                    toPoint(obj.getAsJsonArray("v1")),
                    toPoint(obj.getAsJsonArray("v2")))
                    .setEmission(emission)
                    .setMaterial(material);

            case "cylinder" -> {
                Ray axisRay = new Ray(
                        toPoint(obj.getAsJsonArray("base")),
                        toVector(obj.getAsJsonArray("direction")));
                yield new Cylinder(
                        obj.get("height").getAsDouble(),
                        axisRay,
                        obj.get("radius").getAsDouble())
                        .setEmission(emission)
                        .setMaterial(material);
            }

            default -> throw new IllegalArgumentException(
                    "Unknown geometry type: '" + type + "'. Supported: sphere, plane, triangle, cylinder");
        };
    }

    // ── material ─────────────────────────────────────────────────────

    private static Material parseMaterial(JsonObject mat) {
        Material m = new Material();
        if (mat.has("kD"))        m.setKD(mat.get("kD").getAsDouble());
        if (mat.has("kS"))        m.setKS(mat.get("kS").getAsDouble());
        if (mat.has("shininess")) m.setShininess(mat.get("shininess").getAsInt());
        if (mat.has("kR"))        m.setKr(mat.get("kR").getAsDouble());
        if (mat.has("kT"))        m.setKt(mat.get("kT").getAsDouble());
        return m;
    }

    // ── primitive converters ──────────────────────────────────────────

    private static Point toPoint(JsonArray arr) {
        return new Point(arr.get(0).getAsDouble(), arr.get(1).getAsDouble(), arr.get(2).getAsDouble());
    }

    private static Vector toVector(JsonArray arr) {
        double x = arr.get(0).getAsDouble();
        double y = arr.get(1).getAsDouble();
        double z = arr.get(2).getAsDouble();
        if (x == 0 && y == 0 && z == 0)
            throw new IllegalArgumentException("Direction/normal vector cannot be [0, 0, 0]");
        return new Vector(x, y, z);
    }

    private static Color toColor(JsonArray arr) {
        return new Color(arr.get(0).getAsDouble(), arr.get(1).getAsDouble(), arr.get(2).getAsDouble());
    }
}
