package gay.nyako.infinitech;

import gay.nyako.infinitech.block.MachineUtil;
import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WItemSlot;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.inventory.Inventory;

public class WColoredItemSlot extends WItemSlot {
    MachineUtil.SideTypes sideType;
    public WColoredItemSlot(Inventory inventory, int startIndex, int slotsWide, int slotsHigh, boolean big, MachineUtil.SideTypes sideType) {
        super(inventory, startIndex, slotsWide, slotsHigh, big);
        this.sideType = sideType;

    }
}
