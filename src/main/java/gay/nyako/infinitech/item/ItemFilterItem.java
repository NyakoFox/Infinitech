package gay.nyako.infinitech.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class ItemFilterItem extends FilterItem<Item> {
    public ItemFilterItem(FabricItemSettings settings) {
        super(settings);
    }

    @Override
    public Text getDisplayName() {
        return Text.of("Item Filter");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new ItemFilterGuiDescription(syncId, inv, ScreenHandlerContext.create(player.world, player.getBlockPos()));
    }
}
