package lemon.futility;

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
class GateTest {
	@Mock
	private Consumer<Boolean> booleanListener;
	private Gate gate;
	private final Disposables disposables = new Disposables();

	@SuppressWarnings("unchecked")
	private static Consumer<Boolean> mockBooleanListener() {
		return (Consumer<Boolean>) mock(Consumer.class);
	}

	@BeforeEach
	public void setup() {
		gate = new Gate();
		disposables.add(gate.observableOutput().onChange(booleanListener));
	}

	@AfterEach
	public void cleanup() {
		disposables.dispose();
	}

	@Test
	public void testNoInput() {
		assertTrue(gate.output());
		verifyNoInteractions(booleanListener);
	}

	@Test
	public void testSingleInputInitialOutputTrue() {
		var observable = new FObservable<>(false);
		disposables.add(gate.addInput(observable));
		assertTrue(gate.output());
		verifyNoInteractions(booleanListener);
	}

	@Test
	public void testSingleInputInitialOutputFalse() {
		var observable = new FObservable<>(true);
		disposables.add(gate.addInput(observable));
		assertFalse(gate.output());
		verify(booleanListener).accept(false);
	}

	@Test
	public void testSingleInputChangeOutputToFalse() {
		var observable = new FObservable<>(false);
		disposables.add(gate.addInput(observable));
		observable.setValue(true);
		assertFalse(gate.output());
		verify(booleanListener).accept(false);
	}

	@Test
	public void testSingleInputChangeOutputToTrue() {
		var observable = new FObservable<>(true);
		disposables.add(gate.addInput(observable));
		observable.setValue(false);
		assertTrue(gate.output());
		verify(booleanListener).accept(false); // From initially adding input
		verify(booleanListener).accept(true); // From unblocking gate
	}

	@Test
	public void testMultiInputInitialOutputTrue() {
		for (int i = 0; i < 10; i++) {
			disposables.add(gate.addInput(new FObservable<>(false)));
		}
		assertTrue(gate.output());
		verifyNoInteractions(booleanListener);
	}

	@Test
	public void testMultiInputInitialOutputFalse() {
		disposables.add(gate.addInput(new FObservable<>(false)));
		disposables.add(gate.addInput(new FObservable<>(true)));
		disposables.add(gate.addInput(new FObservable<>(false)));
		disposables.add(gate.addInput(new FObservable<>(false)));
		assertFalse(gate.output());
		verify(booleanListener).accept(false);
	}

	@Test
	public void testMultiInputInitialOutputFalseWithAllTrue() {
		for (int i = 0; i < 10; i++) {
			disposables.add(gate.addInput(new FObservable<>(true)));
		}
		assertFalse(gate.output());
		verify(booleanListener).accept(false);
	}

	@Test
	public void testMultiInputChangeOutputToTrue() {
		var observable = new FObservable<>(true);
		var observable2 = new FObservable<>(true);
		Consumer<Boolean> listener = mockBooleanListener();
		disposables.add(gate.addInput(observable));
		disposables.add(gate.addInput(observable2));
		disposables.add(gate.observableOutput().onChange(listener));
		observable.setValue(false);
		observable2.setValue(false);
		assertTrue(gate.output());
		verify(listener).accept(true);
		verifyNoMoreInteractions(listener);
	}

	@Test
	public void testMultiInputChangeOutputToFalse() {
		var observable = new FObservable<>(false);
		var observable2 = new FObservable<>(false);
		Consumer<Boolean> listener = mockBooleanListener();
		disposables.add(gate.addInput(observable));
		disposables.add(gate.addInput(observable2));
		disposables.add(gate.observableOutput().onChange(listener));
		observable.setValue(true);
		assertFalse(gate.output());
		verify(listener).accept(false);
		verifyNoMoreInteractions(listener);
	}

	@Test
	public void testMultiInputOutputStayFalse() {
		var observable = new FObservable<>(true);
		var observable2 = new FObservable<>(true);
		Consumer<Boolean> listener = mockBooleanListener();
		disposables.add(gate.addInput(observable));
		disposables.add(gate.addInput(observable2));
		disposables.add(gate.observableOutput().onChange(listener));
		observable.setValue(false);
		assertFalse(gate.output());
		verifyNoInteractions(listener);
	}

	@Test
	public void testDuplicateInput() {
		var observable = new FObservable<>(false);
		disposables.add(gate.addInput(observable));
		disposables.add(gate.addInput(observable));
		observable.setValue(true);
		assertFalse(gate.output());
		verify(booleanListener).accept(false);
	}

	@Test
	public void testDisposeInput() {
		var observable = new FObservable<>(false);
		var disposable = gate.addInput(observable);
		disposable.dispose();
		observable.setValue(true);
		assertTrue(gate.output());
		verifyNoInteractions(booleanListener);
	}
}