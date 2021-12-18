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
	private Consumer<Integer> onChange;
	@Mock
	private Consumer<Integer> onChangeAndRun;
	@Mock
	private Consumer<Boolean> booleanListener;

	private Observable<Integer> observable;
	private final Disposables disposables = new Disposables();

	private static final int INITIAL_VALUE = 10;

	@BeforeEach
	public void setup() {
		observable = new Observable<>(INITIAL_VALUE);
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
		Observable<Boolean> a = new Observable<>(false);
		Observable<Boolean> b = new Observable<>(false);
		var and = Observable.ofAnd(a, b, disposables::add);
		assertFalse(and.getValue());
	}

	@Test
	public void testAndInitialAndTrueFalse() {
		Observable<Boolean> a = new Observable<>(true);
		Observable<Boolean> b = new Observable<>(false);
		var and = Observable.ofAnd(a, b, disposables::add);
		assertFalse(and.getValue());
	}

	@Test
	public void testAndInitialTrueTrue() {
		Observable<Boolean> a = new Observable<>(true);
		Observable<Boolean> b = new Observable<>(true);
		var and = Observable.ofAnd(a, b, disposables::add);
		assertTrue(and.getValue());
	}

	@Test
	public void testAndChangeToTrue() {
		Observable<Boolean> a = new Observable<>(false);
		Observable<Boolean> b = new Observable<>(true);
		var and = Observable.ofAnd(a, b, disposables::add);
		disposables.add(and.onChange(booleanListener));
		a.setValue(true);
		assertTrue(and.getValue());
		verify(booleanListener).accept(true);
	}

	@Test
	public void testAndChangeToFalse() {
		Observable<Boolean> a = new Observable<>(true);
		Observable<Boolean> b = new Observable<>(true);
		var and = Observable.ofAnd(a, b, disposables::add);
		disposables.add(and.onChange(booleanListener));
		a.setValue(false);
		assertFalse(and.getValue());
		verify(booleanListener).accept(false);
	}

	@Test
	public void testAndNoChangeStayFalse() {
		Observable<Boolean> a = new Observable<>(true);
		Observable<Boolean> b = new Observable<>(false);
		var and = Observable.ofAnd(a, b, disposables::add);
		disposables.add(and.onChange(booleanListener));
		a.setValue(false);
		assertFalse(and.getValue());
		verify(booleanListener, never()).accept(false);
	}

	@Test
	public void testNotInitialTrue() {
		Observable<Boolean> a = new Observable<>(true);
		var not = Observable.ofNot(a, disposables::add);
		assertFalse(not.getValue());
	}

	@Test
	public void testNotInitialFalse() {
		Observable<Boolean> a = new Observable<>(false);
		var not = Observable.ofNot(a, disposables::add);
		assertTrue(not.getValue());
	}

	@Test
	public void testNotChangeToTrue() {
		Observable<Boolean> a = new Observable<>(true);
		var not = Observable.ofNot(a, disposables::add);
		disposables.add(not.onChange(booleanListener));
		a.setValue(false);
		assertTrue(not.getValue());
		verify(booleanListener).accept(true);
	}

	@Test
	public void testNotChangeToFalse() {
		Observable<Boolean> a = new Observable<>(false);
		var not = Observable.ofNot(a, disposables::add);
		disposables.add(not.onChange(booleanListener));
		a.setValue(true);
		assertFalse(not.getValue());
		verify(booleanListener).accept(false);
	}

	@Test
	public void testNotNoChangeStayTrue() {
		Observable<Boolean> a = new Observable<>(false);
		var not = Observable.ofNot(a, disposables::add);
		disposables.add(not.onChange(booleanListener));
		a.setValue(false);
		assertTrue(not.getValue());
		verify(booleanListener, never()).accept(true);
	}

	@Test
	public void testNotNoChangeStayFalse() {
		Observable<Boolean> a = new Observable<>(true);
		var not = Observable.ofNot(a, disposables::add);
		disposables.add(not.onChange(booleanListener));
		a.setValue(true);
		assertFalse(not.getValue());
		verify(booleanListener, never()).accept(false);
	}
}