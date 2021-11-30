package lemon.engine.texture;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class TextureBankTest {
    @Test
    public void testUniqueTextureId() {
        var idList = Arrays.stream(TextureBank.values()).map(TextureBank::id).toList();
        var idSet = new HashSet<>(idList);
        assertEquals(idSet.size(), idList.size());
    }
}