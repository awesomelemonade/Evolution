package lemon.engine.time;

public class TimeSync {
	private long variableYieldTime, lastTime, lastSecond;
	private int fpsCounter;
	private int fps;

	public TimeSync() {
		variableYieldTime = 0;
		lastTime = 0;
		lastSecond = 0;
		fpsCounter = 0;
		fps = 0;
	}

	/**
	 * An accurate sync method that adapts automatically to the system it runs on to
	 * provide reliable results.
	 * 
	 * @param fps
	 *            The desired frame rate, in frames per second
	 * @author kappa (On the LWJGL Forums)
	 */
	public void sync(int fps) {
		if (lastSecond == 0) {
			lastSecond = System.currentTimeMillis();
		}
		fpsCounter++;
		while (lastSecond + 1000 < System.currentTimeMillis()) {
			lastSecond += 1000;
			this.fps = fpsCounter;
			fpsCounter = 0;
		}
		if (fps <= 0)
			return;

		long sleepTime = 1000000000 / fps; // nanoseconds to sleep this frame
		// yieldTime + remainder micro & nano seconds if smaller than sleepTime
		long yieldTime = Math.min(sleepTime, variableYieldTime + sleepTime % (1000 * 1000));
		long overSleep = 0; // time the sync goes over by

		try {
			while (true) {
				long t = System.nanoTime() - lastTime;

				if (t < sleepTime - yieldTime) {
					Thread.sleep(1);
				} else if (t < sleepTime) {
					// burn the last few CPU cycles to ensure accuracy
					Thread.yield();
				} else {
					overSleep = t - sleepTime;
					break; // exit while loop
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			lastTime = System.nanoTime() - Math.min(overSleep, sleepTime);

			// auto tune the time sync should yield
			if (overSleep > variableYieldTime) {
				// increase by 200 microseconds (1/5 a ms)
				variableYieldTime = Math.min(variableYieldTime + 200 * 1000, sleepTime);
			} else if (overSleep < variableYieldTime - 200 * 1000) {
				// decrease by 2 microseconds
				variableYieldTime = Math.max(variableYieldTime - 2 * 1000, 0);
			}
		}
	}
	public int getFps() {
		return fps;
	}
}
