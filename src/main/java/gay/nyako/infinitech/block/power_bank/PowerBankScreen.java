package gay.nyako.infinitech.block.power_bank;

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class PowerBankScreen extends CottonInventoryScreen<PowerBankGuiDescription> {
    public PowerBankScreen(PowerBankGuiDescription gui, PlayerEntity player, Text title) {
        super(gui, player, title);
    }
}
