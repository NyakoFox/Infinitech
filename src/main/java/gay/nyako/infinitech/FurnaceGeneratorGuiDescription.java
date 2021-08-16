package gay.nyako.infinitech;

import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WItemSlot;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.WSprite;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
        //root.setBackgroundPainter();

        root.setSize(176, 166);
        root.setInsets(Insets.ROOT_PANEL);

        WSprite fireSprite = new WSprite(new Identifier("infinitech:textures/gui/container/fire.png"), 0f / 36f, 0f / 18f, 18f / 36f, 18f / 18f);
        root.add(fireSprite, 47, 27);
        WItemSlot itemSlot = WItemSlot.of(blockInventory, 0);
        root.add(itemSlot, 72, 27);

        root.add(this.createPlayerInventoryPanel(), 0, 65);

        root.validate(this);
    }

    /*@Environment(EnvType.CLIENT)
    @Override
    public void addPainters() {
        getRootPanel().setBackgroundPainter(BackgroundPainter.createColorful(0xFF_15132b));
    }*/

}
