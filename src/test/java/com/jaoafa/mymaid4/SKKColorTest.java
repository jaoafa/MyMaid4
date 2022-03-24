package com.jaoafa.mymaid4;

import com.jaoafa.mymaid4.lib.SKKColorManager;
import net.kyori.adventure.text.format.NamedTextColor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SKKColorTest {
    NamedTextColor getCorrectColor(int count) {
        if (count <= 0) return NamedTextColor.GRAY;
        if (count <= 5) return NamedTextColor.WHITE;
        if (count <= 19) return NamedTextColor.DARK_BLUE;
        if (count <= 33) return NamedTextColor.BLUE;
        if (count <= 47) return NamedTextColor.AQUA;
        if (count <= 61) return NamedTextColor.DARK_AQUA;
        if (count <= 75) return NamedTextColor.DARK_GREEN;
        if (count <= 89) return NamedTextColor.GREEN;
        if (count <= 103) return NamedTextColor.YELLOW;
        if (count <= 117) return NamedTextColor.GOLD;
        if (count <= 131) return NamedTextColor.RED;
        if (count <= 145) return NamedTextColor.DARK_RED;
        if (count <= 159) return NamedTextColor.DARK_PURPLE;
        return NamedTextColor.LIGHT_PURPLE;
    }

    @Test
    public void checkVoteCountToColor() {
        for (int i = 0; i < 200; i++) {
            NamedTextColor correct = getCorrectColor(i);
            NamedTextColor actual = SKKColorManager.TextColors.get(SKKColorManager.calculateRank(i));
            assertEquals(correct, actual);
        }
    }
}
