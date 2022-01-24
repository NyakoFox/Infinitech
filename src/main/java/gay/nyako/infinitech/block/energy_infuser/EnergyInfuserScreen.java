package gay.nyako.infinitech.block.energy_infuser;

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;


public class EnergyInfuserScreen extends CottonInventoryScreen<EnergyInfuserGuiDescription> {
    public EnergyInfuserScreen(EnergyInfuserGuiDescription gui, PlayerEntity player, Text title) {
        super(gui, player, title);
    }
}