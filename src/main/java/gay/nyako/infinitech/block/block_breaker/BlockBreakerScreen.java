package gay.nyako.infinitech.block.block_breaker;

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;


public class BlockBreakerScreen extends CottonInventoryScreen<BlockBreakerGuiDescription> {
    public BlockBreakerScreen(BlockBreakerGuiDescription gui, PlayerEntity player, Text title) {
        super(gui, player, title);
    }
}