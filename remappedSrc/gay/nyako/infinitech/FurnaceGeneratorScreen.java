package gay.nyako.infinitech;

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;



public class FurnaceGeneratorScreen extends CottonInventoryScreen<FurnaceGeneratorGuiDescription> {
    public FurnaceGeneratorScreen(FurnaceGeneratorGuiDescription gui, PlayerEntity player, Text title) {
        super(gui, player, title);
    }
}