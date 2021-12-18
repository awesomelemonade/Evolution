package lemon.futility;

import lemon.engine.toolbox.Disposables;
import lemon.futility.FObservable;
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
class FObservableTest {
	@Mock
	private Consumer<Integer> onChange;
	@Mock
	private Consumer<Integer> onChangeAndRun;
	@Mock
	private Consumer<Boolean> booleanListener;

	private FObservable<Integer> observable;
	private final Disposables disposables = new Disposables();

	private static final int INITIAL_VALUE = 10;

	@BeforeEach
	public void setup() {
		observable = new FObservable<>(INITIAL_VALUE);
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
		verify(onChange).accept(1);
		verify(onChangeAndRun, times(1)).accept(INITIAL_VALUE);
		verify(onChangeAndRun, times(1)).accept(1);
		assertEquals(1, observable.getValue());
	}

	@Test
	public void testNoChange() {
		observable.setValue(INITIAL_VALUE);
		verify(onChange, never()).accept(INITIAL_VALUE);
		verify(onChangeAndRun, times(1)).accept(INITIAL_VALUE);
		assertEquals(INITIAL_VALUE, observable.getValue());
	}

	@Test
	public void testAndInitialAndFalseFalse() {
		FObservable<Boolean> a = new FObservable<>(false);
		FObservable<Boolean> b = new FObservable<>(false);
		var and = FObservable.ofAnd(a, b, disposables::add);
		assertFalse(and.getValue());
	}

	@Test
	public void testAndInitialAndTrueFalse() {
		FObservable<Boolean> a = new FObservable<>(true);
		FObservable<Boolean> b = new FObservable<>(false);
		var and = FObservable.ofAnd(a, b, disposables::add);
		assertFalse(and.getValue());
	}

	@Test
	public void testAndInitialTrueTrue() {
		FObservable<Boolean> a = new FObservable<>(true);
		FObservable<Boolean> b = new FObservable<>(true);
		var and = FObservable.ofAnd(a, b, disposables::add);
		assertTrue(and.getValue());
	}

	@Test
	public void testAndChangeToTrue() {
		FObservable<Boolean> a = new FObservable<>(false);
		FObservable<Boolean> b = new FObservable<>(true);
		var and = FObservable.ofAnd(a, b, disposables::add);
		disposables.add(and.onChange(booleanListener));
		a.setValue(true);
		assertTrue(and.getValue());
		verify(booleanListener).accept(true);
	}

	@Test
	public void testAndChangeToFalse() {
		FObservable<Boolean> a = new FObservable<>(true);
		FObservable<Boolean> b = new FObservable<>(true);
		var and = FObservable.ofAnd(a, b, disposables::add);
		disposables.add(and.onChange(booleanListener));
		a.setValue(false);
		assertFalse(and.getValue());
		verify(booleanListener).accept(false);
	}

	@Test
	public void testAndNoChangeStayFalse() {
		FObservable<Boolean> a = new FObservable<>(true);
		FObservable<Boolean> b = new FObservable<>(false);
		var and = FObservable.ofAnd(a, b, disposables::add);
		disposables.add(and.onChange(booleanListener));
		a.setValue(false);
		assertFalse(and.getValue());
		verify(booleanListener, never()).accept(false);
	}

	@Test
	public void testNotInitialTrue() {
		FObservable<Boolean> a = new FObservable<>(true);
		var not = FObservable.ofNot(a, disposables::add);
		assertFalse(not.getValue());
	}

	@Test
	public void testNotInitialFalse() {
		FObservable<Boolean> a = new FObservable<>(false);
		var not = FObservable.ofNot(a, disposables::add);
		assertTrue(not.getValue());
	}

	@Test
	public void testNotChangeToTrue() {
		FObservable<Boolean> a = new FObservable<>(true);
		var not = FObservable.ofNot(a, disposables::add);
		disposables.add(not.onChange(booleanListener));
		a.setValue(false);
		assertTrue(not.getValue());
		verify(booleanListener).accept(true);
	}

	@Test
	public void testNotChangeToFalse() {
		FObservable<Boolean> a = new FObservable<>(false);
		var not = FObservable.ofNot(a, disposables::add);
		disposables.add(not.onChange(booleanListener));
		a.setValue(true);
		assertFalse(not.getValue());
		verify(booleanListener).accept(false);
	}

	@Test
	public void testNotNoChangeStayTrue() {
		FObservable<Boolean> a = new FObservable<>(false);
		var not = FObservable.ofNot(a, disposables::add);
		disposables.add(not.onChange(booleanListener));
		a.setValue(false);
		assertTrue(not.getValue());
		verify(booleanListener, never()).accept(true);
	}

	@Test
	public void testNotNoChangeStayFalse() {
		FObservable<Boolean> a = new FObservable<>(true);
		var not = FObservable.ofNot(a, disposables::add);
		disposables.add(not.onChange(booleanListener));
		a.setValue(true);
		assertFalse(not.getValue());
		verify(booleanListener, never()).accept(false);
	}
}