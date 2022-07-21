package gay.nyako.infinitech.item;

import gay.nyako.infinitech.InfinitechMod;
import gay.nyako.infinitech.WFakeSlot;
import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WItemSlot;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerContext;

public class ItemFilterGuiDescription extends SyncedGuiDescription {

    public ItemFilterGuiDescription(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(InfinitechMod.ITEM_FILTER_SCREEN_HANDLER, syncId, playerInventory);

        WGridPanel root = new WGridPanel();
        setRootPanel(root);
        root.setSize(176, 133);
        root.setInsets(Insets.ROOT_PANEL);

        var mainItemSlot = WFakeSlot.of(0);
        root.add(mainItemSlot, 0, 0);

        root.add(this.createPlayerInventoryPanel(), 0, 2);

        root.validate(this);
    }

}