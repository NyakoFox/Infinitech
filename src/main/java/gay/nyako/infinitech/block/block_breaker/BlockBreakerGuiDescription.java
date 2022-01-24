package gay.nyako.infinitech.block.block_breaker;

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

public class BlockBreakerGuiDescription extends SyncedGuiDescription {
    private static final int PROPERTY_COUNT = 3;
    private WItemSlot mainItemSlot;

    public BlockBreakerGuiDescription(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, AbstractMachineBlockEntity blockEntity) {
        super(InfinitechMod.BLOCK_BREAKER_SCREEN_HANDLER, syncId, playerInventory, getBlockInventory(context, blockEntity.size()),getBlockPropertyDelegate(context, PROPERTY_COUNT));

        WPlainPanel root = new WPlainPanel();

        setRootPanel(root);
        setTitleAlignment(HorizontalAlignment.CENTER);

        root.setSize(176, 166);
        root.setInsets(Insets.ROOT_PANEL);

        mainItemSlot = WItemSlot.of(blockInventory, 0);
        root.add(mainItemSlot, 72, 27);

        MachineUtil.DrawEnergyBar(5, -1, root, blockInventory, blockEntity, 1, 2);

        MachineUtil.DrawSideButtons(120, 20, root, blockEntity, false, false);
        root.add(this.createPlayerInventoryPanel(), 0, 65);

        root.validate(this);
    }

    @Override
    public void addPainters() {
        super.addPainters();

        MachineUtil.ColorSlot(mainItemSlot, MachineUtil.SideTypes.INPUT);
    }
}
