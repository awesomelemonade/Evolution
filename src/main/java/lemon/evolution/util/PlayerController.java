package lemon.evolution.util;

import lemon.engine.event.Observable;
import lemon.engine.game.Player;
import lemon.engine.glfw.GLFWInput;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.EvolutionControls;
import lemon.evolution.world.GameLoop;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public class PlayerController extends EntityController<Player> implements Disposable {
	private static final float POWER_METER_SPEED_FACTOR = 2.0f;
	private final Disposables disposables = new Disposables();
	private final GameLoop gameLoop;
	private final Observable<Float> powerMeter = new Observable<>(0f);
	private final Observable<Optional<Instant>> startUseItemTime = new Observable<>(Optional.empty());

	public PlayerController(GameLoop gameLoop, GameControls<EvolutionControls, GLFWInput> controls, Player initialPlayer) {
		super(controls, initialPlayer);
		this.gameLoop = gameLoop;
		var disposeOnStop = new Disposables();
		disposables.add(startUseItemTime.onChange(potentialStartTime -> {
			potentialStartTime.ifPresentOrElse(time -> {
				disposeOnStop.dispose();
				powerMeter.setValue(0f);
				disposeOnStop.add(gameLoop.onUpdate(() -> {
					// Adjust power meter
					var secondsPassed = Duration.between(time, Instant.now()).toMillis() / 1000.0;
					powerMeter.setValue((float) (1.0 - Math.abs(Math.cos(secondsPassed * POWER_METER_SPEED_FACTOR))));
				}));
			}, () -> {
				disposeOnStop.dispose();
				powerMeter.setValue(0f);
			});
		}));
		disposables.add(controls.activated(EvolutionControls.USE_ITEM).onChange(activated -> {
			if (activated) {
				startUseItemTime.setValue(Optional.of(Instant.now()));
			} else {
				// Use Item
				startUseItemTime.getValue().ifPresent(time -> {
					var power = powerMeter.getValue();
					var current = current();
					var currentItem = current.inventory().currentItem();
					currentItem.ifPresent(item -> {
						if (!gameLoop.usedWeapon || !item.isWeapon()) {
							item.use(current, power);
							gameLoop.usedWeapon = gameLoop.usedWeapon || item.isWeapon();
							current.inventory().removeOneOfCurrentItem();
						}
					});
					startUseItemTime.setValue(Optional.empty());
				});
			}
		}));
	}

	@Override
	public void dispose() {
		super.dispose();
		disposables.dispose();
	}

	public Observable<Float> observablePowerMeter() {
		return powerMeter;
	}

	public float powerMeter() {
		return powerMeter.getValue();
	}
}
