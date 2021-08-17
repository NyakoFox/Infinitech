package gay.nyako.infinitech.block.furnace_generator;

import gay.nyako.infinitech.InfinitechMod;
import gay.nyako.infinitech.WEnergyBar;
import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.util.Identifier;

public class FurnaceGeneratorGuiDescription extends SyncedGuiDescription {
    private static final int INVENTORY_SIZE = 1;
    private static final int PROPERTY_COUNT = 3;

    public FurnaceGeneratorGuiDescription(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(InfinitechMod.FURNACE_GENERATOR_SCREEN_HANDLER, syncId, playerInventory, getBlockInventory(context, INVENTORY_SIZE),getBlockPropertyDelegate(context, PROPERTY_COUNT));

        WPlainPanel root = new WPlainPanel();
        setRootPanel(root);
        setTitleAlignment(HorizontalAlignment.CENTER);

        root.setSize(176, 166);
        root.setInsets(Insets.ROOT_PANEL);

        Identifier fireBGSprite = new Identifier("infinitech:textures/gui/container/fire_empty.png");
        Identifier fireFGSprite = new Identifier("infinitech:textures/gui/container/fire_full.png");
        WBar fireBar = new WBar(fireBGSprite,fireFGSprite,0,1);
        root.add(fireBar, 47+2, 27+2, 14, 14);

        WItemSlot itemSlot = WItemSlot.of(blockInventory, 0);
        root.add(itemSlot, 72, 27);

        WEnergyBar energyBar = new WEnergyBar(2,200000, true);
        root.add(energyBar, 72 + 29, 27 - 15, 8, 48);

        root.add(this.createPlayerInventoryPanel(), 0, 65);

        root.validate(this);
    }
}
