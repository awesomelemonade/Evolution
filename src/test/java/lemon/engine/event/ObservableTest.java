package lemon.engine.event;

import lemon.engine.toolbox.Disposables;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ObservableTest {
	@Mock
	private Consumer<Integer> onSet;
	@Mock
	private Consumer<Integer> onChange;
	@Mock
	private Consumer<Integer> onChangeAndRun;

	private Observable<Integer> observable;
	private final Disposables disposables = new Disposables();

	private static final int INITIAL_VALUE = 10;

	@BeforeEach
	public void setup() {
		observable = new Observable<>(INITIAL_VALUE);
		disposables.add(observable.onSet(onSet));
		disposables.add(observable.onChange(onChange));
		disposables.add(observable.onChangeAndRun(onChangeAndRun));
	}

	@AfterEach
	public void cleanup() {
		disposables.dispose();
	}

	@Test
	public void testChange() {
		observable.setValue(1);
		verify(onSet).accept(1);
		verify(onChange).accept(1);
		verify(onChangeAndRun, times(1)).accept(INITIAL_VALUE);
		verify(onChangeAndRun, times(1)).accept(1);
		assertEquals(1, observable.getValue());
	}

	@Test
	public void testNoChange() {
		observable.setValue(INITIAL_VALUE);
		verify(onSet).accept(INITIAL_VALUE);
		verify(onChange, never()).accept(INITIAL_VALUE);
		verify(onChangeAndRun, times(1)).accept(INITIAL_VALUE);
		assertEquals(INITIAL_VALUE, observable.getValue());
	}
}