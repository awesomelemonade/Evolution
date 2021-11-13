package lemon.evolution;

import com.google.common.collect.ImmutableMap;
import lemon.engine.control.GLFWWindow;
import lemon.engine.draw.Drawable;
import lemon.engine.game.Player;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.math.Vector3D;
import lemon.engine.render.MatrixType;
import lemon.engine.toolbox.ObjLoader;
import lemon.evolution.entity.*;
import lemon.evolution.pool.MatrixPool;
import lemon.evolution.util.CommonPrograms3D;
import lemon.evolution.util.GLFWGameControls;
import lemon.evolution.world.Entity;
import lemon.evolution.world.GameLoop;
import lemon.evolution.world.WorldRenderer;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class GameResources {
    private final ImmutableMap<String, Consumer<ObjLoader>> objLoaders;
    public GameResources(GLFWWindow window, WorldRenderer worldRenderer, GameLoop gameLoop, GLFWGameControls<EvolutionControls> controls) {
        this.gameLoop = gameLoop;
        var entityRenderer = worldRenderer.entityRenderer();
        var builder = ImmutableMap.<String, Consumer<ObjLoader>>builder();
        builder.put("/res/cloud.obj",
                objLoader -> {
                    var drawable = objLoader.toIndexedDrawable();
                    Consumer<Entity> renderer = entity -> {
                        GL11.glEnable(GL11.GL_DEPTH_TEST);
                        CommonPrograms3D.LIGHT.use(program -> {
                            try (var translationMatrix = MatrixPool.ofTranslation(entity.position());
                                 var scalarMatrix = MatrixPool.ofScalar(entity.scalar().multiply(0.03f))) {
                                var sunlightDirection = Vector3D.of(0f, 1f, 0f).normalize();
                                program.loadMatrix(MatrixType.MODEL_MATRIX, translationMatrix.multiply(scalarMatrix));
                                program.loadVector("sunlightDirection", sunlightDirection);
                                program.loadVector("viewPos", gameLoop.currentPlayer().position());
                            }
                            drawable.draw();
                        });
                        GL11.glDisable(GL11.GL_DEPTH_TEST);
                    };
                    entityRenderer.registerIndividual(RainmakerEntity.class, renderer);
                });
        builder.put("/res/dragon.obj",
                objLoader -> {
                    var drawable = objLoader.toIndexedDrawable();
                });
        builder.put("/res/rocket-launcher-unloaded.obj",
                objLoader -> {
                    var drawable = objLoader.toIndexedDrawable();
                    entityRenderer.registerIndividual(StaticEntity.class,
                            entity -> entity.isType(StaticEntity.Type.ROCKET_LAUNCHER),
                            getGenericRenderer(drawable));
                });
        builder.put("/res/rocket-launcher-loaded.obj",
                objLoader -> {
                    var drawable = objLoader.toIndexedDrawable();
                    viewModel = new ViewModel(window.getWidth(), window.getHeight(), () -> {
                        GL11.glEnable(GL11.GL_DEPTH_TEST);
                        CommonPrograms3D.LIGHT.use(program -> {
                            try (var translationMatrix = MatrixPool.ofTranslation(Vector3D.of(3.5f, -4f, 1f));
                                 var rotationMatrix = MatrixPool.ofRotationY(MathUtil.PI / 2f)) {
                                var sunlightDirection = Vector3D.of(0f, 1f, 0f).normalize();
                                program.loadMatrix(MatrixType.MODEL_MATRIX, rotationMatrix.multiply(translationMatrix));
                                program.loadVector("sunlightDirection", sunlightDirection);
                                program.loadVector("viewPos", gameLoop.currentPlayer().position());
                                program.loadMatrix(MatrixType.VIEW_MATRIX, Matrix.IDENTITY_4);
                            }
                            drawable.draw();
                        });
                        GL11.glDisable(GL11.GL_DEPTH_TEST);
                    });
                });
        builder.put("/res/rocket-launcher-projectile.obj",
                objLoader -> {
                    var drawable = objLoader.toIndexedDrawable();
                    Consumer<Entity> renderer = entity -> {
                        GL11.glEnable(GL11.GL_DEPTH_TEST);
                        CommonPrograms3D.LIGHT.use(program -> {
                            var sunlightDirection = Vector3D.of(0f, 1f, 0f);
                            try (var translationMatrix = MatrixPool.ofTranslation(entity.position());
                                 var rotationMatrix = MatrixPool.ofLookAt(entity.velocity());
                                 var adjustedMatrix = MatrixPool.ofRotationY(MathUtil.PI / 2f);
                                 var scalarMatrix = MatrixPool.ofScalar(entity.scalar())) {
                                program.loadMatrix(MatrixType.MODEL_MATRIX, translationMatrix.multiply(rotationMatrix).multiply(adjustedMatrix).multiply(scalarMatrix));
                                program.loadVector("sunlightDirection", sunlightDirection);
                                drawable.draw();
                            }
                        });
                        GL11.glDisable(GL11.GL_DEPTH_TEST);
                    };
                    entityRenderer.registerIndividual(ExplodeOnHitProjectile.class, entity -> entity.isType(ExplodeType.MISSILE), renderer);
                    entityRenderer.registerIndividual(ExplodeOnHitProjectile.class, entity -> entity.isType(ExplodeType.MINI_MISSILE), renderer);
                    entityRenderer.registerIndividual(MissileShowerEntity.class, renderer);
                });
        builder.put("/res/fox.obj",
                objLoader -> {
                    var drawable = objLoader.toIndexedDrawable();
                    entityRenderer.registerCollection(Player.class, players -> {
                        for (var player : players) {
                            if (player != gameLoop.currentPlayer() || controls.isActivated(EvolutionControls.FREECAM)) {
                                GL11.glEnable(GL11.GL_DEPTH_TEST);
                                CommonPrograms3D.COLOR.use(program -> {
                                    try (var translationMatrix = MatrixPool.ofTranslation(player.position());
                                         var rotationMatrix = MatrixPool.ofRotationY(player.rotation().y() + MathUtil.PI);
                                         var scalarMatrix = MatrixPool.ofScalar(0.45f, 0.45f, 0.45f)) {
                                        program.loadMatrix(MatrixType.MODEL_MATRIX, translationMatrix.multiply(rotationMatrix).multiply(scalarMatrix));
                                    }
                                    drawable.draw();
                                });
                                GL11.glDisable(GL11.GL_DEPTH_TEST);
                            }
                        }
                    });
                });
        builder.put("/res/crate.obj",
                objLoader -> {
                    var drawable = objLoader.toIndexedDrawable();
                    var crateRenderer = getGenericRenderer(drawable);
                    entityRenderer.registerIndividual(StaticEntity.class,
                            entity -> entity.isType(StaticEntity.Type.CRATE),
                            crateRenderer);
                    entityRenderer.registerIndividual(ItemDropEntity.class, crateRenderer);
                });
        builder.put("/res/parachute.obj",
                objLoader -> {
                    var drawable = objLoader.toIndexedDrawable();
                    var parachuteRenderer = getGenericRenderer(drawable, Vector3D.of(0f, -5.5f, 0f), 0.7f);
                    entityRenderer.registerIndividual(StaticEntity.class,
                            entity -> entity.isType(StaticEntity.Type.PARACHUTE),
                            parachuteRenderer);
                    entityRenderer.registerIndividual(ItemDropEntity.class, ItemDropEntity::initialDrop, parachuteRenderer);
                });
        objLoaders = builder.build();
    }

    public Consumer<Entity> getGenericRenderer(Drawable drawable) {
        return getGenericRenderer(drawable, Vector3D.ZERO, 1f);
    }
    public Consumer<Entity> getGenericRenderer(Drawable drawable, Vector3D offset, float scale) {
        return entity -> {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            CommonPrograms3D.LIGHT.use(program -> {
                try (var translationMatrix = MatrixPool.ofTranslation(entity.position().add(offset));
                     var scalarMatrix = MatrixPool.ofScalar(entity.scalar().multiply(scale))) {
                    var sunlightDirection = Vector3D.of(0f, 1f, 0f).normalize();
                    program.loadMatrix(MatrixType.MODEL_MATRIX, translationMatrix.multiply(scalarMatrix));
                    program.loadVector("sunlightDirection", sunlightDirection);
                    program.loadVector("viewPos", gameLoop.currentPlayer().position());
                }
                drawable.draw();
            });
            GL11.glDisable(GL11.GL_DEPTH_TEST);
        };
    }

    public List<ObjLoader> loaders(Executor postLoadExecutor) {
        return objLoaders.entrySet().stream().map(entry -> new ObjLoader(entry.getKey(), postLoadExecutor, entry.getValue())).toList();
    }

    private final GameLoop gameLoop;
    // Outputs
    private ViewModel viewModel;
    public ViewModel viewModel() {
        return viewModel;
    }
}
