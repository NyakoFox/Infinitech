package gay.nyako.infinitech.item;

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class ItemFilterScreen extends CottonInventoryScreen<ItemFilterGuiDescription> {
    public ItemFilterScreen(ItemFilterGuiDescription gui, PlayerEntity player, Text title) {
        super(gui, player, title);
    }
}
