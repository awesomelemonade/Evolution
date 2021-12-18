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
public class FSetWithEventsTest {
	@Mock
	private Consumer<Integer> onAdd;

	@Mock
	private Consumer<Integer> onRemove;

	@Mock
	private Consumer<Integer> onAnyChange;

	private FSetWithEvents<Integer> set;
	private final Disposables disposables = new Disposables();

	@BeforeEach
	public void setup() {
		set = new FSetWithEvents<>();
		disposables.add(set.onAdd(onAdd));
		disposables.add(set.onRemove(onRemove));
		disposables.add(set.onAnyChange(onAnyChange));
	}

	@AfterEach
	public void cleanup() {
		disposables.dispose();
	}

	@Test
	public void testAdd() {
		set.add(0);
		verify(onAdd).accept(0);
		verify(onAnyChange).accept(0);
		assertTrue(set.backingSet().contains(0));
		assertTrue(set.contains(0));
	}

	@Test
	public void testRepeatedAdd() {
		for (int i = 0; i < 10; i++) {
			set.add(0);
		}
		verify(onAdd, times(1)).accept(0);
		verify(onAnyChange, times(1)).accept(0);
		assertTrue(set.backingSet().contains(0));
		assertTrue(set.contains(0));
	}

	@Test
	public void testRemove() {
		set.add(0);
		set.remove(0);
		verify(onRemove).accept(0);
		verify(onAnyChange, times(2)).accept(0);
		assertFalse(set.backingSet().contains(0));
		assertFalse(set.contains(0));
	}

	@Test
	public void testRemoveWhenEmpty() {
		set.remove(0);
		verify(onRemove, never()).accept(0);
		verify(onAnyChange, never()).accept(0);
	}

	@Test
	public void testRemoveIfRemovingItems() {
		for (int i = 0; i < 10; i++) {
			set.add(i);
		}
		set.removeIf(x -> x < 5);
		for (int i = 0; i < 10; i++) {
			verify(onRemove, times(i < 5 ? 1 : 0)).accept(i);
			assertEquals(i < 5, !set.contains(i));
		}
	}
}