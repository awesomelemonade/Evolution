package lemon.evolution.world;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.errorprone.annotations.CheckReturnValue;
import lemon.engine.event.Event;
import lemon.engine.event.EventWith;
import lemon.engine.event.Observable;
import lemon.engine.game.Player;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.engine.toolbox.Scheduler;
import lemon.evolution.EvolutionControls;
import lemon.evolution.util.GLFWGameControls;
import lemon.evolution.util.GatedGLFWGameControls;
import lemon.evolution.util.PlayerController;
import lemon.futility.FSetWithEvents;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class GameLoop implements Disposable {
	private static final Duration OBSERVE_TIME = Duration.ofSeconds(60);
	private static final Duration ACTION_TIME = Duration.ofSeconds(8);
	private static final Duration RESOLVE_TIME = Duration.ofSeconds(2);
	private final Disposables disposables = new Disposables();
	private final Disposables endTurnDisposables = disposables.add(new Disposables());
	private final GatedGLFWGameControls<EvolutionControls> gatedControls;
	private final ImmutableList<Player> allPlayers;
	private final PlayerController controller;
	private final Iterator<Player> cycler;
	private final EventWith<Player> onWinner = new EventWith<>();
	private final Scheduler scheduler = disposables.add(new Scheduler());
	private final Event onUpdate = new Event();
	private final Observable<Boolean> started = new Observable<>(false);
	public boolean usedWeapon = true; // TODO: Temporary
	public Instant startTime; // TODO: Temporary
	public Instant endTime; // TODO: Temporary

	public GameLoop(ImmutableList<Player> allPlayers, GLFWGameControls<EvolutionControls> controls) {
		this.gatedControls = new GatedGLFWGameControls<>(controls);
		this.cycler = Iterators.filter(Iterators.cycle(allPlayers), player -> player.alive().getValue());
		this.allPlayers = allPlayers;
		this.controller = disposables.add(new PlayerController(this, gatedControls, cycler.next()));
		var disposeWhenNotAlive = disposables.add(new Disposables());
		disposables.add(controller.observableCurrent().onChangeAndRun(player -> {
			disposeWhenNotAlive.add(player.alive().onChangeTo(false, () -> {
				disposeWhenNotAlive.dispose();
				endTurn();
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
		disposables.add(controls.onActivated(EvolutionControls.START_GAME, () -> {
			started.setValue(true);
		}));
		disposables.add(started.onChangeTo(true, () -> {
			disposables.add(controller.observableCurrent().onChangeAndRun(player -> {
				gatedControls.setEnabled(true);
				var task = scheduler.add(ACTION_TIME, this::endTurn);
				usedWeapon = false;
				startTime = Instant.now();
				endTime = task.executionTime();
				endTurnDisposables.add(task);
				endTurnDisposables.add(controls.onActivated(EvolutionControls.END_TURN, this::endTurn));
			}));
		}));
		disposables.add(() -> gatedControls.setEnabled(true));
	}

	public void endTurn() {
		gatedControls.setEnabled(!started.getValue());
		scheduler.add(RESOLVE_TIME, this::cycleToNextPlayer);
		endTurnDisposables.dispose();
		endTime = Instant.now();
	}

	public void update() {
		scheduler.run();
		onUpdate.callListeners();
	}

	public PlayerController controller() {
		return controller;
	}

	public Player currentPlayer() {
		return controller.current();
	}

	public Observable<Player> observableCurrentPlayer() {
		return controller.observableCurrent();
	}

	public void cycleToNextPlayer() {
		controller.setCurrent(cycler.next());
	}

	public GatedGLFWGameControls<EvolutionControls> getGatedControls() {
		return this.gatedControls;
	}

	@Override
	public void dispose() {
		disposables.dispose();
	}

	@CheckReturnValue
	public Disposable onWinner(Consumer<? super Player> listener) {
		return onWinner.add(listener);
	}

	public Observable<Boolean> started() {
		return started;
	}

	public List<Player> players() {
		return allPlayers;
	}

	@CheckReturnValue
	public Disposable onUpdate(Runnable runnable) {
		return onUpdate.add(runnable);
	}

	public Event onUpdate() {
		return onUpdate;
	}
}
