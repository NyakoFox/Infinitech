package gay.nyako.infinitech.block.pipe;

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;


public class PipeScreen extends CottonInventoryScreen<PipeGuiDescription> {
    public PipeScreen(PipeGuiDescription gui, PlayerEntity player, Text title) {
        super(gui, player, title);
    }
}