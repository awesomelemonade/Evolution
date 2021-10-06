package lemon.evolution.world;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.errorprone.annotations.CheckReturnValue;
import lemon.engine.event.EventWith;
import lemon.engine.event.Observable;
import lemon.engine.game.Player;
import lemon.engine.glfw.GLFWInput;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.engine.toolbox.Scheduler;
import lemon.evolution.EvolutionControls;
import lemon.evolution.util.EntityController;
import lemon.evolution.util.GLFWGameControls;
import lemon.evolution.util.GatedGLFWGameControls;
import lemon.futility.FSetWithEvents;
import org.lwjgl.glfw.GLFW;

import java.time.Duration;
import java.util.Iterator;
import java.util.function.Consumer;

public class GameLoop implements Disposable {
	private final Disposables disposables = new Disposables();
	private final ImmutableList<Player> allPlayers;
	private final EntityController<Player> controller;
	private final Iterator<Player> cycler;
	private final EventWith<Player> onWinner = new EventWith<>();
	private final Scheduler scheduler = new Scheduler();
	private final Observable<Boolean> running = new Observable<>(false);

	public GameLoop(ImmutableList<Player> allPlayers, GLFWGameControls<EvolutionControls> controls) {
		var gatedControls = new GatedGLFWGameControls<>(controls);
		this.cycler = Iterators.filter(Iterators.cycle(allPlayers), player -> player.alive().getValue());
		this.allPlayers = allPlayers;
		this.controller = disposables.add(new EntityController<>(gatedControls, cycler.next()));
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
		var alivePlayers = FSetWithEvents.ofFiltered(allPlayers, Player::alive, disposables::add);
		disposables.add(alivePlayers.onRemove(player -> {
			if (alivePlayers.size() == 1) {
				onWinner.callListeners(alivePlayers.stream().findFirst().orElseThrow());
			}
		}));
		// Time limit for turns
		controls.addCallback(GLFWInput::keyEvent, event -> {
			if (event.key() == GLFW.GLFW_KEY_ENTER && event.action() == GLFW.GLFW_PRESS) {
				running.setValue(true);
			}
		});
		Disposables disposeOnStop = new Disposables();
		disposables.add(running.onChangeAndRun(running -> {
			if (running) {
				disposeOnStop.add(controller.observableCurrent().onChangeAndRun(player -> {
					gatedControls.setEnabled(true);
					disposeOnStop.add(scheduler.add(Duration.ofSeconds(8), () -> gatedControls.setEnabled(false)));
					disposeOnStop.add(scheduler.add(Duration.ofSeconds(10), this::endCurrentTurn));
				}));
				disposeOnStop.add(() -> gatedControls.setEnabled(true));
			} else {
				disposeOnStop.dispose();
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

	public void update() {
		scheduler.run();
	}

	public EntityController<Player> controller() {
		return controller;
	}

	public Player currentPlayer() {
		return controller.current();
	}

	public void endCurrentTurn() {
		controller.setCurrent(cycler.next());
	}

	@Override
	public void dispose() {
		disposables.dispose();
	}

	@CheckReturnValue
	public Disposable onWinner(Consumer<? super Player> listener) {
		return onWinner.add(listener);
	}
}
