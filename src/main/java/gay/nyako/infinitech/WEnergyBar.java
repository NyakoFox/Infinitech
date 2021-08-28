package gay.nyako.infinitech;

import io.github.cottonmc.cotton.gui.widget.TooltipBuilder;
import io.github.cottonmc.cotton.gui.widget.WBar;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

public class WEnergyBar extends WBar {
    private static final Identifier ENERGY_BAR_BG = new Identifier("infinitech", "textures/gui/container/energy_empty.png");
    private static final Identifier ENERGY_BAR_FG = new Identifier("infinitech", "textures/gui/container/energy_full.png");

    public final int field;

    public Consumer<TooltipBuilder> tooltipCallback;

    public WEnergyBar(int field, int max, boolean constantMaximum) {
        super(ENERGY_BAR_BG, ENERGY_BAR_FG, field, (constantMaximum ? -1 : max), Direction.UP);
        this.field = field;
        if (constantMaximum) {
            this.maxValue = max;
        }
    }

    public WEnergyBar(int field, int max) {
        super(ENERGY_BAR_BG, ENERGY_BAR_FG, field, max, Direction.UP);
        this.field = field;
    }


    @Override
    public void addTooltip(TooltipBuilder information) {
        if (max < 0) {
            information.add(Text.of("Energy stored: " + properties.get(field) + "/" + this.maxValue + " E"));
        } else {
            information.add(Text.of("Energy stored: " + properties.get(field) + "/" + properties.get(max) + " E"));
        }
        if (tooltipCallback != null) {
            tooltipCallback.accept(information);
        }
        /*information.add(Text.of("Energy stored: " + properties.get(field) + "/200000 E"));
        information.add(Text.of("Generating: 20 E/t"));
        information.add(Text.of("Transfer rate: 1000 E/t"));*/
    }

    public void setTooltipCallback(Consumer<TooltipBuilder> tooltipCallback) {
        this.tooltipCallback = tooltipCallback;
    }
}