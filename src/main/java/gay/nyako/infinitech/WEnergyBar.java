package gay.nyako.infinitech;

import io.github.cottonmc.cotton.gui.widget.TooltipBuilder;
import io.github.cottonmc.cotton.gui.widget.WBar;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class WEnergyBar extends WBar {
    private static final Identifier ENERGY_BAR_BG = new Identifier("infinitech", "textures/gui/container/energy_empty.png");
    private static final Identifier ENERGY_BAR_FG = new Identifier("infinitech", "textures/gui/container/energy_full.png");

    public WEnergyBar(int field, int max, boolean constantMaximum) {
        super(ENERGY_BAR_BG, ENERGY_BAR_FG, field, (constantMaximum ? -1 : max), Direction.UP);
        if (constantMaximum) {
            this.maxValue = max;
        }
    }

    public WEnergyBar(int field, int max) {
        super(ENERGY_BAR_BG, ENERGY_BAR_FG, field, max, Direction.UP);
    }


    @Override
    public void addTooltip(TooltipBuilder information) {
        information.add(Text.of("Energy stored: " + properties.get(2) + "/200000 E"));
        information.add(Text.of("Generating: 20 E/t"));
        information.add(Text.of("Transfer rate: 1000 E/t"));
    }
}
