package gay.nyako.infinitech.block.furnace_generator;

import gay.nyako.infinitech.InfinitechMod;
import gay.nyako.infinitech.WEnergyBar;
import gay.nyako.infinitech.WSideButton;
import gay.nyako.infinitech.block.AbstractMachineBlockEntity;
import gay.nyako.infinitech.block.MachineUtil;
import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class FurnaceGeneratorGuiDescription extends SyncedGuiDescription {
    private static final int PROPERTY_COUNT = 4;
    private WItemSlot itemSlot;

    public FurnaceGeneratorGuiDescription(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, AbstractMachineBlockEntity blockEntity) {
        super(InfinitechMod.FURNACE_GENERATOR_SCREEN_HANDLER, syncId, playerInventory, getBlockInventory(context, blockEntity.size()),getBlockPropertyDelegate(context, PROPERTY_COUNT));

        WPlainPanel root = new WPlainPanel();

        setRootPanel(root);
        setTitleAlignment(HorizontalAlignment.CENTER);

        root.setSize(176, 166);
        root.setInsets(Insets.ROOT_PANEL);

        Identifier fireBGSprite = new Identifier("infinitech:textures/gui/container/fire_empty.png");
        Identifier fireFGSprite = new Identifier("infinitech:textures/gui/container/fire_full.png");
        WBar fireBar = new WBar(fireBGSprite,fireFGSprite,0,1);
        root.add(fireBar, 47+2, 27+2, 14, 14);

        itemSlot = WItemSlot.of(blockInventory, 0);
        root.add(itemSlot, 72, 27);

        MachineUtil.DrawEnergyBar(5, -1, root, blockInventory, blockEntity, 2, 3);

        MachineUtil.DrawSideButtons(120, 20, root, blockEntity, false, true);

        root.add(this.createPlayerInventoryPanel(), 0, 65);

        root.validate(this);
    }

    @Override
    public void addPainters() {
        super.addPainters();

        MachineUtil.ColorSlot(itemSlot, MachineUtil.SideTypes.INPUT);
    }
}
