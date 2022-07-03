package gay.nyako.infinitech.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class ItemFilterItem extends FilterItem<ItemVariant> {
    public ItemFilterItem(FabricItemSettings settings) {
        super(settings);
    }

    @Override
    public ItemVariant getResourceFromNBT(NbtCompound nbt) {
        return ItemVariant.fromNbt(nbt);
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
