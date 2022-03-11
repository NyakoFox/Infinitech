package gay.nyako.infinitech.block.pipe;

import gay.nyako.infinitech.InfinitechMod;
import gay.nyako.infinitech.block.AbstractMachineBlockEntity;
import gay.nyako.infinitech.block.MachineUtil;
import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.util.math.BlockPos;

public class PipeGuiDescription extends SyncedGuiDescription {

    public PipeGuiDescription(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, BlockPos blockPos) {
        super(InfinitechMod.PIPE_GUI_SCREEN_HANDLER, syncId, playerInventory);

        WPlainPanel root = new WPlainPanel();

        setRootPanel(root);
        setTitleAlignment(HorizontalAlignment.CENTER);

        root.setSize(176, 166);
        root.setInsets(Insets.ROOT_PANEL);

        root.add(this.createPlayerInventoryPanel(), 0, 65);

        root.validate(this);
    }
}
