package lemon.evolution.world;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import lemon.engine.event.EventWith;
import lemon.engine.game.Player;
import lemon.engine.glfw.GLFWInput;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.EvolutionControls;
import lemon.evolution.util.EntityController;
import lemon.evolution.util.GLFWGameControls;
import lemon.futility.FSetWithEvents;
import org.lwjgl.glfw.GLFW;

import java.util.Iterator;
import java.util.function.Consumer;

public class GameLoop implements Disposable {
	private final Disposables disposables = new Disposables();
	private final ImmutableList<Player> allPlayers;
	private final EntityController<Player> controller;
	private final Iterator<Player> cycler;
	private final EventWith<Player> onWinner = new EventWith<>();

	public GameLoop(ImmutableList<Player> allPlayers, GLFWGameControls<EvolutionControls> controls) {
		this.cycler = Iterators.filter(Iterators.cycle(allPlayers), player -> player.alive().getValue());
		this.allPlayers = allPlayers;
		this.controller = disposables.add(new EntityController<>(controls, cycler.next()));
		var disposeWhenNotAlive = new Disposables();
		disposables.add(controller.observableCurrent().onChangeAndRun(player -> {
			disposeWhenNotAlive.add(player.alive().onChange(alive -> {
				if (!alive) {
					disposeWhenNotAlive.dispose();
					controller.setCurrent(cycler.next());
				}
			}));
		}));
		// Win Condition
		var alivePlayers = new FSetWithEvents<Player>();
		for (var player : allPlayers) {
			disposables.add(player.alive().onChangeAndRun(alive -> {
				if (alive) {
					alivePlayers.add(player);
				} else {
					alivePlayers.remove(player);
				}
			}));
		}
		disposables.add(alivePlayers.onRemove(player -> {
			if (alivePlayers.size() == 1) {
				onWinner.callListeners(alivePlayers.stream().findFirst().orElseThrow());
			}
		}));
	}

	public void bindNumberKeys(GLFWInput input) {
		for (int i = 0; i < allPlayers.size(); i++) {
			var player = allPlayers.get(i);
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

	public Disposable onWinner(Consumer<? super Player> listener) {
		return onWinner.add(listener);
	}
}
