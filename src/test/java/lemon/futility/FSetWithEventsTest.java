package lemon.futility;

import lemon.engine.event.EventWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FSetWithEventsTest {
	@Mock
	private EventWith<Integer> onAdd;

	@Mock
	private EventWith<Integer> onRemove;

	private FSetWithEvents<Integer> set;

	@BeforeEach
	public void setup() {
		set = new FSetWithEvents<>(new HashSet<>(), onAdd, onRemove);
	}

	@Test
	public void testAdd() {
		set.add(0);
		verify(onAdd).callListeners(0);
		assertTrue(set.backingSet().contains(0));
	}

	@Test
	public void testRepeatedAdd() {
		for (int i = 0; i < 10; i++) {
			set.add(0);
		}
		verify(onAdd, times(1)).callListeners(0);
	}

	@Test
	public void testRemove() {
		set.add(0);
		set.remove(0);
		verify(onRemove).callListeners(0);
		assertFalse(set.backingSet().contains(0));
	}

	@Test
	public void testRemoveWhenEmpty() {
		set.remove(0);
		verify(onRemove, never()).callListeners(0);
	}
}