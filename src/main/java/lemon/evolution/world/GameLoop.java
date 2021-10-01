package lemon.evolution.world;

import com.google.common.collect.ImmutableList;
import lemon.engine.game.Player;
import lemon.engine.glfw.GLFWInput;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.EvolutionControls;
import lemon.evolution.util.EntityController;
import lemon.evolution.util.GLFWGameControls;
import org.lwjgl.glfw.GLFW;

public class GameLoop implements Disposable {
	private final Disposables disposables = new Disposables();
	private final ImmutableList<Player> players;
	private final EntityController<Player> controller;

	public GameLoop(ImmutableList<Player> players, GLFWGameControls<EvolutionControls> controls) {
		this.players = players;
		this.controller = disposables.add(new EntityController<>(controls, players.get(0)));
	}

	public void bindNumberKeys(GLFWInput input) {
		for (int i = 0; i < players.size(); i++) {
			var player = players.get(i);
			var key = GLFW.GLFW_KEY_1 + i;
			var disposable = input.keyEvent().add(event -> {
				if (event.action() == GLFW.GLFW_RELEASE) {
					if (event.key() == key) {
						controller.setCurrent(player);
					}
				}
			});
			disposables.add(player.world().entities().onRemove(entity -> {
				if (entity == player) {
					disposable.dispose();
				}
			}));
		}
	}

	public EntityController<Player> controller() {
		return controller;
	}

	public Player currentPlayer() {
		return controller.current();
	}

	@Override
	public void dispose() {
		disposables.dispose();
	}
}
