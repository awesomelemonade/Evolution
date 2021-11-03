package lemon.evolution.util;

import lemon.engine.game.Player;
import lemon.engine.glfw.GLFWInput;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.EvolutionControls;
import lemon.evolution.world.GameLoop;

public class PlayerController extends EntityController<Player> implements Disposable {
	private final Disposables disposables = new Disposables();
	private final GameLoop gameLoop;
	public PlayerController(GameLoop gameLoop, GameControls<EvolutionControls, GLFWInput> controls, Player player) {
		super(controls, player);
		this.gameLoop = gameLoop;
		disposables.add(controls.onActivated(EvolutionControls.USE_ITEM, () -> {
			var current = current();
			var currentItem = current.currentItem();
			currentItem.ifPresent(item -> {
				if (!gameLoop.usedWeapon || !item.isWeapon()) {
					item.use(current);
					gameLoop.usedWeapon = gameLoop.usedWeapon || item.isWeapon();
				}
			});
		}));
	}

	@Override
	public void dispose() {
		super.dispose();
		disposables.dispose();
	}
}
