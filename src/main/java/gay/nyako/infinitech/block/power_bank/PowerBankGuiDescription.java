package gay.nyako.infinitech.block.power_bank;

import gay.nyako.infinitech.InfinitechMod;
import gay.nyako.infinitech.WEnergyBar;
import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.TooltipBuilder;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class PowerBankGuiDescription extends SyncedGuiDescription {
    private static final int INVENTORY_SIZE = 0;
    private static final int PROPERTY_COUNT = 1;

    public PowerBankGuiDescription(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(InfinitechMod.POWER_BANK_SCREEN_HANDLER, syncId, playerInventory, getBlockInventory(context, INVENTORY_SIZE), getBlockPropertyDelegate(context, PROPERTY_COUNT));

        WGridPanel root = new WGridPanel();
        setRootPanel(root);
        root.setSize(176, 166);
        root.setInsets(Insets.ROOT_PANEL);

        WEnergyBar energyBar = new WEnergyBar(0,2_000_000, true);
        energyBar.setTooltipCallback(information -> {
            information.add(Text.of("Transfer rate: 10000 E/t"));
        });
        root.add(energyBar, 4, 1, 1, 3);
        energyBar.setSize(8, 48);

        root.add(this.createPlayerInventoryPanel(), 0, 4);

        root.validate(this);
    }

}