package lemon.futility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FBufferedSetWithEventsTest {
	@Test
	public void testAddNoFlush() {
		var set = new FBufferedSetWithEvents<Integer>();
		for (int i = 0; i < 10; i++) {
			set.add(0);
		}
		for (int i = 1; i < 20; i++) {
			set.add(i);
		}
		assertFalse(set.contains(0));
		assertFalse(set.contains(1));
		assertTrue(set.isEmpty());
	}

	@Test
	public void testAddFlush() {
		var set = new FBufferedSetWithEvents<Integer>();
		for (int i = 0; i < 10; i++) {
			set.add(0);
		}
		for (int i = 1; i < 20; i++) {
			set.add(i);
		}
		set.flush();
		for (int i = 0; i < 20; i++) {
			assertTrue(set.contains(i));
		}
		for (int i = 20; i < 30; i++) {
			assertFalse(set.contains(i));
		}
		assertEquals(20, set.size());
	}

	@Test
	public void testRemoveNoFlush() {
		var set = new FBufferedSetWithEvents<Integer>();
		set.add(0);
		set.flush();
		set.remove(0);
		assertTrue(set.contains(0));
		assertEquals(1, set.size());
	}

	@Test
	public void testRemoveFlush() {
		var set = new FBufferedSetWithEvents<Integer>();
		set.add(0);
		set.flush();
		set.remove(0);
		set.flush();
		assertFalse(set.contains(0));
		assertEquals(0, set.size());
	}

	@Test
	public void testClearNoFlush() {
		var set = new FBufferedSetWithEvents<Integer>();
		set.add(0);
		set.add(1);
		set.flush();
		set.clear();
		assertTrue(set.contains(0));
		assertTrue(set.contains(1));
		assertEquals(2, set.size());
	}

	@Test
	public void testClearFlush() {
		var set = new FBufferedSetWithEvents<Integer>();
		set.add(0);
		set.add(1);
		set.flush();
		set.clear();
		set.flush();
		assertFalse(set.contains(0));
		assertFalse(set.contains(1));
		assertEquals(0, set.size());
	}

	@Test
	public void testClearFlush2() {
		var set = new FBufferedSetWithEvents<Integer>();
		set.add(0);
		set.add(1);
		set.clear();
		set.flush();
		assertFalse(set.contains(0));
		assertFalse(set.contains(1));
		assertEquals(0, set.size());
	}

	@Test
	public void testRemoveIfNoFlush() {
		var set = new FBufferedSetWithEvents<Integer>();
		for (int i = 0; i < 10; i++) {
			set.add(i);
		}
		set.flush();
		set.removeIf(x -> x > 7 || x <= 2);
		for (int i = 0; i < 10; i++) {
			assertTrue(set.contains(i));
		}
		assertEquals(10, set.size());
	}

	@Test
	public void testRemoveIfFlush() {
		var set = new FBufferedSetWithEvents<Integer>();
		for (int i = 0; i < 10; i++) {
			set.add(i);
		}
		set.flush();
		set.removeIf(x -> x > 7 || x <= 2);
		set.flush();
		assertFalse(set.contains(0));
		assertFalse(set.contains(1));
		assertFalse(set.contains(2));
		for (int i = 3; i <= 7; i++) {
			assertTrue(set.contains(i));
		}
		assertFalse(set.contains(8));
		assertFalse(set.contains(9));
		assertEquals(5, set.size());
	}

	@Test
	public void testNoConcurrentModificationException() {
		var set = new FBufferedSetWithEvents<Integer>();
		for (int i = 0; i < 10; i++) {
			set.add(i);
		}
		set.flush();
		for (var x : set) {
			set.remove(x);
			set.add(x + 10);
		}
		set.flush();
		for (int i = 10; i < 20; i++) {
			assertTrue(set.contains(i));
		}
	}
}