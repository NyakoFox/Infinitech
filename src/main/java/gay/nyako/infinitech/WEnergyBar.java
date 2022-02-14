package gay.nyako.infinitech;

import io.github.cottonmc.cotton.gui.widget.TooltipBuilder;
import io.github.cottonmc.cotton.gui.widget.WBar;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

public class WEnergyBar extends WBar {
    private static final Identifier ENERGY_BAR_BG = new Identifier(InfinitechMod.MOD_ID, "textures/gui/container/energy_empty.png");
    private static final Identifier ENERGY_BAR_FG = new Identifier(InfinitechMod.MOD_ID, "textures/gui/container/energy_full.png");

    public final int field;
    public final int differenceField;

    public Consumer<TooltipBuilder> tooltipCallback;

    public WEnergyBar(int field, int differenceField, int max, boolean constantMaximum) {
        super(ENERGY_BAR_BG, ENERGY_BAR_FG, field, (constantMaximum ? -1 : max), Direction.UP);
        this.field = field;
        this.differenceField = differenceField;
        if (constantMaximum) {
            this.maxValue = max;
        }
    }

    public WEnergyBar(int field, int differenceField, int max) {
        super(ENERGY_BAR_BG, ENERGY_BAR_FG, field, max, Direction.UP);
        this.field = field;
        this.differenceField = differenceField;
    }


    @Override
    public void addTooltip(TooltipBuilder information) {
        int difference = properties.get(differenceField);
        String differenceText = "(";
        if (difference < 0) {
            differenceText += "§c";
        } else if (difference > 0) {
            differenceText += "§a+";
        }
        differenceText += difference + " E/t§r)";

        if (max < 0) {
            information.add(Text.of("Energy: " + properties.get(field) + "/" + this.maxValue + " E " + differenceText));
        } else {
            information.add(Text.of("Energy: " + properties.get(field) + "/" + properties.get(max) + " E " + differenceText));
        }
        if (tooltipCallback != null) {
            tooltipCallback.accept(information);
        }
    }

    public void setTooltipCallback(Consumer<TooltipBuilder> tooltipCallback) {
        this.tooltipCallback = tooltipCallback;
    }
}