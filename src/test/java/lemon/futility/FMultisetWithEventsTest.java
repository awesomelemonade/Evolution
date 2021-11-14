package lemon.futility;

import lemon.engine.toolbox.Disposables;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Consumer;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class FMultisetWithEventsTest {
    @Mock
    private Consumer<Integer> onRaiseAboveZero;

    @Mock
    private Consumer<Integer> onFallToZero;

    private FMultisetWithEvents<Integer> set;
    private final Disposables disposables = new Disposables();

    @BeforeEach
    public void setup() {
        set = new FMultisetWithEvents<>();
        disposables.add(set.onRaiseAboveZero(onRaiseAboveZero));
        disposables.add(set.onFallToZero(onFallToZero));
    }

    @AfterEach
    public void cleanup() {
        disposables.dispose();
    }

    @Test
    public void testAdd() {
        set.add(1);
        set.add(1);
        verify(onRaiseAboveZero).accept(1);
        assertTrue(set.contains(1));
        assertFalse(set.isEmpty());
    }
}