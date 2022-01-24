package gay.nyako.infinitech.block.energy_infuser;

import gay.nyako.infinitech.InfinitechMod;
import gay.nyako.infinitech.block.AbstractMachineBlockEntity;
import gay.nyako.infinitech.block.MachineUtil;
import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WItemSlot;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerContext;

public class EnergyInfuserGuiDescription extends SyncedGuiDescription {
    private static final int PROPERTY_COUNT = 2;

    public EnergyInfuserGuiDescription(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, AbstractMachineBlockEntity blockEntity) {
        super(InfinitechMod.ENERGY_INFUSER_SCREEN_HANDLER, syncId, playerInventory, getBlockInventory(context, blockEntity.size()),getBlockPropertyDelegate(context, PROPERTY_COUNT));
        WPlainPanel root = new WPlainPanel();

        setRootPanel(root);
        setTitleAlignment(HorizontalAlignment.CENTER);

        root.setSize(176, 166);
        root.setInsets(Insets.ROOT_PANEL);

        MachineUtil.DrawEnergyBar(5, -1, root, blockInventory, blockEntity, 0, 1);

        MachineUtil.DrawSideButtons(120, 20, root, blockEntity, false, false);
        root.add(this.createPlayerInventoryPanel(), 0, 65);

        root.validate(this);
    }
}
